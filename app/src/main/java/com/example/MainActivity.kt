package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.ReminderRepository
import com.example.ui.reminders.ReminderDashboardScreen
import com.example.ui.theme.LembretesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LembretesTheme {
                // App offline: sem login, tudo fica salvo no próprio celular.
                ReminderDashboardScreen(
                    currentUserId = ReminderRepository.LOCAL_USER_ID,
                    userDisplayName = null
                )
            }
        }
    }
}
