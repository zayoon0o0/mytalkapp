package com.myapps.mytalk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlin.system.exitProcess
class ChatActivity : AppCompatActivity() {
    init {
        System.loadLibrary("mytalk")
    }
    private lateinit var messageInput: EditText
    private lateinit var messageStream: TextView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        messageInput = findViewById(R.id.Messageinput)
        messageStream = findViewById(R.id.MessageStream)
        val app = MyTalk
        val sendBtn = findViewById<ImageButton>(R.id.Sendtxt)
        val listBtn = findViewById<Button>(R.id.list_users_button)
        val disconnectBtn = findViewById<Button>(R.id.disconnectbtn)
        val username = app.Username
        if (!app.daeomenLaunched) {
            val intent = Intent(this, ConnService::class.java)
            startService(intent)
            app.daeomenLaunched = true
        }
        MyTalk.onMessageReceived = { message -> receiveText(message) }
        Log.d("ChatActivity", "Username: $username")
        sendBtn.setOnClickListener {
            val text = messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                val byteMsg = text.toByteArray(Charsets.UTF_8)
                Thread {
                    send_message(byteMsg)
                }.start()
                receiveText("You: $text".toByteArray(Charsets.UTF_8))
                messageInput.text.clear()
            }
        }
        listBtn.setOnClickListener {
            startActivity(Intent(this, ListActivity::class.java))
        }
        disconnectBtn.setOnClickListener {
            val intent = Intent(this, ConnService::class.java)
            stopService(intent)
            app.daeomenLaunched = false
            app.Connected = false
            app.Username = ""
            exitProcess(0)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        MyTalk.onMessageReceived = null
    }
    private external fun send_message(message: ByteArray)
    private external fun recieve_message()
    private fun receiveText(message: ByteArray) {
        runOnUiThread {
            if (message.isEmpty()) return@runOnUiThread
            val text = String(message, Charsets.UTF_8)
            messageStream.append("$text\n")
            Log.d("ChatActivity", "Received: $text")
        }
    }
}
