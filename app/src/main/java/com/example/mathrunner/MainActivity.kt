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
import com.example.mathrunner.ui.screens.GameplayScreen
import com.example.mathrunner.ui.screens.HomeScreen
import com.example.mathrunner.ui.screens.LevelsScreen
import com.example.mathrunner.ui.screens.SplashScreen
import com.example.mathrunner.ui.theme.MathRunnerTheme

private enum class AppScreen {
    Splash,
    Home,
    Levels,
    Game
}

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
    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }

    Crossfade(
        targetState = currentScreen,
        animationSpec = tween(durationMillis = 400),
        label = "AppScreenCrossfade",
        modifier = Modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            AppScreen.Splash -> SplashScreen(
                onSplashScreenFinished = {
                    currentScreen = AppScreen.Home
                },
                autoNavigate = true
            )

            AppScreen.Home -> HomeScreen(
                onBackToSplash = {
                    currentScreen = AppScreen.Splash
                },
                onStartGame = {
                    currentScreen = AppScreen.Game
                }
            )

            AppScreen.Levels -> LevelsScreen(
                onBackToHome = {
                    currentScreen = AppScreen.Home
                },
                onEasySelected = {
                    currentScreen = AppScreen.Game
                }
            )

            AppScreen.Game -> GameplayScreen(
                onExitGame = {
                    currentScreen = AppScreen.Home
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LevelsScreenPreview() {
    MathRunnerTheme {
        LevelsScreen(onBackToHome = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GameplayScreenPreview() {
    MathRunnerTheme {
        GameplayScreen(onExitGame = {})
    }
}