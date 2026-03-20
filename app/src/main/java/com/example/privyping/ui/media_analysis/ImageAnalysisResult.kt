package com.example.privyping.ui.media_analysis

data class ImageAnalysisResult(
    val isAiGenerated: Boolean,
    val confidence: Int,
    val summary: String,
    val keyPatterns: List<String>
)
