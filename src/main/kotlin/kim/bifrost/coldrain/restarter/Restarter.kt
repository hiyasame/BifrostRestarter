package kim.bifrost.coldrain.restarter

import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.*
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.HexColor
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile
import taboolib.module.metrics.Metrics
import taboolib.platform.BukkitPlugin
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object Restarter : Plugin() {

    @Config(migrate = true)
    lateinit var conf: SecuredFile
        private set

    val reminds = ConcurrentHashMap<Long,ConfigurationSection>()

    val dateFormat = SimpleDateFormat("HH:mm")

    val time: String by lazy {
        conf.getString("settings.time")
    }

    val timeMillis by lazy {
        val current = dateFormat.format(Date())
        System.currentTimeMillis() - dateFormat.parse(current).time + dateFormat.parse(time).time
    }

    @Awake(LifeCycle.ENABLE)
    fun load() {
        reminds.clear()
        conf.getConfigurationSection("settings.remind").also {
            it.getKeys(false).forEach { s ->
                reminds[Utils.parseTimeMillis(s)] = it.getConfigurationSection(s)
            }
        }
        dateFormat.parse(time).time
    }

    @Awake(LifeCycle.ENABLE)
    fun task() {
        Metrics(12384, BukkitPlugin.getInstance().description.version, Platform.BUKKIT)
        submit(async = true, period = 300) {
            if (dateFormat.format(System.currentTimeMillis()) == time) {
                onlinePlayers().forEach {
                    submit {
                        it.kick(HexColor.translate(conf.getString("settings.kick-message")))
                    }
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
                            actionbar?.let { s -> it.sendActionBar(HexColor.translate(s)) }
                            message?.let { strs -> strs.forEach { s -> it.sendMessage(HexColor.translate(s)) } }
                            title?.let { strs -> it.sendTitle(strs.getOrNull(0)?.run { HexColor.translate(this) }, strs.getOrNull(1)?.run { HexColor.translate(this) }, 10, 40, 10) }
                        }
                        message?.let { it.forEach { s -> console().sendMessage(HexColor.translate(s)) } }
                        reminds.remove(it)
                    }
                }
            }
        }
    }
}