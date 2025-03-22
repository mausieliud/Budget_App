package com.example.logic3.Interface
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.logic3.BudgetTracker
import com.example.logic3.Expense
import com.example.logic3.Interface.calculateColor
//ui for dashboard
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    budgetSummary: List<String>,
    expenses: List<Expense>,
    tracker: BudgetTracker,
    refreshData: () -> Unit
) {
    val context = LocalContext.current


    BudgetProgressBar(
        totalBudget = tracker.getTotalBudget(),
        totalRemainingBudget = tracker.getTotalRemainingBudget()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Column(modifier = Modifier.fillMaxSize()) {
        // Budget Summary Card
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
                    text = "Budget Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                budgetSummary.forEach { line ->
                    val parts = line.split(": ")
                    if (parts.size == 2) {
                        // Check if this is either "Remaining for today" or "Remaining Amount" line
                        val isRemainingForToday = parts[0].contains("Remaining for today")
                        val isRemainingAmount = parts[0].contains("Remaining Amount")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = parts[0], fontWeight = FontWeight.Medium)

                            // Apply dynamic color for both remaining values
                            if (isRemainingForToday || isRemainingAmount) {
                                // Extract numerical value from the string (remove "Ksh." and parse)
                                val valueStr = parts[1].replace("Ksh.", "").trim()
                                val remainingValue = valueStr.toDoubleOrNull() ?: 0.0

                                // Choose the appropriate total to compare against
                                val totalValue = if (isRemainingForToday) {
                                    tracker.getDailyAllocation()
                                } else {
                                    // For total remaining amount, compare against the original total budget
                                    tracker.getTotalRemainingBudget() + expenses.sumOf { it.amount } // Approximation of original budget
                                }

                                Text(
                                    text = parts[1],
                                    fontWeight = FontWeight.Bold,
                                    color = calculateColor(remainingValue, totalValue)
                                )
                            } else {
                                Text(text = parts[1], fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        //ui for underflow and overflow
        // Get reference to SharedPreferences
        val sharedPreferences = LocalContext.current.getSharedPreferences("BudgetAppPrefs", Context.MODE_PRIVATE)
        // value to record whether the button has been shown
        val hasShownUnderflowCard = remember { mutableStateOf(sharedPreferences.getBoolean("hasShownUnderflowCard", false)) }

        if (tracker.getRemainingDailyAllocation() > 0 && !hasShownUnderflowCard.value) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "You're under budget today! What would you like to do with the surplus?",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                tracker.adjustForUnderflow("reallocate")
                                refreshData()
                                // Save to SharedPreferences that card has been shown
                                sharedPreferences.edit().putBoolean("hasShownUnderflowCard", true).apply()
                                hasShownUnderflowCard.value = true
                                Toast.makeText(context, "Reallocated", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Reallocate")
                        }

                        Button(
                            onClick = {
                                tracker.adjustForUnderflow("next_day")
                                refreshData()
                                // Save to SharedPreferences that card has been shown
                                sharedPreferences.edit().putBoolean("hasShownUnderflowCard", true).apply()
                                hasShownUnderflowCard.value = true
                                Toast.makeText(context, "Added to next day", Toast.LENGTH_LONG).show()

                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Add to Tomorrow")
                        }

                        Button(
                            onClick = {
                                tracker.adjustForUnderflow("save")
                                refreshData()
                                // Save to SharedPreferences that card has been shown
                                sharedPreferences.edit().putBoolean("hasShownUnderflowCard", true).apply()
                                hasShownUnderflowCard.value = true
                                Toast.makeText(context, "Saved", Toast.LENGTH_LONG).show()

                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }

        // Recent Expenses
        Text(
            text = "Recent Expenses",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expenses yet",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(expenses.take(100)) { expense ->
                    ExpenseItem(expense)
                }

                }
            }
        }
    }

