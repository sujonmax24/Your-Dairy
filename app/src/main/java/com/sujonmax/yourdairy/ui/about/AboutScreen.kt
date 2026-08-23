package com.sujonmax.yourdairy.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private const val FACEBOOK_URL = "https://facebook.com/sujon.ssm"
private const val LINKEDIN_URL = "https://www.linkedin.com/in/sujon-mia-95924221a"

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Dream Diry") },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Dream Diry", style = MaterialTheme.typography.headlineMedium)
            Text(
                "A private and friendly space for writing your thoughts, memories and everyday moments.",
                style = MaterialTheme.typography.bodyLarge
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Text("About the app", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Dream Diry is designed to make personal journaling simple, organized and privacy-focused. You can keep diaries, organize notes, and use security features to protect personal memories.")
                    Spacer(Modifier.height(8.dp))
                    Text("Developer: sujonmax")
                    Text("Version: 1.0")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null)
                        Text("Privacy & security", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Dream Diry is designed with privacy in mind. Use the app lock and biometric authentication features to help protect your personal diary.")
                }
            }

            Text("Connect with the developer", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = { openUrl(FACEBOOK_URL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Facebook, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Facebook — sujon.ssm")
            }

            OutlinedButton(
                onClick = { openUrl(LINKEDIN_URL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Link, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("LinkedIn — Sujon Mia")
            }

            Text(
                "Dream Diry • create by sujonmax",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
