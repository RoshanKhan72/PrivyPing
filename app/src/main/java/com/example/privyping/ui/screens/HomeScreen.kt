package com.example.privyping.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privyping.R
import com.example.privyping.ui.theme.LocalThemeManager
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onAnalyzeNotification: () -> Unit,
    onAnalyzeMultimedia: () -> Unit,
onAIContentDetection: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDevelopers: () -> Unit,
    onNavigateToSupport: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val themeManager = LocalThemeManager.current

    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }

    val contentOffset by animateDpAsState(
        targetValue = if (showContent) 0.dp else 24.dp,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "content_offset"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(600),
        label = "content_alpha"
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            Color.Transparent
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .offset(y = contentOffset)
                    .graphicsLayer { alpha = contentAlpha }
            ) {

                Spacer(modifier = Modifier.height(40.dp))

                Image(
                    painter = painterResource(id = R.drawable.logo_privyping),
                    contentDescription = "PrivyPing Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .offset(x = (-32).dp)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.Start)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {

                    Spacer(modifier = Modifier.height((-155).dp))

                    Text(
                        text = "PrivyPing",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Analyze multimedia and messages using intelligent systems. Privacy-focused detection at your fingertips.",
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = onAnalyzeNotification,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Analyze Notifications",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onAnalyzeMultimedia,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Analyze Multimedia",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onAIContentDetection,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "AI Content Detection",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Results may not always be accurate. Please verify independently.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Version v1.1.0",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 18.dp)
                    )
                }
            }

            // Top-right Menu
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
<<<<<<< HEAD
                Text("Analyze Multimedia (Images)")
            }

            // SMALL gap between buttons
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onAnalyzeVideo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text("🎬 Analyze Video (AI Detection)")
=======
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (themeManager.darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(if (themeManager.darkTheme) "Night Mode" else "Day Mode")
                            }
                        },
                        trailingIcon = {
                            Switch(
                                checked = themeManager.darkTheme,
                                onCheckedChange = { themeManager.onThemeChange(it) },
                                thumbContent = {
                                    Icon(
                                        imageVector = if (themeManager.darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        },
                        onClick = { themeManager.onThemeChange(!themeManager.darkTheme) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    DropdownMenuItem(
                        text = { Text("Settings") },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onNavigateToSettings()
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Know About Developers") },
                        leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onNavigateToDevelopers()
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Contact Support") },
                        leadingIcon = { Icon(Icons.Default.SupportAgent, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onNavigateToSupport()
                        }
                    )
                }
>>>>>>> upstream/PrivyPing-Version-v1.1.3
            }
        }
    }
}
