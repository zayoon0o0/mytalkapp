package com.myapps.mytalk.data

import androidx.room.Room
import com.myapps.mytalk.MyTalk
import com.myapps.mytalk.data.models.User

class Repository(app: MyTalk) {
    val db = Room.databaseBuilder(
        app.applicationContext,
        AppDatabase::class.java,
        "myMessages"
    ).build()
    private val userDao = db.userDao()
    suspend fun getAllUsers() = userDao.getAll()
    suspend fun insertUser(user: User) = userDao.insert(user)
    suspend fun deleteUser(user: User) = userDao.delete(user)
    suspend fun clearUsers() {
        val users = getAllUsers()
        for (user in users) {
            deleteUser(user)
        }
    }

}