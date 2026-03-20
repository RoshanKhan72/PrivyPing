package com.example.privyping.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class TourPage(
    val title: String,
    val description: String
)

@Composable
fun TourScreen(
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    val pages = listOf(
        TourPage(
            "Analyze Notifications",
            "PrivyPing scans your notifications in real-time to detect scams, phishing, and misinformation. Stay ahead of misleading messages effortlessly."
        ),
        TourPage(
            "Privacy First",
            "Your data stays on your device. PrivyPing does not track your messages externally, ensuring complete privacy while providing smart analysis."
        ),
        TourPage(
            "Multimedia Insights",
            "Analyze images, videos, and links in your notifications. PrivyPing can detect suspicious content and misinformation across all media types."
        ),
        TourPage(
            "AI-Powered Accuracy",
            "PrivyPing leverages advanced AI to evaluate risks, spot misleading claims, and provide you with trustworthy alerts and summaries."
        ),
        TourPage(
            "Actionable Protection",
            "Understand potential threats clearly and act confidently. Copy, delete, or re-analyze suspicious content with a single tap for safer digital communication."
        )
    )


    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {

            /* ───── Top bar ───── */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkip) {
                    Text("Skip")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            /* ───── Pager content ───── */
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = pages[page].title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = pages[page].description,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                }
            }
            /* ───── Bottom controls ───── */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // ◀ Left arrow with background
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage - 1
                            )
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (pagerState.currentPage > 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Previous",
                        tint = Color.White
                    )
                }

                // ●●● Dots (clearly visible)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pages.size) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (selected) 10.dp else 8.dp)
                                .background(
                                    color = if (selected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // ▶ Right arrow with background
                IconButton(
                    onClick = {
                        if (pagerState.currentPage == pages.lastIndex) {
                            onFinish()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage + 1
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
