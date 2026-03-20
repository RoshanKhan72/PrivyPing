package com.swavik.privyping.multimedia

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding

class BetterVideoActivity : AppCompatActivity() {
    
    private lateinit var selectVideoButton: Button
    private lateinit var analyzeVideoButton: Button
    private lateinit var videoThumbnail: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var videoInfoText: TextView
    private lateinit var mainContainer: LinearLayout
    
    private var videoUri: Uri? = null
    private var videoDetector: SimpleVideoDetector? = null
    
    private val videoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                videoUri = uri
                loadVideoThumbnail(uri)
                analyzeVideoButton.isEnabled = true
                showVideoInfo(uri)
                Toast.makeText(this, "Video selected for analysis", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBetterUI()
        setupVideoDetector()
        checkPermissions()
    }
    
    private fun setupBetterUI() {
        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)  // Reduced from 32
            setBackgroundColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.white))
        }
        
        // Title
        val titleView = TextView(this).apply {
            text = "🎬 AI Video Analysis"
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.black))
            setPadding(0, 0, 0, 16)
            gravity = android.view.Gravity.CENTER
        }
        
        // Video Info
        videoInfoText = TextView(this).apply {
            text = "No video selected"
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.darker_gray))
            setPadding(12, 6, 12, 6)
            setBackgroundColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.background_light))
        }
        
        // Video Thumbnail
        videoThumbnail = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.darker_gray))
            minimumHeight = 120  // Reduced from 200
            setImageResource(android.R.drawable.ic_menu_gallery)
            setPadding(4, 4, 4, 4)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120  // Fixed height instead of WRAP_CONTENT
            )
        }
        
        // Buttons Container
        val buttonsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
        }
        
        // Select Video Button
        selectVideoButton = Button(this).apply {
            text = "📹 Select"
            setBackgroundColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.holo_blue_dark))
            setTextColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.white))
            setPadding(16, 8, 16, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                    setMargins(0, 0, 4, 0)
                }
            setOnClickListener { selectVideo() }
        }
        
        // Analyze Video Button
        analyzeVideoButton = Button(this).apply {
            text = "🔍 Analyze"
            setBackgroundColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.holo_orange_dark))
            setTextColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.white))
            setPadding(16, 8, 16, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                    setMargins(4, 0, 0, 0)
                }
            isEnabled = false
            setOnClickListener { analyzeVideo() }
        }
        
        buttonsContainer.addView(selectVideoButton)
        buttonsContainer.addView(analyzeVideoButton)
        
        // Progress Container
        val progressContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 12)
            visibility = View.GONE
        }
        
        // Progress Bar
        progressBar = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Progress Text
        progressText = TextView(this).apply {
            text = ""
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.black))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }
        
        progressContainer.addView(progressBar)
        progressContainer.addView(progressText)
        
        // Result Text (no ScrollView for compact display)
        resultTextView = TextView(this).apply {
            text = "Select a video to analyze for AI-generated content\n\nFeatures:\n• Real-time AI detection\n• Frame-by-frame analysis\n• Deepfake pattern recognition\n• Confidence scoring"
            textSize = 9f  // Reduced from 10f
            typeface = Typeface.MONOSPACE
            setTextColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.black))
            setBackgroundColor(ContextCompat.getColor(this@BetterVideoActivity, android.R.color.white))
            setPadding(8, 8, 8, 8)  // Reduced from 12
            gravity = android.view.Gravity.CENTER
            // Set a fixed width to ensure proper scrolling
            minWidth = 280
        }
        
        // Add all views to main container
        mainContainer.addView(titleView)
        mainContainer.addView(videoInfoText)
        mainContainer.addView(videoThumbnail)
        mainContainer.addView(buttonsContainer)
        mainContainer.addView(progressContainer)
        mainContainer.addView(resultTextView)
        
        setContentView(mainContainer)
    }
    
    private fun setupVideoDetector() {
        try {
            videoDetector = SimpleVideoDetector(this)
            Log.d("BetterVideo", "✅ Video Detector initialized")
        } catch (e: Exception) {
            Log.e("BetterVideo", "❌ Failed to initialize Video Detector: ${e.message}")
            Toast.makeText(this, "Failed to initialize video detector", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 1001)
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions required for video analysis", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        videoPickerLauncher.launch(intent)
    }
    
    private fun showVideoInfo(uri: Uri) {
        try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(this, uri)
            
            val duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            val width = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            val height = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            
            retriever.release()
            
            val durationText = if (duration != null) "${duration / 1000}s" else "Unknown"
            val sizeText = if (width != null && height != null) "${width}x${height}" else "Unknown"
            
            videoInfoText.text = "📹 Video loaded successfully!\n📐 Size: $sizeText\n⏱️ Duration: $durationText"
            
        } catch (e: Exception) {
            videoInfoText.text = "📹 Video Info: Unable to read metadata"
            videoInfoText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        }
    }
    
    private fun loadVideoThumbnail(uri: Uri) {
        try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(this, uri)
            val bitmap = retriever.frameAtTime
            if (bitmap != null) {
                videoThumbnail.setImageBitmap(bitmap)
            } else {
                videoThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
            }
            retriever.release()
        } catch (e: Exception) {
            Log.e("BetterVideo", "Error loading video thumbnail: ${e.message}")
            videoThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }
    
    private fun analyzeVideo() {
        videoUri?.let { uri ->
            // Show progress
            val progressContainer = mainContainer.getChildAt(4) as LinearLayout
            progressContainer.visibility = View.VISIBLE
            
            progressBar.visibility = View.VISIBLE
            progressText.visibility = View.VISIBLE
            analyzeVideoButton.isEnabled = false
            selectVideoButton.isEnabled = false
            resultTextView.text = "🔍 Analyzing video with AI...\n\nPlease wait while our AI model processes your video frames"
            resultTextView.setTextColor(Color.parseColor("#1976D2"))
            resultCard.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
            progressText.text = "Starting analysis..."
            
            // Run analysis in background thread
            Thread {
                try {
                    Log.d("BetterVideo", "🎬 Starting video analysis")
                    
                    // Update progress
                    Handler(Looper.getMainLooper()).post {
                        progressText.text = "Extracting video frames..."
                        resultTextView.text = "🎬 Extracting video frames for AI analysis..."
                    }
                    
                    val result = videoDetector?.analyzeVideoSimple(uri)
                    
                    Log.d("BetterVideo", "🎬 Analysis completed, result: $result")
                    
                    // Update UI on main thread
                    Handler(Looper.getMainLooper()).post {
                        progressContainer.visibility = View.GONE
                        analyzeVideoButton.isEnabled = true
                        selectVideoButton.isEnabled = true
                        
                        if (result != null) {
                            displayVideoResult(result)
                        } else {
                            resultTextView.text = "❌ Video analysis failed - null result"
                            Toast.makeText(this, "Analysis failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BetterVideo", "❌ Error during video analysis: ${e.message}", e)
                    Handler(Looper.getMainLooper()).post {
                        progressContainer.visibility = View.GONE
                        analyzeVideoButton.isEnabled = true
                        selectVideoButton.isEnabled = true
                        resultTextView.text = "❌ Error: ${e.message}"
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        } ?: run {
            Toast.makeText(this, "Please select a video first", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun displayVideoResult(result: VideoAnalysisResult) {
        val isAI = result.isAI
        val confidence = (result.confidence * 100).toInt()
        
        val resultText = if (isAI) {
            "🤖 AI Generated Video Detected!\n\n" +
            "📊 Analysis Results:\n" +
            "• Confidence: $confidence%\n" +
            "• Frames Analyzed: ${result.frameCount}\n" +
            "• Duration: ${result.videoDuration / 1000}s\n" +
            "• AI Content: ${"%.1f".format(result.aiFramePercentage)}%\n" +
            "• Processing Time: ${result.processingTime}ms\n\n" +
            "📝 Details: ${result.details}\n\n" +
            "⚠️ This video appears to contain AI-generated content"
        } else {
            "✅ Authentic Video Detected!\n\n" +
            "📊 Analysis Results:\n" +
            "• Real Content: $confidence%\n" +
            "• Frames Analyzed: ${result.frameCount}\n" +
            "• Duration: ${result.videoDuration / 1000}s\n" +
            "• AI Content: ${"%.1f".format(result.aiFramePercentage)}%\n" +
            "• Processing Time: ${result.processingTime}ms\n\n" +
            "📝 Details: ${result.details}\n\n" +
            "👍 This video appears to be authentic and not AI-generated"
        }
        
        resultTextView.text = resultText
        resultTextView.textSize = 16f
        
        val color = if (isAI) {
            Color.parseColor("#D32F2F") // Red for AI
        } else {
            Color.parseColor("#388E3C") // Green for Real
        }
        resultTextView.setTextColor(color)
        
        // Update card background color based on result
        resultCard.setCardBackgroundColor(
            if (isAI) Color.parseColor("#FFEBEE") else Color.parseColor("#E8F5E8")
        )
        
                
        Log.d("BetterVideo", "🎬 Video analysis completed: ${if (result.isAI) "AI Generated" else "Real"} (${result.confidence}%)")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        videoDetector?.close()
    }
}
