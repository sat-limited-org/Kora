package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class KoraChatColors(
    val bubbleSent: Color,
    val bubbleSentText: Color,
    val bubbleReceived: Color,
    val bubbleReceivedText: Color,
    val chatBackground: Color,
    val timeSentText: Color,
    val timeReceivedText: Color
)

val LocalKoraChatColors = staticCompositionLocalOf {
    KoraChatColors(
        bubbleSent = BubbleSentLight,
        bubbleSentText = BubbleSentTextLight,
        bubbleReceived = BubbleReceivedLight,
        bubbleReceivedText = BubbleReceivedTextLight,
        chatBackground = BackgroundLight,
        timeSentText = Color(0xFF4B6B58),
        timeReceivedText = Color(0xFF6B7280)
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = KoraGreenPrimaryDark,
    onPrimary = Color(0xFF003822),
    primaryContainer = KoraGreenContainerDark,
    onPrimaryContainer = Color(0xFFD4F4E4),
    secondary = KoraGreenAccent,
    onSecondary = Color(0xFF003822),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerDark
)

private val LightColorScheme = lightColorScheme(
    primary = KoraGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = KoraGreenContainer,
    onPrimaryContainer = Color(0xFF063B25),
    secondary = KoraGreenAccent,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerLight
)

@Composable
fun KoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve branded emerald Kora aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val chatColors = if (darkTheme) {
        KoraChatColors(
            bubbleSent = BubbleSentDark,
            bubbleSentText = BubbleSentTextDark,
            bubbleReceived = BubbleReceivedDark,
            bubbleReceivedText = BubbleReceivedTextDark,
            chatBackground = BackgroundDark,
            timeSentText = Color(0xFF7CA692),
            timeReceivedText = Color(0xFF8E9BAE)
        )
    } else {
        KoraChatColors(
            bubbleSent = BubbleSentLight,
            bubbleSentText = BubbleSentTextLight,
            bubbleReceived = BubbleReceivedLight,
            bubbleReceivedText = BubbleReceivedTextLight,
            chatBackground = Color(0xFFEBEFEA),
            timeSentText = Color(0xFF3B6E52),
            timeReceivedText = Color(0xFF64748B)
        )
    }

    CompositionLocalProvider(LocalKoraChatColors provides chatColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
