package com.safepath.app.ui.navigation_screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import com.safepath.app.data.model.SafetyLevel
import com.safepath.app.ui.components.SOSButton
import com.safepath.app.ui.components.SafetyBadge
import com.safepath.app.ui.routes.RouteViewModel
import com.safepath.app.ui.theme.*
import com.google.android.gms.maps.model.LatLng

@Composable
fun NavigationScreen(
    routeId: Int,
    onBack: () -> Unit,
    viewModel: NavigationViewModel = hiltViewModel(),
    routeViewModel: RouteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context  = LocalContext.current

    LaunchedEffect(routeId) {
        viewModel.loadRoute(routeId, routeViewModel)
    }

    val route = uiState.route
    val currentStep = uiState.currentStep

    // Map camera state
    val startLatLng = route?.polylinePoints?.firstOrNull() ?: LatLng(28.6304, 77.2177)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLatLng, 15f)
    }

    // Animate camera to current step
    LaunchedEffect(uiState.currentStepIndex) {
        currentStep?.startLocation?.let { loc ->
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(loc).zoom(16f).tilt(30f).build()
                ),
                durationMs = 800
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {

        // ── Google Map (full screen) ─────────────────────────────────────────
        GoogleMap(
            modifier           = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties         = MapProperties(mapType = MapType.NORMAL),
            uiSettings         = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            // Draw route polyline
            route?.polylinePoints?.let { points ->
                val color = when (route.safetyScore.level) {
                    SafetyLevel.SAFE     -> androidx.compose.ui.graphics.Color(0xFF00E676)
                    SafetyLevel.MODERATE -> androidx.compose.ui.graphics.Color(0xFFFFB300)
                    SafetyLevel.RISKY    -> androidx.compose.ui.graphics.Color(0xFFFF1744)
                }
                Polyline(
                    points = points,
                    color  = color,
                    width  = 12f,
                    geodesic = true
                )
            }

            // Destination marker
            route?.polylinePoints?.lastOrNull()?.let { dest ->
                Marker(
                    state   = MarkerState(position = dest),
                    title   = "Destination",
                    snippet = "You have arrived!"
                )
            }

            // Current step marker
            currentStep?.startLocation?.let { loc ->
                Marker(
                    state   = MarkerState(position = loc),
                    title   = "You are here"
                )
            }
        }

        // ── Top overlay (back + route info) ─────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth().align(Alignment.TopStart)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(BackgroundDark.copy(alpha = 0.95f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick  = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceDark)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text       = route?.name ?: "Navigating…",
                                color      = TextPrimary,
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            route?.let {
                                Text(
                                    text     = "${it.distance}  ·  ${it.duration}",
                                    color    = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    // Safety badge (compact)
                    route?.safetyScore?.let { score ->
                        SafetyBadge(score = score, compact = true)
                    }
                }
            }

            // Progress bar
            if (route != null) {
                LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color    = PurplePrimary,
                    trackColor = SurfaceVariant
                )
            }
        }

        // ── SOS button (top-right) ────────────────────────────────────────────
        SOSButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp),
            onConfirmed = {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:112"))
                context.startActivity(intent)
            }
        )

        // ── Bottom turn-by-turn panel ────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            // Current step instruction
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith
                    slideOutVertically { -it } + fadeOut()
                },
                label = "step_anim"
            ) { step ->
                if (step != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, BackgroundDark.copy(alpha = 0.9f), BackgroundDark)
                                )
                            )
                            .padding(top = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .background(SurfaceDark)
                                .padding(20.dp)
                        ) {
                            // Maneuver icon + instruction
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                ManeuverIcon(maneuver = step.maneuver)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text       = step.instruction,
                                        color      = TextPrimary,
                                        fontSize   = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 22.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text     = "${step.distance}  ·  ${step.duration}",
                                        color    = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Next step preview
                            uiState.nextStep?.let { next ->
                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = DividerColor)
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Default.NavigateNext,
                                        contentDescription = null,
                                        tint               = TextMuted,
                                        modifier           = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text     = "Then: ${next.instruction}",
                                        color    = TextMuted,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Navigation controls
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = viewModel::previousStep,
                                    enabled = uiState.currentStepIndex > 0,
                                    modifier = Modifier.weight(1f),
                                    shape   = RoundedCornerShape(12.dp),
                                    border  = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant)
                                ) {
                                    Icon(Icons.Default.NavigateBefore, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Prev", color = TextSecondary, fontSize = 13.sp)
                                }

                                // Step counter
                                Text(
                                    text      = "${uiState.currentStepIndex + 1} / ${route?.steps?.size ?: 1}",
                                    color     = TextMuted,
                                    fontSize  = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier  = Modifier.width(50.dp)
                                )

                                Button(
                                    onClick  = viewModel::nextStep,
                                    modifier = Modifier.weight(1f),
                                    shape    = RoundedCornerShape(12.dp),
                                    colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(listOf(PurplePrimary, PinkAccent)),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Next", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            Spacer(Modifier.width(4.dp))
                                            Icon(Icons.Default.NavigateNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Arrival dialog ────────────────────────────────────────────────────
        if (uiState.isArrived) {
            ArrivalDialog(onDismiss = {
                viewModel.dismissArrival()
                onBack()
            })
        }
    }
}

@Composable
private fun ManeuverIcon(maneuver: String) {
    val icon = when {
        maneuver.contains("left")    -> Icons.Default.TurnLeft
        maneuver.contains("right")   -> Icons.Default.TurnRight
        maneuver.contains("arrive")  -> Icons.Default.LocationOn
        maneuver.contains("depart")  -> Icons.Default.MyLocation
        else                         -> Icons.Default.Straight
    }
    val tint = when {
        maneuver.contains("arrive")  -> SafeGreen
        else                         -> PurplePrimary
    }

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(listOf(PurpleDark.copy(alpha = 0.5f), PinkAccent.copy(alpha = 0.3f)))
            )
            .then(
                if (maneuver.contains("arrive"))
                    Modifier.background(SafeGreen.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = maneuver,
            tint               = tint,
            modifier           = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun ArrivalDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceDark,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PurplePrimary, PinkAccent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(36.dp)
                )
            }
        },
        title = {
            Text(
                text       = "You've Arrived! 🎉",
                fontWeight = FontWeight.Bold,
                color      = TextPrimary,
                fontSize   = 20.sp,
                textAlign  = TextAlign.Center
            )
        },
        text = {
            Text(
                text      = "You reached your destination safely. Stay aware of your surroundings.",
                color     = TextSecondary,
                fontSize  = 14.sp,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors  = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.horizontalGradient(listOf(PurplePrimary, PinkAccent)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}
