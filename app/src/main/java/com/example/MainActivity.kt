package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.NavigationDestination

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request POST_NOTIFICATIONS runtime permission on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val requestPermissionLauncher = registerForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                // Status logged or handled
            }
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Edge to edge rendering support for dynamic taskbar spacing
        enableEdgeToEdge()

        setContent {
            // Respect the user's Dark Mode override preference immediately
            val isDarkThemeActive by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkThemeActive) {
                var showSplash by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreenContent()
                } else {
                    val destination by viewModel.currentDestination.collectAsState()
                    val isSignUpActive = viewModel.isSignUpActive.value

                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        // Animated Content entry when transitioning between high-level states
                        AnimatedContent(
                            targetState = destination,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "MainFlowTransitions",
                            modifier = Modifier.fillMaxSize()
                        ) { targetDest ->
                            when (targetDest) {
                                is NavigationDestination.AuthLogin,
                                is NavigationDestination.AuthSignUp,
                                is NavigationDestination.AuthForgot -> {
                                    if (!isSignUpActive) {
                                        LoginScreen(viewModel = viewModel)
                                    } else {
                                        SignUpScreen(
                                            viewModel = viewModel,
                                            onBack = { viewModel.isSignUpActive.value = false }
                                        )
                                    }
                                }
                                is NavigationDestination.Onboarding -> {
                                    OnboardingScreen(viewModel = viewModel)
                                }
                                is NavigationDestination.MainFlow -> {
                                    MainScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreenContent() {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF21005D),
                        Color(0xFF15003B),
                        Color(0xFF1C1B1F)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha
            )
        ) {
            // Stylized Logo Icon: P inside chat bubble representation
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        color = Color(0xFFD0BCFF),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "P",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1C1B1F)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PayStory",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Every payment has a story.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFCAC4D0),
                textAlign = TextAlign.Center
            )
        }
    }
}

