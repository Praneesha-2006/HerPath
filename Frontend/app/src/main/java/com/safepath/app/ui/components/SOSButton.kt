package com.safepath.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safepath.app.ui.theme.SOSRed
import com.safepath.app.ui.theme.SOSRedLight

/**
 * A pulsing SOS floating action button.
 *
 * When clicked, shows a confirmation dialog with a 3-second countdown.
 * On confirmation, triggers [onConfirmed] (caller should launch dialer).
 */
@Composable
fun SOSButton(
    modifier: Modifier = Modifier,
    onConfirmed: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    // Pulsing ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue  = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Pulsing ring
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(pulseScale)
                .background(
                    color  = SOSRed.copy(alpha = ringAlpha),
                    shape  = CircleShape
                )
        )

        // Main SOS button
        FloatingActionButton(
            onClick            = { showDialog = true },
            shape              = CircleShape,
            containerColor     = SOSRed,
            contentColor       = Color.White,
            modifier           = Modifier.size(60.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = "SOS",
                    modifier           = Modifier.size(22.dp)
                )
                Text(
                    text       = "SOS",
                    fontSize   = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White
                )
            }
        }
    }

    // Confirmation Dialog
    if (showDialog) {
        SOSConfirmDialog(
            onConfirm = {
                showDialog = false
                onConfirmed()
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun SOSConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF1A0010),
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint    = SOSRed,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text       = "Emergency SOS",
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                fontSize   = 20.sp
            )
        },
        text = {
            Column {
                Text(
                    text  = "This will call emergency services (112) immediately.",
                    color = Color(0xFFFFAAAA),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "Your live location will be shared with emergency contacts.",
                    color = Color(0xFFCC8888),
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = SOSRed)
            ) {
                Text("CALL 112 NOW", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFAA6666))
            }
        }
    )
}
