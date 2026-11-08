package app.ocphotos.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Date/time formatting. All backend timestamps are Unix SECONDS.
object TimeFormat {
    fun formatRelativeTime(epochSeconds: Long): String {
        val diff = System.currentTimeMillis() / 1000 - epochSeconds
        val minutes = diff / 60
        val hours = diff / 3600
        val days = diff / 86400
        return when {
            minutes < 1 -> "now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours h ago"
            days < 7 -> "$days d ago"
            days < 365 -> "${days / 7} w ago"
            else -> "${days / 365} y ago"
        }
    }

    fun formatFullDate(epochSeconds: Long): String =
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date(epochSeconds * 1000))

    fun formatShortDate(epochSeconds: Long): String =
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(epochSeconds * 1000))

    fun formatMonth(epochSeconds: Long): String =
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(epochSeconds * 1000))

    // Day bucket label for the timeline (keyed by local date).
    fun dayKey(epochSeconds: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = epochSeconds * 1000
        return "%04d-%02d-%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH),
        )
    }

    fun year(epochSeconds: Long): Int {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = epochSeconds * 1000
        return cal.get(java.util.Calendar.YEAR)
    }

    fun durationLabel(ms: Long): String = when {
        ms < 1000 -> "${ms}ms"
        ms < 60000 -> "${TimeUnit.MILLISECONDS.toSeconds(ms)}s"
        else -> {
            val m = TimeUnit.MILLISECONDS.toMinutes(ms)
            val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
            "$m:${s.toString().padStart(2, '0')}"
        }
    }
}
