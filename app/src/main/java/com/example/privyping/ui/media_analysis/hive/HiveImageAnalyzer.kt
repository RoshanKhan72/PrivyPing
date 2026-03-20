package com.example.privyping.ui.media_analysis.hive

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.privyping.ui.media_analysis.ImageAnalysisResult
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import java.util.concurrent.TimeUnit

object HiveImageAnalyzer {

    private const val TAG = "HiveImageAnalyzer"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun analyze(
        context: Context,
        imageUri: Uri,
        onResult: (ImageAnalysisResult) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: run {
                    onError("Failed to read image")
                    return
                }

            val imageBytes = inputStream.readBytes()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "image.jpg",
                    RequestBody.create("image/jpeg".toMediaType(), imageBytes)
                )
                .build()

            val request = Request.Builder()
                .url("${HiveConfig.BASE_URL}/classify/image")
                .addHeader("Authorization", "Bearer ${HiveConfig.API_KEY}")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Network error", e)
                    onError("Network error")
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()

                    if (!response.isSuccessful || body == null) {
                        Log.e(TAG, "HTTP ${response.code}: $body")
                        onError("API Error ${response.code}")
                        return
                    }

                    parseHiveResponse(body, onResult, onError)
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            onError("Unexpected error")
        }
    }

    private fun parseHiveResponse(
        json: String,
        onResult: (ImageAnalysisResult) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val gson = com.google.gson.Gson()
            val response = gson.fromJson(json, HiveResponse::class.java)

            val aiScore = response.results.categories
                .firstOrNull { it.label.contains("ai", ignoreCase = true) }
                ?.score ?: 0.0

            val isAiGenerated = aiScore >= 0.75

            val summary = when {
                aiScore >= 0.75 -> "Image shows patterns commonly found in AI-generated media."
                aiScore >= 0.4 -> "Image contains mixed signals; origin cannot be confidently determined."
                else -> "Image likely originates from a real camera source."
            }

            val keyPatterns = response.results.categories
                .sortedByDescending { it.score }
                .take(3)
                .map { "${it.label} (${(it.score * 100).toInt()}%)" }

            onResult(
                ImageAnalysisResult(
                    isAiGenerated = isAiGenerated,
                    confidence = (aiScore * 100).toInt(),
                    summary = summary,
                    keyPatterns = keyPatterns
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Parsing error", e)
            onError("Failed to parse response")
        }
    }
}
