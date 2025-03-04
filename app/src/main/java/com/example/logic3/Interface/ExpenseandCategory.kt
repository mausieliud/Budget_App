package com.example.logic3.Interface

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic3.Expense
import java.time.format.DateTimeFormatter

@Composable
fun ExpenseItem(expense: Expense) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryChip(expense.category)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = expense.date.format(DateTimeFormatter.ISO_DATE),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "$${expense.amount}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun CategoryChip(category: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = category,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseDescription: String,
    onDescriptionChange: (String) -> Unit,
    expenseAmount: String,
    onAmountChange: (String) -> Unit,
    expenseCategory: String,
    onCategoryChange: (String) -> Unit,
    onAddExpense: () -> Unit
) {
    val categoryOptions = listOf("Food", "Transportation", "Entertainment", "Utilities", "Shopping", "Other")
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add New Expense",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = expenseDescription,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = "Description") }
        )

        OutlinedTextField(
            value = expenseAmount,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Amount") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )

        Box {
            OutlinedTextField(
                value = expenseCategory,
                onValueChange = onCategoryChange,
                label = { Text("Category") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryDropdown = true },
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = "Category") },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select Category",
                        modifier = Modifier.clickable { showCategoryDropdown = true }
                    )
                },
                readOnly = true
            )

            DropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { showCategoryDropdown = false },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                categoryOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onCategoryChange(option)
                            showCategoryDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddExpense,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = expenseDescription.isNotEmpty() &&
                    expenseAmount.isNotEmpty() &&
                    expenseCategory.isNotEmpty() &&
                    expenseAmount.toDoubleOrNull() != null
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Expense")
        }
    }
}