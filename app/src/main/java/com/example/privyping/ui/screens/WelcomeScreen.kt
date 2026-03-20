package com.example.privyping.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privyping.R
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onContinueClick: () -> Unit
) {
    var showPrivy by remember { mutableStateOf(false) }
    var showPing by remember { mutableStateOf(false) }
    var exit by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showPrivy = true
        delay(400)
        showPing = true
    }
    LaunchedEffect(exit) {
        if (exit) {
            delay(300) // wait for exit animation
            onContinueClick()
        }
    }


    val alpha by animateFloatAsState(
        targetValue = if (exit) 0f else 1f,
        animationSpec = tween(300)
    )

    val scale by animateFloatAsState(
        targetValue = if (exit) 0.96f else 1f,
        animationSpec = tween(300)
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

            Image(
                painter = painterResource(id = R.drawable.privyping_icon),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                AnimatedVisibility(
                    visible = showPrivy,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600)
                    )
                ) {
                    Text(
                        text = "Privy",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                AnimatedVisibility(
                    visible = showPing,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600)
                    )
                ) {
                    Text(
                        text = "Ping",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Protecting you from misinformation",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { exit = true }, // ✅ ONLY STATE CHANGE
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = "Get Started")
            }
        }
    }
}
