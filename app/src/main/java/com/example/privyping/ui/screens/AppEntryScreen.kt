package com.example.privyping.ui.screens

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.privyping.R
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.privyping.ui.utils.isNotificationAccessGranted


@Composable
fun AppEntryScreen(
    navController: NavController
) {
    val context = LocalContext.current

    val scale = remember { Animatable(0.9f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {

        // Fade + scale in
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700)
        )

        // ⏸ Stay visible for 3 seconds
        delay(3000)

        val prefs = context.getSharedPreferences(
            "privyping_prefs",
            Context.MODE_PRIVATE
        )

        val onboardingDone =
            prefs.getBoolean("onboarding_done", false)

        val permissionGranted =
            isNotificationAccessGranted(context)

        when {
            !onboardingDone -> {
                navController.navigate("welcome") {
                    popUpTo("entry") { inclusive = true }
                }
            }

            !permissionGranted -> {
                navController.navigate("notification") {
                    popUpTo("entry") { inclusive = true }
                }
            }

            else -> {
                navController.navigate("home") {
                    popUpTo("entry") { inclusive = true }
                }
            }
        }
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(230.dp) // slightly bigger
                .scale(scale.value)
                .alpha(alpha.value)
                .clip(RoundedCornerShape(56.dp)) // very rounded
                .background(Color.White)
                .border(
                    width = 2.dp,
                    color = Color.White, // ✅ white border
                    shape = RoundedCornerShape(56.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.privyping_icon),
                contentDescription = "PrivyPing Logo",
                modifier = Modifier.size(170.dp) // ✅ bigger logo
            )
        }
    }


}

