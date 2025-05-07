package kr.evangers.rapidthemore.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3F51B5),
    onPrimary = Color.White,
    secondary = Color(0xFF03A9F4),
    onSecondary = Color.White,
    tertiary = Color(0xFF4CAF50),
    background = Color.White,
    onBackground = Color(0xFF121212),
    surface = Color.White,
    onSurface = Color(0xFF121212)
)

@Composable
fun RapidTheMoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 항상 라이트 테마 사용 (다크 테마 강제 비활성화)
    val colorScheme = LightColorScheme

    // 상태바와 네비게이션바 색상 설정
    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController, colorScheme) {
        systemUiController.setSystemBarsColor(
            color = colorScheme.primary,
            darkIcons = false
        )
        onDispose {}
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
} 