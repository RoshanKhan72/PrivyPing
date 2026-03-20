package com.swavik.privyping.multimedia

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class SimpleVideoDetector(private val context: Context) {
    
    private var interpreter: Interpreter? = null
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(299, 299, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()
    
    companion object {
        private const val TAG = "SimpleVideoDetector"
        private const val MODEL_FILE = "ai_detector_model.tflite"
    }
    
    init {
        try {
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options()
            options.setNumThreads(4)
            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "✅ Simple Video Detector loaded")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading model: ${e.message}")
        }
    }
    
    private fun loadModelFile(): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_FILE)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    fun analyzeVideoSimple(videoUri: Uri): VideoAnalysisResult {
        Log.d(TAG, "🎬 Starting simple video analysis")
        Log.d(TAG, "🎬 Input URI: $videoUri")
        Log.d(TAG, "🎬 URI Scheme: ${videoUri.scheme}")
        Log.d(TAG, "🎬 URI Authority: ${videoUri.authority}")
        Log.d(TAG, "🎬 URI Path: ${videoUri.path}")
        
        val startTime = System.currentTimeMillis()
        
        return try {
            val retriever = MediaMetadataRetriever()
            
            // Try to set data source
            try {
                Log.d(TAG, "🎬 Setting data source with context and URI...")
                retriever.setDataSource(context, videoUri)
                Log.d(TAG, "✅ Data source set successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error setting data source: ${e.message}", e)
                return createErrorResult("Failed to load video: ${e.message}")
            }
            
            // Get basic metadata
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull()
            
            Log.d(TAG, "🎬 Video metadata: duration=${duration}ms, size=${width}x${height}, rotation=$rotation")
            
            if (duration == null || duration == 0L) {
                Log.e(TAG, "❌ Invalid video - duration is null or 0")
                return createErrorResult("Invalid video file - no duration")
            }
            
            if (width == null || width == 0 || height == null || height == 0) {
                Log.e(TAG, "❌ Invalid video - dimensions are invalid")
                return createErrorResult("Invalid video file - no dimensions")
            }
            
            // Extract frames at different points
            val frameResults = mutableListOf<FrameResult>()
            val frameCount = 5 // Analyze 5 frames
            
            for (i in 0 until frameCount) {
                val timeMs = (duration * i / (frameCount - 1)).toLong()
                Log.d(TAG, "🎬 Extracting frame $i at ${timeMs}ms (${(timeMs * 100 / duration)}% of video)")
                
                try {
                    val bitmap = retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    
                    if (bitmap != null) {
                        Log.d(TAG, "🎬 Frame $i extracted successfully: ${bitmap.width}x${bitmap.height}")
                        val result = analyzeFrame(bitmap, i)
                        frameResults.add(result)
                        Log.d(TAG, "📊 Frame $i: ${if (result.isAI) "AI" else "Real"} (${result.confidence}%)")
                    } else {
                        Log.w(TAG, "⚠️ Frame $i is null - no frame at this timestamp")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error extracting frame $i: ${e.message}", e)
                }
            }
            
            try {
                retriever.release()
                Log.d(TAG, "✅ MediaMetadataRetriever released")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error releasing retriever: ${e.message}")
            }
            
            // Calculate overall result
            val aiFrames = frameResults.count { it.isAI }
            val totalFrames = frameResults.size
            val aiPercentage = if (totalFrames > 0) (aiFrames * 100 / totalFrames) else 0
            val avgConfidence = if (totalFrames > 0) frameResults.map { it.confidence }.average().toInt() else 50
            
            val isAI = aiPercentage > 50
            val processingTime = System.currentTimeMillis() - startTime
            
            Log.d(TAG, "🎬 Analysis complete:")
            Log.d(TAG, "   - Total frames analyzed: $totalFrames")
            Log.d(TAG, "   - AI frames: $aiFrames")
            Log.d(TAG, "   - AI percentage: $aiPercentage%")
            Log.d(TAG, "   - Average confidence: $avgConfidence%")
            Log.d(TAG, "   - Overall result: ${if (isAI) "AI Generated" else "Real"}")
            Log.d(TAG, "   - Processing time: ${processingTime}ms")
            
            VideoAnalysisResult(
                isAI = isAI,
                confidence = avgConfidence,
                processingTime = processingTime,
                modelVersion = "Simple-Video-Detector-v2",
                details = "Analyzed $totalFrames frames, $aiPercentage% AI content (${width}x${height})",
                frameCount = totalFrames,
                videoDuration = duration,
                frameResults = emptyList(), // Simplified
                aiFramePercentage = aiPercentage.toFloat(),
                analysisSummary = "Simple analysis: $aiFrames AI frames out of $totalFrames, duration: ${duration}ms"
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Critical error in simple analysis: ${e.message}", e)
            createErrorResult("Critical analysis failure: ${e.message}")
        }
    }
    
    private fun analyzeFrame(bitmap: Bitmap, frameIndex: Int): FrameResult {
        return try {
            interpreter?.let { tfInterpreter ->
                val tensorImage = TensorImage.fromBitmap(bitmap)
                val processedImage = imageProcessor.process(tensorImage)
                
                val outputBuffer = Array(1) { FloatArray(2) }
                tfInterpreter.run(processedImage.buffer, outputBuffer)
                
                val probabilities = outputBuffer[0]
                val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
                val confidence = probabilities[maxIndex]
                
                val isAI = maxIndex == 1 // Index 1 = AI Generated
                val confidencePercent = (confidence * 100).toInt()
                
                FrameResult(frameIndex, isAI, confidencePercent)
            } ?: FrameResult(frameIndex, false, 50)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error analyzing frame $frameIndex: ${e.message}")
            FrameResult(frameIndex, false, 50)
        }
    }
    
    private fun createErrorResult(error: String): VideoAnalysisResult {
        return VideoAnalysisResult(
            isAI = false,
            confidence = 0,
            processingTime = 0,
            modelVersion = "Error",
            details = error,
            frameCount = 0,
            videoDuration = 0,
            frameResults = emptyList(),
            aiFramePercentage = 0f,
            analysisSummary = "Analysis failed: $error"
        )
    }
    
    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

data class FrameResult(
    val frameIndex: Int,
    val isAI: Boolean,
    val confidence: Int
)
