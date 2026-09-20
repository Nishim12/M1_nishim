package com.example.cpen321application

import com.example.cpen321application.ui.theme.AppButtonShape
import androidx.activity.compose.BackHandler
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cpen321application.ui.button1.Button1Screen
import com.example.cpen321application.ui.button2.Button2Screen
import com.example.cpen321application.ui.button3.Button3Screen
import com.example.cpen321application.ui.theme.CPEN321ApplicationTheme

private enum class Screen { HOME, BUTTON1, BUTTON2, BUTTON3 }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CPEN321ApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppRoot(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun AppRoot(modifier: Modifier = Modifier) {
    var screen by remember { mutableStateOf(Screen.HOME) }

    BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

    when (screen) {
        Screen.HOME -> HomeScreen(
            onButton1Click = { screen = Screen.BUTTON1 },
            onButton2Click = { screen = Screen.BUTTON2 },
            onButton3Click = { screen = Screen.BUTTON3 },
            modifier = modifier
        )
        Screen.BUTTON1 -> Button1Screen(
            apiBaseUrl = BuildConfig.API_BASE_URL,
            googleClientId = BuildConfig.GOOGLE_CLIENT_ID,
            modifier = modifier
        )
        Screen.BUTTON2 -> Button2Screen(
            apiBaseUrl = BuildConfig.API_BASE_URL,
            modifier = modifier
        )
        Screen.BUTTON3 -> Button3Screen(
            modifier = modifier
        )
    }
}

@Composable
private fun HomeScreen(
    onButton1Click: () -> Unit,
    onButton2Click: () -> Unit,
    onButton3Click: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HomeButton("Login + Server", onButton1Click)
        HomeButton("Live Updates", onButton2Click)
        HomeButton("Timer", onButton3Click)
    }
}

@Composable
private fun HomeButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = AppButtonShape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
    ) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}
