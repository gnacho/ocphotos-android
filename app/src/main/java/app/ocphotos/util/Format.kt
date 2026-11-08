package app.ocphotos.util

import java.util.Locale

// Human-readable file sizes, mirroring the web viewer (B/KB/MB/GB).
object ByteFormat {
    fun human(size: Long): String {
        if (size < 1024) return "$size B"
        val kb = size / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format(Locale.US, "%.1f GB", gb)
    }
}

object Geo {
    // Round a coordinate to `decimals` decimals (~1 km with 2), mirroring the backend
    // PlaceClusters grouping.
    fun round(value: Double, decimals: Int = 2): Double {
        val factor = Math.pow(10.0, decimals.toDouble())
        return Math.round(value * factor) / factor
    }
}
