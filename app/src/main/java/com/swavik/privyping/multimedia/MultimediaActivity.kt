package com.swavik.privyping.multimedia

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.privyping.R
import java.text.SimpleDateFormat
import java.util.*

class MultimediaActivity : AppCompatActivity() {
    
    private lateinit var selectButton: Button
    private lateinit var analyzeButton: Button
    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    
    private var selectedImageUri: Uri? = null
    private var aiDetector: AIDetector? = null
    
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            try {
                val bitmap = contentResolver.openInputStream(it)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
                bitmap?.let { bmp ->
                    imageView.setImageBitmap(bmp)
                    resultTextView.text = "Image loaded: ${bmp.width}x${bmp.height}\n\nTap Analyze to check if AI generated"
                }
            } catch (e: Exception) {
                Log.e("MultimediaActivity", "Error loading image: ${e.message}")
                resultTextView.text = "Error loading image: ${e.message}"
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Set theme before setContentView
            setTheme(R.style.Theme_PrivyPing)
            
            // Initialize AI Detector
            aiDetector = AIDetector(this)
            
            selectButton = Button(this).apply {
                text = "📷 Select Image"
                setOnClickListener { checkPermissionAndPickImage() }
            }
            
            analyzeButton = Button(this).apply {
                text = "🔍 Analyze Image"
                setOnClickListener { analyzeImageWithAI() }
            }
            
            imageView = ImageView(this).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            
            resultTextView = TextView(this).apply {
                text = "Welcome to AI Image Detection\n\nSelect an image to analyze if it's AI generated or real"
                textSize = 16f
            }
            
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 80, 32, 32)
            }
            
            layout.addView(selectButton)
            val selectButtonParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            selectButtonParams.setMargins(0, 20, 0, 10)
            selectButton.layoutParams = selectButtonParams
            
            layout.addView(analyzeButton)
            val analyzeButtonParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            analyzeButtonParams.setMargins(0, 0, 0, 20)
            analyzeButton.layoutParams = analyzeButtonParams
            
            layout.addView(imageView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            ))
            layout.addView(resultTextView)
            
            setContentView(layout)
            
            Log.d("MultimediaActivity", "✅ Activity created successfully with AI Detector")
            
        } catch (e: Exception) {
            Log.e("MultimediaActivity", "❌ Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error initializing activity", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun checkPermissionAndPickImage() {
        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                        pickImageLauncher.launch("image/*")
                    } else {
                        requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 100)
                    }
                }
                else -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        pickImageLauncher.launch("image/*")
                    } else {
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MultimediaActivity", "Error checking permissions: ${e.message}")
        }
    }
    
    private fun analyzeImageWithAI() {
        try {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
                return
            }
            
            resultTextView.text = "Analyzing image with AI..."
            
            Thread {
                try {
                    val bitmap = contentResolver.openInputStream(selectedImageUri!!)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                    
                    bitmap?.let { bmp ->
                        // Use AI Detector for real analysis
                        val result = aiDetector?.analyzeImage(bmp)
                        
                        runOnUiThread {
                            try {
                                result?.let { detectionResult ->
                                    resultTextView.text = detectionResult.getResultText()
                                    resultTextView.textSize = 14f
                                    
                                    val color = if (detectionResult.isAI) {
                                        getColor(R.color.ai_detected)
                                    } else {
                                        getColor(R.color.real_detected)
                                    }
                                    resultTextView.setTextColor(color)
                                    
                                    Log.d("MultimediaActivity", "✅ AI Analysis completed: ${detectionResult.details}")
                                    
                                } ?: run {
                                    resultTextView.text = "AI analysis failed - using fallback"
                                }
                            } catch (e: Exception) {
                                Log.e("MultimediaActivity", "Error updating UI: ${e.message}")
                                resultTextView.text = "Error displaying results"
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MultimediaActivity", "Error in AI analysis thread: ${e.message}")
                    runOnUiThread {
                        resultTextView.text = "AI analysis failed: ${e.message}"
                    }
                }
            }.start()
            
        } catch (e: Exception) {
            Log.e("MultimediaActivity", "Error starting AI analysis: ${e.message}")
            resultTextView.text = "Error starting AI analysis"
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("MultimediaActivity", "Error handling permissions: ${e.message}")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        aiDetector?.close()
        Log.d("MultimediaActivity", "🧹 MultimediaActivity destroyed")
    }
}
