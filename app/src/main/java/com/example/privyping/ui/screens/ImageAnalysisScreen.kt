package com.example.privyping.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.privyping.ui.media_analysis.GeminiImageAnalyzer
import com.example.privyping.ui.media_analysis.HFImageAnalyzer
import com.example.privyping.ui.media_analysis.ImageAnalysisResult
import com.example.privyping.ui.media_analysis.hive.HiveImageAnalyzer

@Composable
fun ImageAnalysisScreen() {

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var analysisResult by remember { mutableStateOf<ImageAnalysisResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Android Photo Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
        analysisResult = null
        errorMessage = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Title
        Text(
            text = "Image Analysis",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Privacy disclaimer
        Text(
            text = "Selected images are analyzed using a cloud AI model. Images are not stored.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        // Select Image Button
        Button(
            onClick = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Image")
        }

        // Image Preview
        selectedImageUri?.let { uri ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Button(
                onClick = {
                    isAnalyzing = true
                    analysisResult = null
                    errorMessage = null

                    HiveImageAnalyzer.analyze(
                        context = context,
                        imageUri = selectedImageUri!!,
                        onResult = { result ->
                            analysisResult = result
                        },
                        onError = { error ->
                            errorMessage = error
                        }
                    )


                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAnalyzing
            ) {
                Text(if (isAnalyzing) "Analyzing..." else "Analyze Image")
            }
        }

        // Divider
        HorizontalDivider()

        // Error Message
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontWeight = FontWeight.Medium
            )
        }

        // Analysis Result
        analysisResult?.let { result ->
            Text(
                text = "Analysis Result",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = if (result.isAiGenerated)
                    "Verdict: AI-Generated Image"
                else
                    "Verdict: Real Image",
                color = if (result.isAiGenerated) Color.Red else Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold
            )

            Text("Confidence: ${result.confidence}%")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Summary",
                fontWeight = FontWeight.SemiBold
            )
            Text(result.summary)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Key Patterns",
                fontWeight = FontWeight.SemiBold
            )
            result.keyPatterns.forEach {
                Text("• $it")
            }
        }

        if (analysisResult == null && errorMessage == null && selectedImageUri != null && !isAnalyzing) {
            Text(
                text = "No analysis performed yet.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
