package com.safepath.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safepath.app.data.model.Route
import com.safepath.app.data.model.SafetyLevel
import com.safepath.app.ui.theme.*

/**
 * Card displaying a route with safety score, duration, distance, and highlights.
 * Animates its border/background when selected.
 */
@Composable
fun RouteCard(
    route: Route,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safetyColor = when (route.safetyScore.level) {
        SafetyLevel.SAFE     -> SafeGreen
        SafetyLevel.MODERATE -> ModerateAmber
        SafetyLevel.RISKY    -> RiskyRed
    }

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PurplePrimary else safetyColor.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "border_anim"
    )
    val bgAlpha by animateColorAsState(
        targetValue = if (isSelected) SurfaceVariant else SurfaceDark,
        animationSpec = tween(300),
        label = "bg_anim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgAlpha)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
    ) {
        // Recommended glow gradient at top
        if (route.isRecommended) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(listOf(PurplePrimary, PinkAccent))
                    )
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header row ──────────────────────────────────────────────────
            Row(
                modifier       = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text       = route.name,
                            color      = TextPrimary,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (route.isRecommended) {
                            RecommendedBadge()
                        }
                    }
                }
                SafetyBadge(score = route.safetyScore, compact = true)
            }

            Spacer(Modifier.height(12.dp))

            // ── Info row (distance / time) ───────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                InfoChip(icon = Icons.Default.Route, label = route.distance)
                InfoChip(icon = Icons.Default.Timer, label = route.duration)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(12.dp))

            // ── Safety reasons ───────────────────────────────────────────────
            Text(
                text     = "Safety highlights",
                color    = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                route.safetyScore.reasons.take(3).forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint               = safetyColor,
                            modifier           = Modifier.size(13.dp)
                        )
                        Text(
                            text     = reason,
                            color    = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ── Selected indicator ───────────────────────────────────────────
            if (isSelected) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Selected",
                        color = PurplePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendedBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.horizontalGradient(listOf(PurplePrimary, PinkAccent))
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text       = "★ Best",
            color      = Color.White,
            fontSize   = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = PurpleLight,
            modifier           = Modifier.size(14.dp)
        )
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
    }
}
