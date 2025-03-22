package com.example.logic3

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class BudgetTracker(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private var totalBudget: Double = 0.0
    private var startDate: LocalDate = LocalDate.now()
    private var endDate: LocalDate = LocalDate.now()
    var allocationPerDay: Double = 0.0
    private var totalRemainingBudget: Double = 0.0
    val expenses = mutableListOf<Expense>() // Keep expenses in memory for faster access

    init {
        loadBudget()
        loadExpenses()
    }

    //Gets the budget from the database and updates the values
    private fun loadBudget() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.BUDGET_TABLE_NAME,
            arrayOf(
                DatabaseHelper.BUDGET_COLUMN_TOTAL_BUDGET,
                DatabaseHelper.BUDGET_COLUMN_START_DATE,
                DatabaseHelper.BUDGET_COLUMN_END_DATE,
                DatabaseHelper.BUDGET_COLUMN_ALLOCATION_PER_DAY,
                DatabaseHelper.BUDGET_COLUMN_REMAINING_BUDGET
            ),
            null, null, null, null, null
        )

        cursor.use {
            if (it.moveToFirst()) {
                totalBudget = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.BUDGET_COLUMN_TOTAL_BUDGET))
                startDate = LocalDate.parse(it.getString(it.getColumnIndexOrThrow(DatabaseHelper.BUDGET_COLUMN_START_DATE)), DateTimeFormatter.ISO_DATE)
                endDate = LocalDate.parse(it.getString(it.getColumnIndexOrThrow(DatabaseHelper.BUDGET_COLUMN_END_DATE)), DateTimeFormatter.ISO_DATE)
                allocationPerDay = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.BUDGET_COLUMN_ALLOCATION_PER_DAY))
                totalRemainingBudget = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.BUDGET_COLUMN_REMAINING_BUDGET))
            } else {
                // Initialize with default values if no budget in DB
                totalBudget = 0.0
                startDate = LocalDate.now()
                endDate = LocalDate.now()
                allocationPerDay = 0.0
                totalRemainingBudget = 0.0
            }
        }
        db.close()
    }

    //save budget to the database
    private fun saveBudgetToDb() {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.BUDGET_TABLE_NAME, null, null) // Clear old budget data
        val values = ContentValues().apply {
            put(DatabaseHelper.BUDGET_COLUMN_TOTAL_BUDGET, totalBudget)
            put(DatabaseHelper.BUDGET_COLUMN_START_DATE, startDate.format(DateTimeFormatter.ISO_DATE))
            put(DatabaseHelper.BUDGET_COLUMN_END_DATE, endDate.format(DateTimeFormatter.ISO_DATE))
            put(DatabaseHelper.BUDGET_COLUMN_ALLOCATION_PER_DAY, allocationPerDay)
            put(DatabaseHelper.BUDGET_COLUMN_REMAINING_BUDGET, totalRemainingBudget)
        }
        db.insert(DatabaseHelper.BUDGET_TABLE_NAME, null, values)
        db.close()
    }

    private fun loadExpenses() {
        expenses.clear()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.EXPENSE_TABLE_NAME,
            arrayOf(
                DatabaseHelper.EXPENSE_COLUMN_ID,
                DatabaseHelper.EXPENSE_COLUMN_DESCRIPTION,
                DatabaseHelper.EXPENSE_COLUMN_AMOUNT,
                DatabaseHelper.EXPENSE_COLUMN_CATEGORY,
                DatabaseHelper.EXPENSE_COLUMN_DATE
            ),
            null, null, null, null, null
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.EXPENSE_COLUMN_ID))
                val description = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.EXPENSE_COLUMN_DESCRIPTION))
                val amount = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.EXPENSE_COLUMN_AMOUNT))
                val category = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.EXPENSE_COLUMN_CATEGORY))
                val date = LocalDate.parse(it.getString(it.getColumnIndexOrThrow(DatabaseHelper.EXPENSE_COLUMN_DATE)), DateTimeFormatter.ISO_DATE)
                expenses.add(Expense(id, description, amount, category, date))
            }
        }
        db.close()
    }

    //Set budget for a given period of time
    fun setBudget(amount: Double, endDate: LocalDate) {
        totalBudget = amount
        this.endDate = endDate
        startDate = LocalDate.now()
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        allocationPerDay = if (totalDays > 0) amount / totalDays else 0.0
        totalRemainingBudget = amount
        saveBudgetToDb() // Save budget to database
    }
    fun getTotalBudget(): Double {
        return totalBudget //usedinprogress bar in dashboardscreen and reportScreen
    }

    //Allows adding of expenses
    fun addExpense(description: String, amount: Double, category: String) {
        val today = LocalDate.now()
        val todayExpensesBeforeNew = expenses.filter { it.date == today }.sumOf { it.amount }
        val remainingForToday = allocationPerDay - todayExpensesBeforeNew

        // Create and save the expense
        val id = if (expenses.isEmpty()) 1 else expenses.maxOf { it.id } + 1
        val expense = Expense(id, description, amount, category)
        expenses.add(expense)

        // Always subtract the full amount from the total remaining budget
        totalRemainingBudget -= amount

        // If this expense exceeds today's remaining allocation, handle the overflow
        if (amount > remainingForToday) {
            // This expense will cause daily remaining to go negative,
            // which means we must adjust for future allocations
            adjustForOverflow()
        }

        // Save expense to database
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.EXPENSE_COLUMN_ID, id)
            put(DatabaseHelper.EXPENSE_COLUMN_DESCRIPTION, description)
            put(DatabaseHelper.EXPENSE_COLUMN_AMOUNT, amount)
            put(DatabaseHelper.EXPENSE_COLUMN_CATEGORY, category)
            put(DatabaseHelper.EXPENSE_COLUMN_DATE, expense.date.format(DateTimeFormatter.ISO_DATE))
        }
        db.insert(DatabaseHelper.EXPENSE_TABLE_NAME, null, values)
        db.close()

        // Update remaining budget in database
        updateRemainingBudgetInDb()
    }

    private fun updateRemainingBudgetInDb() {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.BUDGET_COLUMN_REMAINING_BUDGET, totalRemainingBudget)
        }
        db.update(DatabaseHelper.BUDGET_TABLE_NAME, values, null, null) // Assuming only one budget record
        db.close()
    }

    //Gets the remaining money allocated for the day
    fun getRemainingDailyAllocation(date: LocalDate = LocalDate.now()): Double {
        val dailySpent = expenses.filter { it.date == date }.sumOf { it.amount }
        // Return 0 if daily spent exceeds allocation, otherwise return the difference
        return maxOf(0.0, allocationPerDay - dailySpent)
    }

    fun getExpensesList(): String {
        return expenses.joinToString("\n") {
            "${it.description} - $${String.format("%.2f", it.amount)} (${it.category}) on ${it.date}"
        }
    }

    //Budget summary
    fun getBudgetSummary(): String {
        return "Total Budget: Ksh.${String.format("%.2f", totalBudget)}\n" +
                "Daily Allocation: Ksh.${String.format("%.2f", allocationPerDay)}\n" +
                "Remaining for today: Ksh.${String.format("%.2f", getRemainingDailyAllocation())}\n" +
                "Remaining Amount: Ksh.${String.format("%.2f", getTotalRemainingBudget())}"
    }

    // Public getter function to access totalRemainingBudget
    fun getTotalRemainingBudget(): Double {
        return totalRemainingBudget
    }

    // Public getter function to access dailyAllocation
    fun getDailyAllocation(): Double {
        return allocationPerDay
    }

    // underflow logic - handles cases where daily spending is less than allocation
    fun adjustForUnderflow(option: String = "reallocate") {
        val today = LocalDate.now()
        val dailySpent = expenses.filter { it.date == today }.sumOf { it.amount }

        // Check if today's expenses are less than the daily budget
        if (dailySpent < allocationPerDay) {
            // Calculate surplus
            val surplus = allocationPerDay - dailySpent

            // Calculate remaining days
            val remainingDays = ChronoUnit.DAYS.between(today.plusDays(1), endDate).toInt() + 1

            when (option) {
                "reallocate" -> {
                    // Spread surplus evenly across remaining days
                    if (remainingDays > 0) {
                        val additionalPerDay = surplus / remainingDays
                        allocationPerDay += additionalPerDay
                    }
                }
                "next_day" -> {
                    //TODO
                    // This option would require a daily_adjustments table
                    // For now, we just store the surplus into tomorrow's allocation
                    // This is a simplified implementation
                    if (remainingDays > 0) {
                        // Just add to the next day's allocation (simplified approach)
                        // for real implimentation we need a table, Couldn't crreate because of time.
                        totalRemainingBudget = totalRemainingBudget // No change, just being explicit
                    }
                }
                "save" -> {

                    totalRemainingBudget = totalRemainingBudget // No change needed
                }
            }

            // Update database
            saveBudgetToDb()
        }
    }

    //overflow logic - handles cases where daily spending exceeds allocation
    fun adjustForOverflow() {
        val today = LocalDate.now()
        val dailySpent = expenses.filter { it.date == today }.sumOf { it.amount }

        // Check if today's expenses exceed the daily budget
        if (dailySpent > allocationPerDay) {
            // Calculate remaining days
            val remainingDays = ChronoUnit.DAYS.between(today.plusDays(1), endDate).toInt() + 1

            if (remainingDays > 0 && totalRemainingBudget > 0) {
                // Recalculate allocation per day based on remaining budget and days
                allocationPerDay = totalRemainingBudget / remainingDays
            } else {
                // No more days left or no more budget
                allocationPerDay = 0.0
            }

            // Update database
            saveBudgetToDb()
        }
    }

    //Getting expenses by category, used in donutchart
    fun getExpensesByCategory(): Map<String, Double> {
        return expenses.groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }

    //reseting database
    fun resetDatabase() {
        val db = dbHelper.writableDatabase

        // Clear all tables
        db.delete(DatabaseHelper.EXPENSE_TABLE_NAME, null, null)
        db.delete(DatabaseHelper.BUDGET_TABLE_NAME, null, null)

        // Reset in-memory data
        totalBudget = 0.0
        startDate = LocalDate.now()
        endDate = LocalDate.now()
        allocationPerDay = 0.0
        totalRemainingBudget = 0.0
        expenses.clear()

        db.close()
    }
}