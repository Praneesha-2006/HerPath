package com.safepath.app.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safepath.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearch: (source: String, destination: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Shield pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "shield")
    val shieldScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "shield_scale"
    )
    val shieldGlow by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 0.9f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "shield_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // ── Decorative background blobs ──────────────────────────────────────
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        listOf(PurplePrimary.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    CircleShape
                )
                .blur(60.dp)
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .background(
                    Brush.radialGradient(
                        listOf(PinkAccent.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    CircleShape
                )
                .blur(60.dp)
        )

        // ── Content ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            // Shield icon with glow
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(shieldScale),
                contentAlignment = Alignment.Center
            ) {
                // Glow ring
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(PurplePrimary.copy(alpha = shieldGlow * 0.35f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
                // Shield circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(PurpleDark, PinkAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Shield,
                        contentDescription = "SafePath Shield",
                        tint               = Color.White,
                        modifier           = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // App name
            Text(
                text       = "SafePath",
                fontSize   = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                style      = LocalTextStyle.current.copy(
                    brush = Brush.horizontalGradient(listOf(PurpleLight, PinkAccent))
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text      = "Your safety, our priority",
                color     = TextSecondary,
                fontSize  = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // ── Input card ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
            ) {
                // Top gradient line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Brush.horizontalGradient(listOf(PurplePrimary, PinkAccent)))
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text       = "Find Safe Routes",
                        color      = TextPrimary,
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = "We'll choose the safest path for you",
                        color    = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(20.dp))

                    // Source field
                    SafePathTextField(
                        value          = uiState.source,
                        onValueChange  = viewModel::onSourceChanged,
                        label          = "From",
                        placeholder    = "Enter starting location",
                        leadingIcon    = Icons.Default.MyLocation,
                        leadingTint    = SafeGreen,
                        isError        = uiState.sourceError != null,
                        errorMessage   = uiState.sourceError,
                        imeAction      = ImeAction.Next
                    )

                    Spacer(Modifier.height(4.dp))

                    // Swap button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = viewModel::swapLocations,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.SwapVert,
                                contentDescription = "Swap locations",
                                tint               = PurpleLight,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Destination field
                    SafePathTextField(
                        value          = uiState.destination,
                        onValueChange  = viewModel::onDestinationChanged,
                        label          = "To",
                        placeholder    = "Enter destination",
                        leadingIcon    = Icons.Default.LocationOn,
                        leadingTint    = PinkAccent,
                        isError        = uiState.destinationError != null,
                        errorMessage   = uiState.destinationError,
                        imeAction      = ImeAction.Search
                    )

                    Spacer(Modifier.height(24.dp))

                    // Search button
                    Button(
                        onClick = {
                            if (viewModel.validate()) {
                                onSearch(uiState.source.trim(), uiState.destination.trim())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(PurplePrimary, PinkAccent)),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint     = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text       = "Find Safe Routes",
                                    color      = Color.White,
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Quick stats ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(
                    icon    = Icons.Default.Shield,
                    iconTint = SafeGreen,
                    label   = "Safety First",
                    value   = "Routes scored",
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    icon    = Icons.Default.Visibility,
                    iconTint = PurpleLight,
                    label   = "Real-time",
                    value   = "CCTV zones",
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    icon    = Icons.Default.Warning,
                    iconTint = SOSRed,
                    label   = "SOS Ready",
                    value   = "One tap away",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SafePathTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    leadingTint: Color,
    isError: Boolean,
    errorMessage: String?,
    imeAction: ImeAction
) {
    Column {
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(14.dp),
            label         = { Text(label, fontSize = 13.sp) },
            placeholder   = { Text(placeholder, color = TextMuted, fontSize = 13.sp) },
            leadingIcon   = {
                Icon(imageVector = leadingIcon, contentDescription = null, tint = leadingTint, modifier = Modifier.size(20.dp))
            },
            isError       = isError,
            singleLine    = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction      = imeAction
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = PurplePrimary,
                unfocusedBorderColor = SurfaceVariant,
                focusedLabelColor    = PurplePrimary,
                unfocusedLabelColor  = TextMuted,
                cursorColor          = PurplePrimary,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary,
                errorBorderColor     = RiskyRed,
                errorLabelColor      = RiskyRed,
                unfocusedContainerColor = SurfaceVariant.copy(alpha = 0.5f),
                focusedContainerColor   = SurfaceVariant.copy(alpha = 0.5f)
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text     = errorMessage,
                color    = RiskyRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
            )
        }
    }
}

@Composable
private fun QuickStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = iconTint,
            modifier           = Modifier.size(22.dp)
        )
        Text(
            text       = label,
            color      = TextPrimary,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign  = TextAlign.Center
        )
        Text(
            text      = value,
            color     = TextMuted,
            fontSize  = 9.sp,
            textAlign = TextAlign.Center
        )
    }
}
