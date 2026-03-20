package com.example.privyping.ui.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.privyping.ui.screens.*
import com.example.privyping.ui.utils.isNotificationAccessGranted

const val ANALYSIS_DETAIL = "analysis_detail"

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {

        composable(NavRoutes.ENTRY) {
            AppEntryScreen(navController)
        }

        composable(NavRoutes.WELCOME) {
            WelcomeScreen(
                onContinueClick = {
                    navController.navigate(NavRoutes.TOUR) {
                        popUpTo(NavRoutes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.TOUR) {
            TourScreen(
                onSkip = {
                    navController.navigate(NavRoutes.NOTIFICATION) {
                        popUpTo(NavRoutes.TOUR) { inclusive = true }
                    }
                },
                onFinish = {
                    navController.navigate(NavRoutes.NOTIFICATION) {
                        popUpTo(NavRoutes.TOUR) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.NOTIFICATION) {

            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            var isGranted by remember { mutableStateOf(false) }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        isGranted = isNotificationAccessGranted(context)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            NotificationPermissionScreen(
                isGranted = isGranted,
                onGrantClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    )
                },
                onPermissionComplete = {

                    context.getSharedPreferences(
                        "privyping_prefs",
                        Context.MODE_PRIVATE
                    ).edit {
                        putBoolean("onboarding_done", true)
                    }

                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.NOTIFICATION) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.HOME) {
            val context = LocalContext.current
            HomeScreen(
                onAnalyzeNotification = {
                    navController.navigate(NavRoutes.ANALYZE_NOTIFICATION)
                },
                onAnalyzeMultimedia = {
<<<<<<< HEAD
                    val intent = Intent(context, MultimediaActivity::class.java)
                    context.startActivity(intent)
                },
                onAnalyzeVideo = {
                    val intent = Intent(context, BetterVideoActivity::class.java)
                    context.startActivity(intent)
=======
                    navController.navigate(NavRoutes.ANALYZE_MULTIMEDIA)
                },
                onAIContentDetection = {
                    navController.navigate(NavRoutes.AI_CONTENT)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onNavigateToDevelopers = {
                    navController.navigate(NavRoutes.DEVELOPERS)
                },
                onNavigateToSupport = {
                    navController.navigate(NavRoutes.SUPPORT)
>>>>>>> upstream/PrivyPing-Version-v1.1.3
                }
            )
        }

        composable(NavRoutes.ANALYZE_NOTIFICATION) {
            AnalyzeNotificationScreen(
                onOpenAnalysis = { id ->
                    val safeId = Uri.encode(id)
                    navController.navigate("$ANALYSIS_DETAIL/$safeId")
                }
            )
        }
        
        composable(NavRoutes.ANALYZE_MULTIMEDIA) {
            AnalyzeMultimediaScreen()
        }

        composable(NavRoutes.IMAGE_ANALYSIS) {
            ImageAnalysisScreen()
        }

        composable(NavRoutes.AI_CONTENT) {
            AIContentScreen(onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.API_KEY) {
            ApiKeyScreen {
                navController.popBackStack()
            }
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.DEVELOPERS) {
            DeveloperScreen(onBack = { navController.popBackStack() })
        }

        composable(NavRoutes.SUPPORT) {
            SupportScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "$ANALYSIS_DETAIL/{notificationId}"
        ) { backStackEntry ->

            val notificationId =
                backStackEntry.arguments?.getString("notificationId")
                    ?: return@composable

            AnalysisDetailScreen(
                notificationId = notificationId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
