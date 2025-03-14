package com.example.logic3.Interface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.logic3.BudgetTracker
import com.example.logic3.Expense
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.math.pow
import kotlin.math.roundToInt
import com.example.logic3.Interface.chart.CategoryDonutChart
import com.example.logic3.Interface.chart.CategoryLegend
import java.math.BigDecimal
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box


@Composable
fun ReportsScreen(
    expenses: List<Expense>,
    budgetTracker: BudgetTracker,
    onExportReport: (String) -> Unit
) {
    var selectedTimeFrame by remember { mutableStateOf("Week") }
    val timeFrameOptions = listOf("Week", "Month", "All")
    var showTimeFrameDropdown by remember { mutableStateOf(false) }
    var selectedChartType by remember { mutableStateOf("Categories") }
    val chartTypeOptions = listOf("Categories", "Daily", "Trends")
    var showChartTypeDropdown by remember { mutableStateOf(false) }

    // Filter expenses based on selected time frame
    val filteredExpenses = remember(expenses, selectedTimeFrame) {
        when (selectedTimeFrame) {
            "Week" -> expenses.filter {
                it.date.isAfter(LocalDate.now().minusWeeks(1)) || it.date.isEqual(LocalDate.now())
            }
            "Month" -> expenses.filter {
                it.date.isAfter(LocalDate.now().minusMonths(1)) || it.date.isEqual(LocalDate.now())
            }
            else -> expenses
        }
    }

    // Calculate various metrics
    val totalSpent = filteredExpenses.sumOf { it.amount }
    val averageDaily = if (filteredExpenses.isNotEmpty()) {
        val uniqueDays = filteredExpenses.map { it.date }.distinct().size
        if (uniqueDays > 0) totalSpent / uniqueDays else 0.0
    } else 0.0

    val categoryTotals = filteredExpenses
        .groupBy { it.category }
        .mapValues { it.value.sumOf { expense -> expense.amount } }
        .toList()
        .sortedByDescending { it.second }

    val dailyTotals = filteredExpenses
        .groupBy { it.date }
        .mapValues { it.value.sumOf { expense -> expense.amount } }
        .toList()
        .sortedByDescending { it.first }

    val biggestExpense = filteredExpenses.maxByOrNull { it.amount }
    val mostFrequentCategory = filteredExpenses
        .groupBy { it.category }
        .maxByOrNull { it.value.size }?.key ?: "None"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Expense Reports",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                val reportText = generateFullReport(filteredExpenses, budgetTracker)
                onExportReport(reportText)
            }) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Export Report",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Filter Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Time frame selector
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { showTimeFrameDropdown = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedTimeFrame)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, "Select Time Frame")
                }

                DropdownMenu(
                    expanded = showTimeFrameDropdown,
                    onDismissRequest = { showTimeFrameDropdown = false }
                ) {
                    timeFrameOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedTimeFrame = option
                                showTimeFrameDropdown = false
                            }
                        )
                    }
                }
            }

            // Chart type selector
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { showChartTypeDropdown = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedChartType)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, "Select Chart Type")
                }

                DropdownMenu(
                    expanded = showChartTypeDropdown,
                    onDismissRequest = { showChartTypeDropdown = false }
                ) {
                    chartTypeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedChartType = option
                                showChartTypeDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Summary Card with key metrics
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Spending Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Spent:")
                    Text(
                        text = "Ksh.${totalSpent.roundToDecimalPlaces(2)}",
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily Average:")
                    Text(
                        text = "Ksh.${averageDaily.roundToDecimalPlaces(2)}",
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Budget Utilization:")
                    val availableBudget = budgetTracker.getTotalRemainingBudget()
                    val totalBudget = availableBudget + totalSpent
                    val utilization = if (totalBudget > 0) (totalSpent / totalBudget * 100) else 0.0
                    Text(
                        text = "${utilization.roundToDecimalPlaces(1)}%",
                        fontWeight = FontWeight.Bold,
                        color = when {
                            utilization > 90 -> MaterialTheme.colorScheme.error
                            utilization > 75 -> Color(0xFFFF9800) // Orange
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }

                if (biggestExpense != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Largest Expense:")
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Ksh.${biggestExpense.amount}",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = biggestExpense.description,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Most Frequent Category:")
                    CategoryChip(mostFrequentCategory)
                }
            }
        }

        //minMax
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Min Spent Card
            MinMaxSpentCard(
                modifier = Modifier
                    .weight(1f)
                    .height(IntrinsicSize.Min),
                isMin = true,
                expenses = filteredExpenses,
                onExportReport = onExportReport
            )

            // Max Spent Card
            MinMaxSpentCard(
                modifier = Modifier
                    .weight(1f)
                    .height(IntrinsicSize.Min),
                isMin = false,
                expenses = filteredExpenses,
                onExportReport = onExportReport
            )
        }

        when (selectedChartType) {
            "Categories" -> {
                // Category Breakdown
                // Updated Category Breakdown card with both chart and legend
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                ){
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Category Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (categoryTotals.isEmpty()) {
                            Text(
                                text = "No expense data available",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            // Chart and Legend in a Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Add the Donut Chart
                                Box(
                                    modifier = Modifier
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CategoryDonutChart(
                                        categoryData = categoryTotals,
                                        getCategoryColor = ::getCategoryColor
                                    )
                                }

                                // Add the Legend
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    CategoryLegend(
                                        categories = categoryTotals.map { it.first },
                                        getCategoryColor = ::getCategoryColor
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            // Calculate percentages for each category
                            val totalExpenses = categoryTotals.sumOf { it.second }

                            categoryTotals.forEach { (category, total) ->
                                val percentage = if (totalExpenses > 0) (total / totalExpenses * 100) else 0.0

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CategoryChip(category)
                                        Row {
                                            Text(
                                                text = "Ksh.${total.roundToDecimalPlaces(2)}",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = " (${percentage.roundToDecimalPlaces(1)}%)",
                                                color = Color.Gray,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }

                                    // Category progress bar
                                    LinearProgressIndicator(
                                        progress = (percentage / 100).toFloat(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .padding(top = 4.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = getCategoryColor(category)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "Daily" -> {
                // Daily Spending Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Daily Spending",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (dailyTotals.isEmpty()) {
                            Text(
                                text = "No daily spending data available",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            // Calculate daily budget from budget tracker
                            val dailyBudget = budgetTracker.getDailyAllocation()

                            dailyTotals.forEach { (date, total) ->
                                val overBudget = total > dailyBudget
                                val percentOfBudget = if (dailyBudget > 0) (total / dailyBudget * 100) else 0.0

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Row {
                                            Text(
                                                text = "$${total.roundToDecimalPlaces(2)}",
                                                fontWeight = FontWeight.Bold,
                                                color = if (overBudget) MaterialTheme.colorScheme.error else Color.Unspecified
                                            )
                                            if (dailyBudget > 0) {
                                                Text(
                                                    text = " (${percentOfBudget.roundToDecimalPlaces(0)}%)",
                                                    color = Color.Gray,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }

                                    // Daily spending progress bar
                                    if (dailyBudget > 0) {
                                        LinearProgressIndicator(
                                            progress = (percentOfBudget / 100).coerceAtMost(1.0).toFloat(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .padding(top = 4.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            //Bar chart with chartlibrary---todo find library
                            //try donut
                        }
                    }
                }
            }
            "Trends" -> {
                // Spending Trends Analysis Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Spending Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (filteredExpenses.isEmpty() || dailyTotals.size < 2) {
                            Text(
                                text = "Not enough data to analyze trends",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            // Calculate trend (increasing/decreasing)
                            val sortedDaily = dailyTotals.sortedBy { it.first }
                            val trend = calculateTrend(sortedDaily)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when {
                                        trend > 0 -> Icons.Default.TrendingUp
                                        trend < 0 -> Icons.Default.TrendingDown
                                        else -> Icons.Default.TrendingFlat
                                    },
                                    contentDescription = "Spending Trend",
                                    tint = when {
                                        trend > 0 -> MaterialTheme.colorScheme.error
                                        trend < 0 -> Color.Green
                                        else -> Color.Gray
                                    },
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = when {
                                        trend > 0.1 -> "Your spending is increasing"
                                        trend < -0.1 -> "Your spending is decreasing"
                                        else -> "Your spending is stable"
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Show weekly averages if we have enough data
                            if (filteredExpenses.size >= 7) {
                                val weeklyAverages = calculateWeeklyAverages(filteredExpenses)

                                Text(
                                    text = "Weekly Averages",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )

                                weeklyAverages.forEach { (weekStart, average) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${weekStart.format(DateTimeFormatter.ofPattern("MMM dd"))} - " +
                                                    "${weekStart.plusDays(6).format(DateTimeFormatter.ofPattern("MMM dd"))}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "$${average.roundToDecimalPlaces(2)}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Line chart...TODO find library
                        }
                    }
                }
            }
        }

        // Top Expenses List
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
        ){
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Top Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (filteredExpenses.isEmpty()) {
                    Text(
                        text = "No expenses to display",
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    filteredExpenses.sortedByDescending { it.amount }.take(5).forEach { expense ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = expense.description,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CategoryChip(expense.category)
                                    Text(
                                        text = expense.date.format(DateTimeFormatter.ofPattern("MMM dd")),
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Text(
                                text = "Ksh.${expense.amount}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        // Export and Share buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = {
                    val reportText = generateFullReport(filteredExpenses, budgetTracker)
                    onExportReport(reportText)
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Export CSV",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export CSV")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val reportText = generateSummaryReport(filteredExpenses, budgetTracker)
                    onExportReport(reportText)
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share Report",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Report")
            }
        }
    }
}

// Helper functions for the ReportsScreen
private fun Double.roundToDecimalPlaces(places: Int): Double {
    val factor = 10.0.pow(places)
    return (this * factor).roundToInt() / factor
}
//Useful once chart libraries found,
fun getCategoryColor(category: String): Color {
    return when (category) {
        "Food" -> Color(0xFF4CAF50) // Green
        "Transportation" -> Color(0xFF2196F3) // Blue
        "Entertainment" -> Color(0xFFFF9800) // Orange
        "Utilities" -> Color(0xFF9C27B0) // Purple
        "Shopping" -> Color(0xFFE91E63) // Pink
        else -> Color(0xFF607D8B) // Blue Gray
    }
}

private fun calculateTrend(dailyTotals: List<Pair<LocalDate, Double>>): Double {
    if (dailyTotals.size < 2) return 0.0

    // Simple linear regression to detect trend direction
    val n = dailyTotals.size
    val x = List(n) { it.toDouble() }
    val y = dailyTotals.map { it.second }

    val sumX = x.sum()
    val sumY = y.sum()
    val sumXY = x.zip(y).sumOf { it.first * it.second }
    val sumXX = x.sumOf { it * it }

    // Calculate slope
    return if (n * sumXX - sumX * sumX != 0.0) {
        (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)
    } else {
        0.0
    }
}

private fun calculateWeeklyAverages(expenses: List<Expense>): List<Pair<LocalDate, Double>> {
    if (expenses.isEmpty()) return emptyList()

    // Find min and max dates
    val minDate = expenses.minOf { it.date }
    val maxDate = expenses.maxOf { it.date }

    // Adjust minDate to start of week (Monday)
    val startDate = minDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    // Group expenses by week
    val weeklyExpenses = mutableMapOf<LocalDate, MutableList<Expense>>()

    var currentWeekStart = startDate
    while (currentWeekStart.isBefore(maxDate) || currentWeekStart.isEqual(maxDate)) {
        val nextWeekStart = currentWeekStart.plusWeeks(1)

        // Get expenses for this week
        val weekExpenses = expenses.filter {
            !it.date.isBefore(currentWeekStart) && it.date.isBefore(nextWeekStart)
        }

        if (weekExpenses.isNotEmpty()) {
            weeklyExpenses[currentWeekStart] = weekExpenses.toMutableList()
        }

        currentWeekStart = nextWeekStart
    }

    // Calculate averages
    return weeklyExpenses.map { (weekStart, weekExpenses) ->
        val dailyTotal = weekExpenses.groupBy { it.date }
            .mapValues { it.value.sumOf { expense -> expense.amount } }
        val average = dailyTotal.values.average()

        weekStart to average
    }.sortedBy { it.first }
}

// Add this function to BudgetTracker class
fun BudgetTracker.getDailyAllocation(): Double {
    return allocationPerDay
}

private fun generateFullReport(expenses: List<Expense>, budgetTracker: BudgetTracker): String {
    val sb = StringBuilder()

    // CSV Header
    sb.appendLine("Date,Description,Amount,Category")

    // Add all expenses
    expenses.sortedByDescending { it.date }.forEach { expense ->
        sb.appendLine("${expense.date},\"${expense.description}\",${expense.amount},${expense.category}")
    }

    return sb.toString()
}

private fun generateSummaryReport(expenses: List<Expense>, budgetTracker: BudgetTracker): String {
    val sb = StringBuilder()

    // Budget Info
    sb.appendLine("BUDGET SUMMARY REPORT")
    sb.appendLine("Generated on: ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}")
    sb.appendLine()

    val totalBudget = budgetTracker.getTotalRemainingBudget() + expenses.sumOf { it.amount }
    val remainingBudget = budgetTracker.getTotalRemainingBudget()
    val totalSpent = expenses.sumOf { it.amount }

    sb.appendLine("Total Budget: Ksh.${totalBudget.roundToDecimalPlaces(2)}")
    sb.appendLine("Total Spent: Ksh.${totalSpent.roundToDecimalPlaces(2)}")
    sb.appendLine("Remaining Budget: Ksh.${remainingBudget.roundToDecimalPlaces(2)}")
    sb.appendLine("Daily Allocation: Ksh.${budgetTracker.getDailyAllocation().roundToDecimalPlaces(2)}")
    sb.appendLine()

    // Category Breakdown
    sb.appendLine("SPENDING BY CATEGORY")
    expenses.groupBy { it.category }
        .mapValues { it.value.sumOf { expense -> expense.amount } }
        .toList()
        .sortedByDescending { it.second }
        .forEach { (category, amount) ->
            sb.appendLine("$category: $${amount.roundToDecimalPlaces(2)}")
        }
    sb.appendLine()

    // Date range
    if (expenses.isNotEmpty()) {
        val minDate = expenses.minOf { it.date }
        val maxDate = expenses.maxOf { it.date }
        sb.appendLine("Report period: ${minDate.format(DateTimeFormatter.ofPattern("MMM dd"))} to ${maxDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}")
    }

    return sb.toString()
}
