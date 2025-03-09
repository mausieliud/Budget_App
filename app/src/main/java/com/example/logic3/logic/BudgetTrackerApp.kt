package com.example.logic3.logic

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.logic3.Interface.AddExpenseScreen
import com.example.logic3.Interface.BudgetSetupScreen
import com.example.logic3.BudgetTracker
import com.example.logic3.Interface.DashboardScreen
import com.example.logic3.Expense
import com.example.logic3.Interface.ReportsScreen
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTrackerApp() {
    val context = LocalContext.current
    val tracker = remember { BudgetTracker(context) }

    // State variables
    var budgetAmount by remember { mutableStateOf("") }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var expenseDescription by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseCategory by remember { mutableStateOf("") }

    // Tab state
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Add Expense", "Budget Setup", "Reports")

    // For refreshing expense list and budget summary
    var refreshTrigger by remember { mutableStateOf(0) }
    val refreshData = {
        refreshTrigger++
        Unit
    }

    // Snackbar for user feedback
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope() // **Get a coroutine scope**

    // Parse budget summary and expense list
    val budgetSummary = remember(refreshTrigger) { tracker.getBudgetSummary().split("\n") }
    val expensesList = remember(refreshTrigger) {
        val expenseStringList = tracker.getExpensesList().split("\n")
        expenseStringList.mapNotNull { line ->
            if (line.isBlank()) return@mapNotNull null // Skip blank lines
            val parts = line.split(" - ", limit = 2)
            val description = parts.getOrNull(0) ?: ""
            val restParts = parts.getOrNull(1)?.split(" ", "(", ")", "on ") ?: emptyList()
            val amount = restParts.firstOrNull { it.startsWith("$") }?.substring(1)?.toDoubleOrNull() ?: 0.0
            val category = restParts.firstOrNull { it.endsWith(")") }?.removeSuffix(")")?.removePrefix("(") ?: ""
            val date = restParts.lastOrNull()?.trim() ?: LocalDate.now().toString()
            Expense(0, description, amount, category, LocalDate.parse(date)) // ID is not parsed from string, assuming auto-increment by DB
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Add SnackbarHost
        topBar = {
            TopAppBar(
                title = { Text("Budget Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = when(index) {
                                    0 -> Icons.Default.Dashboard
                                    1 -> Icons.Default.Add
                                    2 -> Icons.Default.Settings
                                    else -> Icons.Default.Assessment
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(budgetSummary, expensesList, tracker, refreshData)
                1 -> AddExpenseScreen(
                    expenseDescription = expenseDescription,
                    onDescriptionChange = { expenseDescription = it },
                    expenseAmount = expenseAmount,
                    onAmountChange = { expenseAmount = it },
                    expenseCategory = expenseCategory,
                    onCategoryChange = { expenseCategory = it },
                    onAddExpense = {
                        val amount = expenseAmount.toDoubleOrNull()
                        if (expenseDescription.isNotEmpty() && amount != null && expenseCategory.isNotEmpty()) {
                            tracker.addExpense(expenseDescription, amount, expenseCategory)
                            expenseDescription = ""
                            expenseAmount = ""
                            expenseCategory = ""
                            refreshData()
                            // Show Snackbar for confirmation
                            coroutineScope.launch{ // **Launch coroutine here**
                                snackbarHostState.showSnackbar("Expense added successfully!")
                            }
                        } else {
                            coroutineScope.launch { // **Launch coroutine here**
                                snackbarHostState.showSnackbar("Invalid expense details. Please check.")
                            }
                        }
                    }
                )
                2 -> {
                    // Create the date picker within the composable function call for the correct context
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val formattedDate = selectedEndDate?.format(dateFormatter) ?: "Select Date"

                    BudgetSetupScreen(
                        budgetAmount = budgetAmount,
                        onBudgetAmountChange = { budgetAmount = it },
                        selectedEndDate = selectedEndDate,
                        displayDate = formattedDate,
                        onShowDatePicker = {
                            // Create the date picker on demand when the button is clicked
                            val calendar = Calendar.getInstance()

                            // Set the calendar to the selected date if one exists, otherwise use current date
                            selectedEndDate?.let {
                                calendar.set(it.year, it.monthValue - 1, it.dayOfMonth)
                            }

                            val datePickerDialog = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    selectedEndDate = LocalDate.of(year, month + 1, dayOfMonth)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )

                            datePickerDialog.show()
                        },
                        onSetBudget = {
                            val amount = budgetAmount.toDoubleOrNull()
                            if (amount != null && selectedEndDate != null) {
                                tracker.setBudget(amount, selectedEndDate!!)
                                refreshData()
                                coroutineScope.launch { // **Launch coroutine here**
                                    snackbarHostState.showSnackbar("Budget set successfully!")
                                }
                            } else {
                                coroutineScope.launch { // **Launch coroutine here**
                                    snackbarHostState.showSnackbar("Invalid budget amount or end date.")
                                }
                            }
                        },
                        onEndBudget = {
                            val remainingAmount = tracker.getTotalRemainingBudget()
                            coroutineScope.launch { // **Launch coroutine here**
                                snackbarHostState.showSnackbar("Budget ended. You have $${"%.2f".format(remainingAmount)} remaining.")
                            }
                            refreshData()
                        }
                    )
                }
                3 -> ReportsScreen(tracker.expenses)
            }
        }
    }
}