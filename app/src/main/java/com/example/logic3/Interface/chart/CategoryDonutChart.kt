package com.example.logic3.Interface.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun CategoryDonutChart(
    categoryData: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    getCategoryColor: (String) -> Color
) {
    Box(
        modifier = modifier
            .size(250.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw the donut chart
        Canvas(modifier = Modifier.fillMaxSize()) {
            val total = categoryData.sumOf { it.second }
            if (total <= 0) return@Canvas

            val strokeWidth = size.width * 0.15f
            val outerRadius = (min(size.width, size.height) / 2) - (strokeWidth / 2)
            val innerRadius = outerRadius - strokeWidth
            val center = Offset(size.width / 2, size.height / 2)

            // Draw donut segments
            var startAngle = -90f
            categoryData.forEach { (category, amount) ->
                val sweepAngle = (amount / total * 360).toFloat()

                // Draw the arc
                drawArc(
                    color = getCategoryColor(category),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = strokeWidth)
                )

                startAngle += sweepAngle
            }

            // Draw inner circle (white hole)
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = center
            )
        }

        // Display total amount in the center
        Text(
            text = "Ksh.${categoryData.sumOf { it.second }.toInt()}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}