package com.safepath.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using system default sans-serif as a fallback (replace with Google Fonts if needed)
val SafePathFontFamily = FontFamily.Default

val SafePathTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 57.sp,
        lineHeight = 64.sp,
        color      = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 45.sp,
        lineHeight = 52.sp,
        color      = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 32.sp,
        lineHeight = 40.sp,
        color      = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 24.sp,
        lineHeight = 32.sp,
        color      = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 20.sp,
        lineHeight = 28.sp,
        color      = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        lineHeight = 28.sp,
        color      = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        color      = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        color      = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        color      = TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        color      = TextMuted
    ),
    labelLarge = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        color      = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        color      = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = SafePathFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        color      = TextMuted
    )
)
