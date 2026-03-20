package com.example.privyping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.*
import com.example.privyping.ui.navigation.AppNavGraph
import com.example.privyping.ui.navigation.NavRoutes
import com.example.privyping.ui.theme.PrivyPingTheme
import com.example.privyping.ui.utils.isNotificationAccessGranted
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        val splashScreen = installSplashScreen()
        val startTime = System.currentTimeMillis()
        splashScreen.setKeepOnScreenCondition {
            System.currentTimeMillis() - startTime < 3000
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("privyping_prefs", MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean("onboarding_done", false)
        val permissionGranted = isNotificationAccessGranted(this)

        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkTheme by remember { 
                mutableStateOf(prefs.getBoolean("dark_theme", systemDark)) 
            }

            PrivyPingTheme(
                darkTheme = darkTheme,
                onThemeChange = { isDark ->
                    darkTheme = isDark
                    prefs.edit().putBoolean("dark_theme", isDark).apply()
                }
            ) {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    when {
                        !onboardingDone -> {
                            navController.navigate(NavRoutes.WELCOME) {
                                popUpTo(0)
                            }
                        }
                        !permissionGranted -> {
                            navController.navigate(NavRoutes.NOTIFICATION) {
                                popUpTo(0)
                            }
                        }
                        else -> {
                            navController.navigate(NavRoutes.HOME) {
                                popUpTo(0)
                            }
                        }
                    }
                }

                AppNavGraph(navController = navController)
            }
        }
    }
}
