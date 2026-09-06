package com.example.mathrunner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mathrunner.ui.screens.HomeScreen
import com.example.mathrunner.ui.screens.SplashScreen
import com.example.mathrunner.ui.theme.MathRunnerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MathRunnerTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    var isSplashScreenVisible by remember { mutableStateOf(true) }

    Crossfade(
        targetState = isSplashScreenVisible,
        animationSpec = tween(durationMillis = 500),
        label = "SplashToHomeCrossfade",
        modifier = Modifier.fillMaxSize()
    ) { showSplash ->
        if (showSplash) {
            SplashScreen(
                onSplashScreenFinished = {
                    isSplashScreenVisible = false
                },
                autoNavigate = true // Auto navigate after 6 seconds loading duration
            )
        } else {
            HomeScreen(
                onBackToSplash = {
                    isSplashScreenVisible = true
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    MathRunnerTheme {
        SplashScreen(onSplashScreenFinished = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MathRunnerTheme {
        HomeScreen()
    }
}