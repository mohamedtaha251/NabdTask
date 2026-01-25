package com.example.nabdtask.presentation.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nabdtask.data.notification.WorkManagerNotificationScheduler
import com.example.nabdtask.domain.model.LocalNotification
import com.example.nabdtask.domain.notification.NotificationScheduler
import com.example.nabdtask.domain.usecase.CancelNotificationUseCase
import com.example.nabdtask.domain.usecase.ScheduleNotificationUseCase
import com.example.nabdtask.ui.theme.NabdTaskTheme

class DetailActivity : ComponentActivity() {
    private lateinit var scheduler: NotificationScheduler
    private lateinit var scheduleNotification: ScheduleNotificationUseCase
    private lateinit var cancelNotification: CancelNotificationUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduler = WorkManagerNotificationScheduler(this)
        scheduleNotification = ScheduleNotificationUseCase(scheduler)
        cancelNotification = CancelNotificationUseCase(scheduler)
        enableEdgeToEdge()
        val id = intent.getIntExtra(EXTRA_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val time = intent.getLongExtra(EXTRA_TIME, 0L)
        val notification = LocalNotification(id, title, time)
        setContent {
            NabdTaskTheme {
                DetailScreen(
                    notification = notification,
                    onSchedule = { item ->
                        scheduleNotification(item)
                    },
                    onCancel = { item ->
                        cancelNotification(item)
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TIME = "extra_time"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScreen(
    notification: LocalNotification,
    onSchedule: (LocalNotification) -> Unit,
    onCancel: (LocalNotification) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification") },
                actions = {
                    IconButton(onClick = { onCancel(notification) }) {
                        Text("Cancel")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = notification.title)
            Button(
                onClick = {
                    onSchedule(notification)
                    showDialog = true
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Schedule this notification")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            text = { Text("Notification scheduled") }
        )
    }
}
