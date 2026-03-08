package com.myapps.mytalk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myapps.mytalk.data.adapters.UserAdapter
class ListActivity : AppCompatActivity() {
    init {
        System.loadLibrary("mytalk")
    }
    private lateinit var adapter: UserAdapter
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list)
        val gobtn = findViewById<Button>(R.id.GoToChat)
        val listbtn = findViewById<Button>(R.id.listbtn)
        val usersList = findViewById<RecyclerView>(R.id.user_list)
        usersList.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(arrayListOf()) { user ->
            startActivity(Intent(this, DMActivity::class.java).apply { putExtra("user", user) })
        }
        usersList.adapter = adapter
        Thread {
            val usersArray = listUsers()
            if (usersArray != null) {
                runOnUiThread {
                    adapter.clearUsers()
                    usersArray.filter { it.isNotBlank() }.forEach { adapter.addUser(it) }
                    adapter.notifyDataSetChanged()
                }
            }
        }.start()
        listbtn.setOnClickListener {
            listbtn.isEnabled = false
            Thread {
                MyTalk.fetchingList = true
                val usersArray = listUsers()
                MyTalk.fetchingList = false
                runOnUiThread {
                    if (usersArray != null) {
                        adapter.clearUsers()
                        usersArray.filter { it.isNotBlank() }.forEach { adapter.addUser(it) }
                        adapter.notifyDataSetChanged()
                    }
                    listbtn.isEnabled = true
                }
            }.start()
        }
        gobtn.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }
    private external fun listUsers(): Array<String>?

}
