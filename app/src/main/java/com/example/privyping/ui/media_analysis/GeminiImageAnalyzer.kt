package com.example.privyping.ui.media_analysis

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.privyping.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiImageAnalyzer {

    private const val ENDPOINT =
        "https://api-inference.huggingface.co/models/umm-maybe/AI-image-detector"


    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun analyze(
        context: Context,
        imageUri: Uri,
        onResult: (ImageAnalysisResult) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val bitmap = android.graphics.BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(imageUri)
            )

            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
                bitmap,
                512,
                (bitmap.height * 512) / bitmap.width,
                true
            )

            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(
                android.graphics.Bitmap.CompressFormat.JPEG,
                80,
                outputStream
            )

            val imageBytes = outputStream.toByteArray()

                ?: run {
                    onError("Failed to read image")
                    return
                }

            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            // ✅ VERY IMPORTANT PROMPT
            val prompt = """
                You are an image forensics expert.
                Determine whether the given image is REAL or AI-GENERATED.

                Respond strictly in this JSON format:
                {
                  "verdict": "REAL or AI",
                  "confidence": number between 0 and 100,
                  "summary": "short explanation",
                  "key_patterns": ["pattern1", "pattern2", "pattern3"]
                }
            """.trimIndent()

            // ✅ CORRECT GEMINI VISION REQUEST
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                            put(
                                JSONObject().put(
                                    "inline_data",
                                    JSONObject().apply {
                                        put("mime_type", "image/jpeg")
                                        put("data", base64Image)
                                    }
                                )
                            )
                        })
                    })
                })
            }



            val request = Request.Builder()
                .url("$ENDPOINT?key=${BuildConfig.HF_API_KEY}")
                .post(
                    RequestBody.create(
                        "application/json".toMediaType(),
                        requestJson.toString()
                    )
                )
                .addHeader("Content-Type", "application/json")
                .build()



            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    onError("Network error: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {

                    val body = response.body?.string()

                    // 🔍 DEBUG LOGS — ADD HERE
                    android.util.Log.e("GeminiDebug", "HTTP Code: ${response.code}")
                    android.util.Log.e("GeminiDebug", "Response Body: ${body ?: "null"}")

                    if (!response.isSuccessful || body == null) {
                        onError("API Error (${response.code})")
                        return
                    }

                    parseResponse(body, onResult, onError)
                }

            })

        } catch (e: Exception) {
            onError("Unexpected error: ${e.message}")
        }
    }

    private fun parseResponse(
        response: String,
        onResult: (ImageAnalysisResult) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val text = JSONObject(response)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            // Gemini returns JSON inside text — parse it
            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}")
            val json = JSONObject(text.substring(jsonStart, jsonEnd + 1))

            onResult(
                ImageAnalysisResult(
                    isAiGenerated = json.getString("verdict")
                        .equals("AI", ignoreCase = true),
                    confidence = json.getInt("confidence"),
                    summary = json.getString("summary"),
                    keyPatterns = json.getJSONArray("key_patterns")
                        .let { arr ->
                            List(arr.length()) { arr.getString(it) }
                        }
                )
            )

        } catch (e: Exception) {
            onError("Failed to parse AI response")
        }
    }
}
