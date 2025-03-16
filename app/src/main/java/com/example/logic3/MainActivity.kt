@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.logic3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.logic3.logic.BudgetTrackerApp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()


        // Option to Keep splash screen visible until your app is ready
        splashScreen.setKeepOnScreenCondition {
            false
        }

        setContent {
            com.example.logic3.ui.theme.Logic3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BudgetTrackerApp()
                }
            }
        }
    }
}

