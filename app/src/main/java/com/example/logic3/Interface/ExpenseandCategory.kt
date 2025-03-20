package com.example.logic3.Interface

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
//ui for adding expense and category
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseItem(expense: Expense) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
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
                text = "Ksh.${expense.amount}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
//Category chip is place holder for image, icon or conditional colors...Each category with it's own colored icon.
@Composable
fun CategoryChip(
    category: String,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(category)
    val backgroundColor = categoryColor.copy(alpha = 0.15f)
    val borderColor = categoryColor.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Color indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(categoryColor)
            )

            // Category text
            Text(
                text = category,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
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
    //budget category
    var categoryOptions by remember { mutableStateOf(listOf("Food", "Transportation", "Entertainment", "Utilities", "Shopping", "Other")) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showAddCustomCategory by remember { mutableStateOf(false) }
    var customCategoryInput by remember { mutableStateOf("") }

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

        // Category selection with custom option
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
                //  Option to create a custom category
                DropdownMenuItem(
                    text = { Text("+ Add Custom Category") },
                    onClick = {
                        showCategoryDropdown = false
                        showAddCustomCategory = true
                    }
                )

                // Divider between add option and existing categories
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.LightGray)
                        .padding(vertical = 4.dp)
                )

                // Show existing categories
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

        // Custom category input dialog
        if (showAddCustomCategory) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Add Custom Category", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = customCategoryInput,
                        onValueChange = { customCategoryInput = it },
                        label = { Text("New Category Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showAddCustomCategory = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (customCategoryInput.isNotEmpty()) {
                                    // Add the new category to the list
                                    categoryOptions = categoryOptions + customCategoryInput
                                    // Select the new category
                                    onCategoryChange(customCategoryInput)
                                    // Reset the input and close the dialog
                                    customCategoryInput = ""
                                    showAddCustomCategory = false
                                }
                            },
                            enabled = customCategoryInput.isNotEmpty()
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
//add expense button
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
