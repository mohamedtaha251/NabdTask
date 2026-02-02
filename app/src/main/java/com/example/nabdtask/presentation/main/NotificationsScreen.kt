package com.example.nabdtask.presentation.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.nabdtask.domain.model.LocalNotification
import com.example.nabdtask.presentation.main.ui.NotificationsLandscape
import com.example.nabdtask.presentation.main.ui.NotificationsPortrait

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onCancelAll: () -> Unit,
    onOpenDetail: (LocalNotification) -> Unit,
    loadNotifications: ((List<LocalNotification>) -> Unit, (Boolean) -> Unit) -> Unit,
    refreshKey: Int
) {
    val notificationsSaver = listSaver<List<LocalNotification>, Any>(
        save = { list -> list.map { listOf(it.id, it.title, it.timeInSeconds) } },
        restore = { restored ->
            restored.map { item ->
                val data = item as List<*>
                LocalNotification(data[0] as Int, data[1] as String, data[2] as Long)
            }
        }
    )
    var items by rememberSaveable(stateSaver = notificationsSaver) {
        mutableStateOf<List<LocalNotification>>(emptyList())
    }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var hasLoaded by rememberSaveable { mutableStateOf(false) }
    var wasInBackground by rememberSaveable { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val refresh = {
        loadNotifications({ loaded -> items = loaded }, { loading -> isLoading = loading })
        hasLoaded = true
    }
    LaunchedEffect(refreshKey) {
        if (!hasLoaded || refreshKey > 0) {
            refresh()
        }
    }
    DisposableEffect(Unit) {
        val lifecycleOwner = ProcessLifecycleOwner.get()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (wasInBackground) {
                    refresh()
                    wasInBackground = false
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                wasInBackground = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                actions = {
                    TextButton(onClick = { onCancelAll() }) {
                        Text("Cancel All")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Loading...", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                NotificationsContent(
                    items = items,
                    isLandscape = isLandscape,
                    onOpenDetail = onOpenDetail
                )
            }
        }
    }
}

@Composable
private fun NotificationsContent(
    items: List<LocalNotification>,
    isLandscape: Boolean,
    onOpenDetail: (LocalNotification) -> Unit
) {
    if (items.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "No notifications yet", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }
    if (isLandscape) {
        NotificationsLandscape(items = items, onOpenDetail = onOpenDetail)
    } else {
        NotificationsPortrait(items = items, onOpenDetail = onOpenDetail)
    }
}

