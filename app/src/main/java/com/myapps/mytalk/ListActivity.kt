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
            adapter.addUser(MyTalk.Username)
            val usersBytes = listUsers()
            if (usersBytes != null) {
                val usersString = usersBytes.toString()
                val usersList = usersString.lines().filter { it.isNotBlank() }
                runOnUiThread {
                    adapter.clearUsers()
                    usersList.forEach { username ->
                        adapter.addUser(username)
                    }
                    adapter.notifyDataSetChanged()
                    Log.d("ListActivity", "Users fetched successfully")
                }
            }  else {
                Log.e("ListActivity", "Failed to fetch user list")
            }
        }.start()
        listbtn.setOnClickListener {
            Thread {
                val usersArray = listUsers()
                if (usersArray != null) {
                    val usersList = usersArray.filter { it.isNotBlank() }
                    runOnUiThread {
                        adapter.clearUsers()
                        adapter.addUser(MyTalk.Username)
                        usersList.forEach { username ->
                            if (username != MyTalk.Username) {
                                adapter.addUser(username)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
                Log.d("ListActivity", "Users fetched successfully")
            }.start()
        }
        gobtn.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }
    private external fun listUsers(): Array<String>?

}
