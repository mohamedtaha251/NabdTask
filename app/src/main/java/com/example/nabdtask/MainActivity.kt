package com.example.nabdtask

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.nabdtask.ui.theme.NabdTaskTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class MainActivity : ComponentActivity() {
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            NabdTaskTheme {
                NotificationsScreen(
                    onCancelAll = { notifications ->
                        NotificationScheduler.cancelAll(this, notifications)
                    },
                    onOpenDetail = { notification ->
                        val intent = Intent(this, DetailActivity::class.java).apply {
                            putExtra(DetailActivity.EXTRA_ID, notification.id)
                            putExtra(DetailActivity.EXTRA_TITLE, notification.title)
                            putExtra(DetailActivity.EXTRA_TIME, notification.timeInSeconds)
                        }
                        startActivity(intent)
                    },
                    loadNotifications = { onLoaded, onLoading ->
                        lifecycleScope.launch {
                            onLoading(true)
                            val items = withContext(Dispatchers.IO) {
                                parseNotifications()
                            }
                            onLoaded(items)
                            onLoading(false)
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NotificationScheduler.createChannel(this)
    }

    private fun parseNotifications(): List<LocalNotification> {
        val items = mutableListOf<LocalNotification>()
        val input = assets.open("notifications.xml")
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(input, null)
        var event = parser.eventType
        var id: Int? = null
        var title: String? = null
        var time: Long? = null
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "id" -> id = parser.nextText().trim().toIntOrNull()
                    "title" -> title = parser.nextText().trim()
                    "timeInSeconds" -> time = parser.nextText().trim().toLongOrNull()
                }
            } else if (event == XmlPullParser.END_TAG && parser.name == "notification") {
                val safeId = id
                val safeTitle = title
                val safeTime = time
                if (safeId != null && safeTitle != null && safeTime != null) {
                    items.add(LocalNotification(safeId, safeTitle, safeTime))
                }
                id = null
                title = null
                time = null
            }
            event = parser.next()
        }
        input.close()
        return items
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsScreen(
    onCancelAll: (List<LocalNotification>) -> Unit,
    onOpenDetail: (LocalNotification) -> Unit,
    loadNotifications: ((List<LocalNotification>) -> Unit, (Boolean) -> Unit) -> Unit
) {
    var items by remember { mutableStateOf<List<LocalNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val refresh = {
        loadNotifications({ loaded -> items = loaded }, { loading -> isLoading = loading })
    }
    LaunchedEffect(Unit) { refresh() }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refresh()
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
                    IconButton(onClick = { onCancelAll(items) }) {
                        Text("Cancel All")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
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
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items) { item ->
                        Text(
                            text = item.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenDetail(item) }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}