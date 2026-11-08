package app.ocphotos.util

import app.ocphotos.data.local.AssetEntity

// Groups a timeline-ordered asset list (takenAt DESC) into day buckets, preserving
// order. The label is the local day key; screens format it with TimeFormat.
fun groupByDay(assets: List<AssetEntity>): List<Pair<String, List<AssetEntity>>> {
    val result = mutableListOf<Pair<String, List<AssetEntity>>>()
    for (a in assets) {
        val key = TimeFormat.dayKey(a.takenAt)
        val last = result.lastOrNull()
        if (last != null && last.first == key) {
            result[result.size - 1] = key to (last.second + a)
        } else {
            result += key to listOf(a)
        }
    }
    return result
}
