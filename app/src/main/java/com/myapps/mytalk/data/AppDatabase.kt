package com.myapps.mytalk.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.myapps.mytalk.data.models.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}