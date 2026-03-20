package com.example.privyping.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privyping.R
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext

data class Developer(
    val name: String,
    val role: String,
    val imageRes: Int,
    val linkedinUrl: String
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(onBack: () -> Unit) {
    // TIP: Replace R.drawable.logo_privyping with your actual dev image names
    // For example: R.drawable.dev_yashwanth
    val developers = listOf(
        Developer(
            "Yashwanth V S Devang",
            "Lead Android Developer",
            R.drawable.dev1,
            "https://www.linkedin.com/in/yashwanthvsdevang/"
        ),
        Developer(
            "Yashaswini D S",
            "Model Trainer",
            R.drawable.dev2,
            "https://www.linkedin.com/in/yashaswini-ds-aa3966353/"
        ),
        Developer(
            "Roshan Khan Soudagar",
            "Android Developer",
            R.drawable.dev3,
            "https://www.linkedin.com/in/mohammad-roshan-soudagar/"
        ),
        Developer(
            "Prajwal K N",
            "UI/UX Designer",
            R.drawable.dev4,
            "https://www.linkedin.com/in/prajwal-k-npkn/"
        )
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Our Developers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "The team behind PrivyPing working to keep your digital life secure.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(developers) { developer ->
                    DeveloperCard(developer)
                }
            }
        }
    }
}

@Composable
fun DeveloperCard(developer: Developer) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = developer.imageRes),
                contentDescription = developer.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = developer.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = developer.role,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // LinkedIn Icon
            Icon(
                painter = painterResource(id = R.drawable.ic_linkedin),
                contentDescription = "LinkedIn Profile",
                tint = Color(0xFF0A66C2),
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(developer.linkedinUrl)
                        )
                        context.startActivity(intent)
                    }
            )
        }
    }
}
