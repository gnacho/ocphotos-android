package app.ocphotos.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Date/time formatting. All backend timestamps are Unix SECONDS.
object TimeFormat {
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

    fun month(epochSeconds: Long): Int {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = epochSeconds * 1000
        return cal.get(java.util.Calendar.MONTH) + 1
    }

    // Month name in the device locale (the scrubber must not be English only).
    fun monthShortName(month: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.MONTH, month - 1)
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        return SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
    }

}
