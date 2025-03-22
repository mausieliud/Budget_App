package com.example.logic3.Interface

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@Composable
fun BudgetProgressBar(
    totalBudget: Double,
    totalRemainingBudget: Double
) {
    val remainingPercentage = (totalRemainingBudget / totalBudget).coerceIn(0.0, 1.0)

    LinearProgressIndicator(
        progress = { remainingPercentage.toFloat() },
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = calculateColor(totalRemainingBudget, totalBudget),
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

// Function to calculate color based on remaining percentage
fun calculateColor(remaining: Double, total: Double): Color {
    val percentage = if (total > 0) remaining / total else 0.0
    val safePercentage = percentage.coerceIn(0.0, 1.0)

    val red = (255 * (1 - safePercentage)).toInt()
    val green = (255 * safePercentage).toInt()

    return Color(red, green, 0)
}
