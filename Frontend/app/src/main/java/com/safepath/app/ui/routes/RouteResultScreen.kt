package com.safepath.app.ui.routes

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safepath.app.ui.components.RouteCard
import com.safepath.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteResultScreen(
    source: String,
    destination: String,
    onStartNavigation: (routeId: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Trigger load once
    LaunchedEffect(source, destination) {
        viewModel.loadRoutes(source, destination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
            ) {
                // Gradient accent line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.BottomCenter)
                        .background(Brush.horizontalGradient(listOf(PurplePrimary, PinkAccent)))
                )

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector        = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint               = TextPrimary
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = "Safe Routes",
                                color      = TextPrimary,
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            RouteHeaderRow(source = source, destination = destination)
                        }
                    }
                }
            }

            // ── Body ─────────────────────────────────────────────────────────
            when (val state = uiState) {
                is RoutesUiState.Loading -> LoadingContent()
                is RoutesUiState.Error   -> ErrorContent(state.message, onBack)
                is RoutesUiState.Success -> {
                    LazyColumn(
                        modifier            = Modifier.weight(1f),
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            // Safety summary banner
                            SafetySummaryBanner(routeCount = state.routes.size)
                            Spacer(Modifier.height(4.dp))
                        }

                        items(state.routes, key = { it.id }) { route ->
                            AnimatedVisibility(
                                visible = true,
                                enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                            ) {
                                RouteCard(
                                    route      = route,
                                    isSelected = route.id == state.selectedRouteId,
                                    onClick    = { viewModel.selectRoute(route.id) }
                                )
                            }
                        }

                        item { Spacer(Modifier.height(80.dp)) } // bottom padding for FAB
                    }

                    // ── Start Navigation button ──────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, BackgroundDark)
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { onStartNavigation(state.selectedRouteId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape  = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(listOf(PurplePrimary, PinkAccent)),
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint               = Color.White,
                                        modifier           = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text       = "Start Safe Navigation",
                                        color      = Color.White,
                                        fontSize   = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun RouteHeaderRow(source: String, destination: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text     = source,
            color    = TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Icon(
            imageVector        = Icons.Default.ArrowForward,
            contentDescription = null,
            tint               = PurpleLight,
            modifier           = Modifier.size(12.dp)
        )
        Text(
            text     = destination,
            color    = TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun SafetySummaryBanner(routeCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceVariant)
            .padding(14.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(PurplePrimary, PinkAccent))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Shield,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text       = "Found $routeCount routes — ranked by safety",
                color      = TextPrimary,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text     = "★ Recommended route is highlighted",
                color    = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(
                color            = PurplePrimary,
                strokeWidth      = 3.dp,
                modifier         = Modifier.size(48.dp)
            )
            Text(
                text     = "Analyzing route safety…",
                color    = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(12.dp),
            modifier              = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint               = RiskyRed,
                modifier           = Modifier.size(48.dp)
            )
            Text(text = "Something went wrong", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(text = message, color = TextSecondary, fontSize = 13.sp)
            TextButton(onClick = onBack) {
                Text("Go Back", color = PurpleLight)
            }
        }
    }
}
