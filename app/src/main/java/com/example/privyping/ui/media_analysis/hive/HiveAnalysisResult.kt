package com.example.privyping.ui.media_analysis.hive

data class HiveAnalysisResult(
    val verdict: String,
    val confidence: Int,
    val rawScore: Double
)
