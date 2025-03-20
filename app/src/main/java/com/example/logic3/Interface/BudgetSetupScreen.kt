@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.logic3.Interface

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
//Budget setup ui
@Composable
fun BudgetSetupScreen(
    budgetAmount: String,
    onBudgetAmountChange: (String) -> Unit,
    selectedEndDate: LocalDate?,
    displayDate: String,
    onShowDatePicker: () -> Unit,
    onSetBudget: () -> Unit,
    onEndBudget: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Budget Setup",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
//budget amount input
        OutlinedTextField(
            value = budgetAmount,
            onValueChange = onBudgetAmountChange,
            label = { Text("Budget Amount") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Budget") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )

        // Date selection field - modified to show formatted date and be more visibly clickable
        OutlinedTextField(
            value = displayDate,
            onValueChange = { /* Readonly field, handled by date picker */ },
            label = { Text("End Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onShowDatePicker),
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "End Date") },
            trailingIcon = {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Select Date",
                    modifier = Modifier.clickable(onClick = onShowDatePicker)
                )
            },
            readOnly = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledTextColor = LocalContentColor.current,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
//set budget button
        Button(
            onClick = onSetBudget,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = budgetAmount.isNotEmpty() &&
                    budgetAmount.toDoubleOrNull() != null &&
                    selectedEndDate != null
        ) {
            Icon(Icons.Default.SaveAlt, contentDescription = "Set Budget")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Set Budget")
        }

        Spacer(modifier = Modifier.height(8.dp))
//end budget button
        OutlinedButton(
            onClick = onEndBudget,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = "End Budget")
            Spacer(modifier = Modifier.width(8.dp))
            Text("End Budget & Show Savings")
        }
    }
}