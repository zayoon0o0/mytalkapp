package com.myapps.mytalk.data.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myapps.mytalk.R

class UserAdapter(
    val users: ArrayList<String>,
    val userClicked: (String) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.useritem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.userNameTextView.text = user
        holder.messageButton.setOnClickListener {
            userClicked(user)
        }
    }

    override fun getItemCount() = users.size

    fun clearUsers() {
        users.clear()
        notifyDataSetChanged()
    }

    fun addUser(user: String) {
        users.add(user)
        notifyItemInserted(users.size - 1)
    }

    fun removeUser(user: String) {
        val position = users.indexOf(user)
        if (position != -1) {
            users.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameTextView: TextView = view.findViewById(R.id.UserName)
        val messageButton: TextView = view.findViewById(R.id.messagebtn)
    }
}
