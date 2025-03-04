package com.example.logic3.Interface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.logic3.Expense
import java.time.format.DateTimeFormatter

@Composable
fun ReportsScreen(expenses: List<Expense>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Expense Reports",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Category Summary
        val categoryTotals = expenses
            .groupBy { it.category }
            .mapValues { it.value.sumOf { expense -> expense.amount } }
            .toList()
            .sortedByDescending { it.second }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
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
                    categoryTotals.forEach { (category, total) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryChip(category)
                            Text(
                                text = "$${total}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Daily Spending
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Recent Daily Spending",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val dailyTotals = expenses
                    .groupBy { it.date }
                    .mapValues { it.value.sumOf { expense -> expense.amount } }
                    .toList()
                    .sortedByDescending { it.first }
                    .take(7)

                if (dailyTotals.isEmpty()) {
                    Text(
                        text = "No daily spending data available",
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    dailyTotals.forEach { (date, total) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = date.format(DateTimeFormatter.ISO_DATE))
                            Text(
                                text = "$${total}",
                                fontWeight = FontWeight.Bold,
                                color = if (total > 0) MaterialTheme.colorScheme.error else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // All Expenses Button
        Button(
            onClick = { /* Navigate to all expenses */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.List, contentDescription = "All Expenses")
            Spacer(modifier = Modifier.width(8.dp))
            Text("View All Expenses")
        }
    }
}
