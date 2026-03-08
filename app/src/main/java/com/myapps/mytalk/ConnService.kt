package com.myapps.mytalk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
class ConnService : Service() {
    private lateinit var app: MyTalk
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var isConnectedLocally = false
    override fun onCreate() {
        super.onCreate()
        app = application as MyTalk
        app.service = this
        Log.d(TAG, "Service created")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val username = MyTalk.Username
        createNotificationChannel()
        val s = MyTalk.daeomenLaunched
        if (!s) {
            MyTalk.daeomenLaunched = true
        }
        if (!MyTalk.Connected && !isConnectedLocally) {
            Log.d(TAG, "Connecting to server as $username")
            serviceScope.launch {
                connectToServer(username.toByteArray())
                MyTalk.Connected = true
                isConnectedLocally = true
                Log.d(TAG, "Successfully connected to server as $username")
            }
        }
        return START_STICKY
    }

    fun receiveText(data: ByteArray) {
        Log.d(TAG, "Notification data received: ${String(data)}")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("New message")
            .setContentText("Received: ${String(data)}")
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .build()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(MESSAGE_NOTIFICATION_ID, notification)
        MyTalk.onMessageReceived?.invoke(data)
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroying. Cancelling jobs.")
        close()
        serviceJob.cancel()
        MyTalk.Connected = false
        isConnectedLocally = false
    }
    private external fun connectToServer(username: ByteArray)
    private external fun recieveMessage()
    private external fun close()
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "MyTalk Connection Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
    companion object {
        private const val TAG = "ConnService"
        private const val CHANNEL_ID = "mytalk_conn_channel"
        private const val FOREGROUND_SERVICE_ID = 1
        private const val MESSAGE_NOTIFICATION_ID = 2
    }
}
