package com.myapps.mytalk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

class DMActivity : AppCompatActivity() {
    init {
        System.loadLibrary("mytalk")
    }

    private lateinit var username: String
    private lateinit var messageInput: EditText
    private lateinit var messageStream: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dmactivity)
        username = intent.getStringExtra("user") ?: ""
        messageInput = findViewById(R.id.Messageinput)
        messageStream = findViewById(R.id.MessageStream)
        val sendBtn = findViewById<ImageButton>(R.id.Sendtxt)
        val goBackBtn = findViewById<Button>(R.id.gobackbtn)
        lifecycleScope.launch(Dispatchers.IO) {
                while (isActive) {
                    recieve_message()
                    recieve_message()
                    recieve_message()
                }
        }
        sendBtn.setOnClickListener {
            val messageStr = messageInput.text.toString().trim()
            if (messageStr.isNotEmpty()) {
                val combined = "$username\n$messageStr"
                Thread {
                    send_primessage(combined.toByteArray(Charsets.UTF_8))
                }.start()
                Log.d("DMActivity", "Sent: $combined")
                receiveText("You: $messageStr\n".toByteArray())
                messageInput.text.clear()
            }
        }
        goBackBtn.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
            finish()
        }
    }
    private external fun recieve_message()
    private external fun send_primessage(message: ByteArray)
    fun receiveText(message: ByteArray) {
        runOnUiThread {
            if (message.isEmpty()) return@runOnUiThread
            val msgStr = String(message)
            Log.d("DMActivity", "Received: $msgStr")
            when {
                msgStr.startsWith("You: ") -> {
                    messageStream.append(msgStr)
                    return@runOnUiThread
                }
                msgStr.startsWith("[Private] $username\n") -> {
                    Toast.makeText(this, "New private message", Toast.LENGTH_SHORT).show()
                    var cleanMsg = msgStr.removePrefix("[Private] $username\n")
                    cleanMsg = ("$username: $cleanMsg\n")
                    messageStream.append(cleanMsg)
                    return@runOnUiThread
                }
                else -> {
                    return@runOnUiThread
                }
            }
        }
    }
}
