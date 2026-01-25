package com.example.nabdtask.presentation.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
