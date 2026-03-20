package com.example.privyping.ui.media_analysis.hive

data class HiveResponse(
    val status: String,
    val results: HiveResults
)

data class HiveResults(
    val categories: List<HiveCategory>
)

data class HiveCategory(
    val label: String,
    val score: Double
)
