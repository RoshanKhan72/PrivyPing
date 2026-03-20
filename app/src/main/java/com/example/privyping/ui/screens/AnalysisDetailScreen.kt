package com.example.privyping.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.privyping.ui.viewmodel.NotificationViewModel
import com.example.privyping.ui.analysis.RiskLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnalysisDetailScreen(
    notificationId: String,
    onBack: () -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val item = notifications.find { it.id == notificationId }
    val scrollState = rememberScrollState()

    var visible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        visible = true
    }

    val handleBack = {
        visible = false
        scope.launch {
            delay(300)
            onBack()
        }
    }

    BackHandler {
        handleBack()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(300)
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            if (item == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Notification not found")
                }
                return@AnimatedVisibility
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 24.dp, // Reduced top padding since back button is removed
                        bottom = 96.dp
                    )
                    .verticalScroll(scrollState)
            ) {

                Text(
                    text = item.appName,
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Text(
                    text = item.senderName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = item.message,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Risk Level",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = item.riskLevel?.name ?: "Not analyzed",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (item.riskLevel) {
                        RiskLevel.HIGH -> Color.Red
                        RiskLevel.MEDIUM -> Color(0xFFFFA000)
                        RiskLevel.LOW -> Color(0xFF2E7D32)
                        null -> Color.Gray
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Confidence: ${item.confidence ?: 0}%",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Explanation",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = item.summary ?: "No explanation available",
                    lineHeight = 18.sp
                )
            }

            Button(
                onClick = { handleBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5C5791),
                    contentColor = Color.White
                )
            ) {
                Text("Back")
            }
        }
    }
}
