package kim.bifrost.coldrain.restarter

import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.*
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.HexColor
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile
import taboolib.module.metrics.Metrics
import taboolib.platform.BukkitPlugin
import java.text.SimpleDateFormat

object Restarter : Plugin() {

    @Config(migrate = true)
    lateinit var conf: SecuredFile
        private set

    val reminds = HashMap<Long,ConfigurationSection>()

    val dateFormat = SimpleDateFormat("HH:mm")

    val time: String by lazy {
        conf.getString("settings.time")
    }

    val timeMillis by lazy {
        dateFormat.parse(time).time
    }

    @Awake(LifeCycle.ENABLE)
    fun load() {
        reminds.clear()
        conf.getConfigurationSection("settings.remind").also {
            it.getKeys(false).forEach { s ->
                reminds[Utils.parseTimeMillis(s)] = it.getConfigurationSection(s)
            }
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun task() {
        Metrics(12384, BukkitPlugin.getInstance().description.version, Platform.BUKKIT)
        submit(async = true, period = 300) {
            if (dateFormat.format(System.currentTimeMillis()) == time) {
                onlinePlayers().forEach {
                    it.kick(HexColor.translate(conf.getString("settings.kick-message")))
                }
                console().sendMessage(HexColor.translate("&7&l[&f&lBifrostRestart&7&l] &f重启服务器..."))
                Bukkit.shutdown()
            } else {
                reminds.keys.forEach {
                    if (it > (timeMillis - System.currentTimeMillis())) {
                        val section = reminds[it]!!
                        val message: List<String>? = section.getStringList("message")
                        val actionbar = section.getString("actionbar")
                        val title: List<String>? = section.getStringList("title")
                        onlinePlayers().forEach {
                            actionbar?.let { s -> it.sendActionBar(s) }
                            message?.let { strs -> strs.forEach { s -> it.sendMessage(s) } }
                            title?.let { strs -> it.sendTitle(strs.getOrNull(0), strs.getOrNull(1), 10, 40, 10) }
                        }
                        reminds.remove(it)
                    }
                }
            }
        }
    }
}