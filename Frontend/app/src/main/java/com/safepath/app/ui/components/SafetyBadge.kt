package com.safepath.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safepath.app.data.model.SafetyLevel
import com.safepath.app.data.model.SafetyScore
import com.safepath.app.ui.theme.*

/**
 * Displays a safety score badge with color coding, shield icon, and level label.
 */
@Composable
fun SafetyBadge(
    score: SafetyScore,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (bgColor, borderColor, textColor) = when (score.level) {
        SafetyLevel.SAFE     -> Triple(SafeGreen.copy(alpha = 0.15f),   SafeGreen,     SafeGreen)
        SafetyLevel.MODERATE -> Triple(ModerateAmber.copy(alpha = 0.15f), ModerateAmber, ModerateAmber)
        SafetyLevel.RISKY    -> Triple(RiskyRed.copy(alpha = 0.15f),    RiskyRed,      RiskyRed)
    }

    if (compact) {
        // Compact: just the score number in a pill
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector        = Icons.Default.Shield,
                    contentDescription = null,
                    tint               = textColor,
                    modifier           = Modifier.size(12.dp)
                )
                Text(
                    text       = "${score.score}",
                    color      = textColor,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        // Full badge: score circle + level text
        Row(
            modifier            = modifier,
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Score circle
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(bgColor, Color.Transparent)
                        )
                    )
                    .border(2.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "${score.score}",
                        color      = textColor,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 16.sp
                    )
                    Text(
                        text       = "/100",
                        color      = textColor.copy(alpha = 0.7f),
                        fontSize   = 8.sp,
                        lineHeight = 8.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector        = Icons.Default.Shield,
                        contentDescription = null,
                        tint               = textColor,
                        modifier           = Modifier.size(14.dp)
                    )
                    Text(
                        text       = score.level.label,
                        color      = textColor,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text     = "Safety Score",
                    color    = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
