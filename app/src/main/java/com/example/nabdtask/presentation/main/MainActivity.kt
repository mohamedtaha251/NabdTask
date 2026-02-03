package com.example.nabdtask.presentation.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.nabdtask.data.notification.WorkManagerNotificationScheduler
import com.example.nabdtask.data.repository.AssetsNotificationsRepository
import com.example.nabdtask.domain.notification.NotificationScheduler
import com.example.nabdtask.domain.usecase.CancelAllNotificationsUseCase
import com.example.nabdtask.domain.usecase.LoadNotificationsUseCase
import com.example.nabdtask.presentation.detail.DetailActivity
import com.example.nabdtask.ui.theme.NabdTaskTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private var refreshKey by mutableIntStateOf(0)
    private var shouldReloadOnResume by mutableStateOf(false)
    private val openDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshKey += 1
    }

    private lateinit var scheduler: NotificationScheduler
    private lateinit var loadNotifications: LoadNotificationsUseCase
    private lateinit var cancelAllNotifications: CancelAllNotificationsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduler = WorkManagerNotificationScheduler(this)
        loadNotifications = LoadNotificationsUseCase(AssetsNotificationsRepository())
        cancelAllNotifications = CancelAllNotificationsUseCase(scheduler)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val processLifecycleOwner = ProcessLifecycleOwner.get()
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    shouldReloadOnResume = true
                }
                else -> {}
            }
        }
        processLifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        setContent {
            NabdTaskTheme {
                NotificationsScreen(
                    onCancelAll = { cancelAllNotifications() },
                    onOpenDetail = { notification ->
                        val intent = Intent(this, DetailActivity::class.java).apply {
                            putExtra(DetailActivity.EXTRA_ID, notification.id)
                            putExtra(DetailActivity.EXTRA_TITLE, notification.title)
                            putExtra(DetailActivity.EXTRA_TIME, notification.timeInSeconds)
                        }
                        openDetailLauncher.launch(intent)
                    },
                    loadNotifications = { onLoaded, onLoading ->
                        lifecycleScope.launch {
                            onLoading(true)
                            val items = withContext(Dispatchers.IO) {
                                loadNotifications()
                            }
                            onLoaded(items)
                            onLoading(false)
                        }
                    },
                    refreshKey = refreshKey
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        scheduler.createChannel()
        if (shouldReloadOnResume) {
            shouldReloadOnResume = false
            refreshKey += 1
        }
    }
}
