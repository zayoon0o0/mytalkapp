package com.myapps.mytalk

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    init {
        System.loadLibrary("mytalk")
    }

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.submit_username)
        val usernameText = findViewById<EditText>(R.id.username_input)
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val myValue = sharedPref.getString("my_key", null)
        if (myValue == null) {
            Log.d("MyApp", "Value is null")
            if (MyTalk.Connected) {
                    startActivity(Intent(this, ChatActivity::class.java))
                    finish()
            }
            btn.setOnClickListener {
                val username = usernameText.text.toString()
                MyTalk.Username = username
                val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString("my_key", username)
                    apply()
                }
                Log.d("MainActivity", "Username1: $username")
                startActivity(Intent(this, ChatActivity::class.java))
                finish()
            }
        } else {
            Log.d("MyApp", "Stored value: $myValue")
            MyTalk.Username = myValue
            if (MyTalk.Connected) {
                startActivity(Intent(this, ChatActivity::class.java))
                finish()
            }
            if (MyTalk.Username.isNotEmpty()) {
                startActivity(Intent(this, ChatActivity::class.java))
                finish()
            }
            startActivity(Intent(this, ChatActivity::class.java))
            finish()
        }


    }
}