package com.example.logic3.Interface.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Function to get category colors
@Composable
fun getCategoryColors(categories: List<String>): Map<String, Color> {
    // Define a list of distinct colors
    val colors = listOf(
        Color(0xFF4285F4), // Blue
        Color(0xFFEA4335), // Red
        Color(0xFFFBBC05), // Yellow
        Color(0xFF34A853), // Green
        Color(0xFF9C27B0), // Purple
        Color(0xFFFF9800), // Orange
        Color(0xFF795548), // Brown
        Color(0xFF607D8B)  // Gray
    )

    // Map categories to colors (repeat colors if there are more categories than colors)
    return categories.mapIndexed { index, category ->
        category to colors[index % colors.size]
    }.toMap()
}

// Composable for the donut chart
@Composable
fun CategoryDonutChart(
    categoryExpenses: Map<String, Double>,
    modifier: Modifier = Modifier,
    donutThickness: Float = 60f
) {
    val total = categoryExpenses.values.sum()

    // Skip rendering if there are no expenses
    if (total <= 0) {
        Box(
            modifier = modifier.fillMaxWidth().height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No expense data to display")
        }
        return
    }

    val categories = categoryExpenses.keys.toList()
    val colorMap = getCategoryColors(categories)

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Expenses by Category",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            // Draw the donut chart
            Canvas(modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val radius = (canvasWidth.coerceAtMost(canvasHeight) - donutThickness) / 2
                val center = Offset(canvasWidth / 2, canvasHeight / 2)

                var startAngle = -90f // Start from the top (12 o'clock position)

                categoryExpenses.forEach { (category, amount) ->
                    val sweepAngle = (amount / total * 360).toFloat()
                    val color = colorMap[category] ?: Color.Gray

                    // Draw the arc
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius - donutThickness / 2, center.y - radius - donutThickness / 2),
                        size = Size((radius + donutThickness / 2) * 2, (radius + donutThickness / 2) * 2),
                        style = Stroke(width = donutThickness)
                    )

                    startAngle += sweepAngle
                }

                // Draw a white circle in the middle to create the donut hole
                drawCircle(
                    color = Color.White,
                    radius = radius - donutThickness / 2,
                    center = center
                )

                // Draw total amount in the center
                drawContext.canvas.nativeCanvas.drawText(
                    "Ksh.${total.toInt()}",
                    center.x - 40,
                    center.y + 8,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 36f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }

        // Draw the legend
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            categoryExpenses.forEach { (category, amount) ->
                val percentage = (amount / total * 100).toInt()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colorMap[category] ?: Color.Gray, shape = RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Ksh.${amount.toInt()} ($percentage%)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}