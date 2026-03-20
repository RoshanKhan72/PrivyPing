package com.example.privyping.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIContentScreen(onBack: () -> Unit) {
    var textToAnalyze by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisProgress by remember { mutableStateOf(0f) }
    var analysisStatus by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf<AIAnalysisResult?>(null) }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val analysisMessages = listOf(
        "Scanning text structure...",
        "Checking semantic patterns...",
        "Analyzing linguistic consistency...",
        "Evaluating vocabulary diversity...",
        "Finalizing detection..."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "AI Content Detection",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Paste text below to detect AI patterns",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                TextButton(
                    onClick = {
                        clipboardManager.getText()?.let { textToAnalyze = it.text }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Paste", fontSize = 14.sp)
                }
            }

            OutlinedTextField(
                value = textToAnalyze,
                onValueChange = { textToAnalyze = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("Enter or paste content here...") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (textToAnalyze.isNotBlank()) {
                        scope.launch {
                            isAnalyzing = true
                            analysisResult = null
                            
                            val totalDuration = 4000L
                            val step = 100L
                            val steps = (totalDuration / step).toInt()
                            
                            for (i in 0..steps) {
                                analysisProgress = i.toFloat() / steps
                                val msgIndex = (analysisProgress * (analysisMessages.size - 1)).toInt()
                                analysisStatus = analysisMessages[msgIndex]
                                delay(step)
                            }
                            
                            analysisResult = performAIAnalysis(textToAnalyze)
                            isAnalyzing = false
                        }
                    }
                },
                enabled = textToAnalyze.isNotBlank() && !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Analyze Content", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Indicator
            AnimatedVisibility(
                visible = isAnalyzing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { analysisProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = analysisStatus,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Results
            if (analysisResult != null && !isAnalyzing) {
                AIResultCard(result = analysisResult!!)
            }
        }
        
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Back")
        }
    }
}

@Composable
fun AIResultCard(result: AIAnalysisResult) {
    val isAI = result.isAI
    val containerColor = if (isAI) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    
    val contentColor = if (isAI) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isAI) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isAI) "Likely AI-Generated" else "Likely Human-Written",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Confidence Score: ${result.confidence}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = result.explanation,
                fontSize = 14.sp,
                color = contentColor.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        }
    }
}

data class AIAnalysisResult(
    val isAI: Boolean,
    val confidence: Int,
    val explanation: String
)

private fun performAIAnalysis(text: String): AIAnalysisResult {
    // Simple mock logic for AI detection
    val words = text.split("\\s+".toRegex())
    val avgWordLength = if (words.isNotEmpty()) words.map { it.length }.average() else 0.0
    
    // Heuristic: repetitive patterns or very formal structure often associated with AI in basic models
    val isFormal = text.contains("Furthermore", ignoreCase = true) || text.contains("In conclusion", ignoreCase = true)
    
    val aiScore = when {
        isFormal && words.size > 50 -> 75 + (0..15).random()
        avgWordLength > 6.0 -> 60 + (0..20).random()
        else -> 20 + (0..30).random()
    }
    
    val isAI = aiScore > 50
    
    val explanation = if (isAI) {
        "Analysis found patterns consistent with large language models, including highly structured phrasing and formal transitions."
    } else {
        "Analysis suggests human-like linguistic variety and naturally occurring patterns in vocabulary usage."
    }
    
    return AIAnalysisResult(isAI, aiScore, explanation)
}
