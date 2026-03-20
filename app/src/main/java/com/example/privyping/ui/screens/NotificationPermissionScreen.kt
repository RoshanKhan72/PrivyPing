package com.example.privyping.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun NotificationPermissionScreen(
    isGranted: Boolean,
    onGrantClick: () -> Unit,
    onPermissionComplete: () -> Unit
) {
    var exit by remember { mutableStateOf(false) }

    // Permission granted → show success → animate → navigate
    LaunchedEffect(isGranted) {
        if (isGranted) {
            delay(1500)   // show "Permission enabled"
            exit = true
            delay(300)    // exit animation
            onPermissionComplete()
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (exit) 0f else 1f,
        animationSpec = tween(300),
        label = "permission_alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (exit) 0.96f else 1f,
        animationSpec = tween(300),
        label = "permission_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (isGranted) "Permission Enabled" else "Enable Notification Access",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isGranted)
                    "You're all set. Redirecting…"
                else
                    "PrivyPing needs notification access to protect you from scams.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!isGranted) {
                Button(
                    onClick = onGrantClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permission")
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}
