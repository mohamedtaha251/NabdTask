package com.example.nabdtask.data.repository

import com.example.nabdtask.domain.model.LocalNotification
import com.example.nabdtask.domain.repository.NotificationsRepository
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL

class AssetsNotificationsRepository : NotificationsRepository {
    override suspend fun loadNotifications(): List<LocalNotification> {
        val items = mutableListOf<LocalNotification>()
        val url = URL("https://lynxapp.com/localalerts.php")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            requestMethod = "GET"
        }
        try {
            connection.inputStream.use { input ->
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
            }
        } finally {
            connection.disconnect()
        }
        return items
    }
}
