package kim.bifrost.coldrain.restarter

import org.bukkit.util.NumberConversions
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * kim.bifrost.coldrain.restarter.TimeParser
 * BifrostRestarter
 *
 * @author 寒雨
 * @since 2021/8/10 18:32
 **/
object Utils {

    val pattern = Pattern.compile("((?<hour>\\d+)h)?((?<minute>\\d+)min)?((?<second>\\d+)s)?")

    // 将字符串解析为timeMillis
    fun parseTimeMillis(str: String): Long {
        val m = pattern.matcher(str)
        var seconds = 0L
        var minute = 0L
        var hour = 0L
        if (m.find()) {
            try {
                hour = NumberConversions.toLong(m.group("hour"))
            } catch (ignored: IllegalStateException) {
                ignored.printStackTrace()
            }
            try {
                minute = NumberConversions.toLong(m.group("minute"))
            } catch (ignored: IllegalStateException) {
                ignored.printStackTrace()
            }
            try {
                seconds = NumberConversions.toLong(m.group("second"))
            } catch (ignored: IllegalStateException) {
                ignored.printStackTrace()
            }
        }
        return TimeUnit.SECONDS.toMillis(seconds) + TimeUnit.MINUTES.toMillis(minute) + TimeUnit.HOURS.toMillis(hour)
    }
}