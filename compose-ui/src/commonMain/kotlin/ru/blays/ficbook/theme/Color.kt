package ru.blays.ficbook.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import ru.blays.ficbook.reader.shared.data.dto.FanficCompletionStatus
import ru.blays.ficbook.reader.shared.data.dto.FanficDirection

val trophyColor = Color(0xFFA48D19)
val likeColor = Color(0xFF4BAF3B)
val lockColor = Color(0xFF9C111C)
val unlockColor = likeColor

val flameColorEnd = Color(0xFFE28522)
val flameColorStart = Color(0xFFE2B822)
val flameGradient = Brush.verticalGradient(
    listOf(
        flameColorEnd,
        flameColorStart
    )
)

val lightGreen = Color(0xff63a75a)

// Fanfic direction colors
val genColor = Color(0xFF986a43)
val hetColor = Color(0xFF1E7A20)
val slashColor = Color(0xFF1A74BD)
val femslashColor = Color(0xFFB82859)
val articleColor = Color(0xFF525252)
val mixedColor = Color(0xFFC49930)
val otherColor = Color(0xFF08917C)

// Fanfic status colors
val inProgressColor = Color(0xFFCEB952)
val completeColor = likeColor
val frozenColor = Color(0xFF1DAEB9)

// Rainbow colors for gradient
val rainbowColors = arrayOf(
    Color.Red,
    Color.Magenta,
    Color.Blue,
    Color.Cyan,
    Color.Green,
    Color.Yellow,
    Color.Red
)

// Default primary colors
val defaultAccentColors = arrayOf(
    Color(0xFFE23939),
    Color(0xFF5D7ED3),
    Color(0xFFE0277E),
    Color(0xFF00B4A3),
    Color(0xFFE76840),
    Color(0xFFD6B348),
    Color(0xFF724BB8),
    Color(0xFF4DA551),
)

val FanficDirection.color: Color
    get() = when (this) {
        FanficDirection.GEN -> genColor
        FanficDirection.HET -> hetColor
        FanficDirection.SLASH -> slashColor
        FanficDirection.FEMSLASH -> femslashColor
        FanficDirection.ARTICLE -> articleColor
        FanficDirection.MIXED -> mixedColor
        FanficDirection.OTHER -> otherColor
        FanficDirection.UNKNOWN -> Color.Unspecified
    }

val FanficCompletionStatus.color: Color
    get() = when (this) {
        FanficCompletionStatus.IN_PROGRESS -> inProgressColor
        FanficCompletionStatus.COMPLETE -> completeColor
        FanficCompletionStatus.FROZEN -> frozenColor
        FanficCompletionStatus.UNKNOWN -> Color.Unspecified
    }
