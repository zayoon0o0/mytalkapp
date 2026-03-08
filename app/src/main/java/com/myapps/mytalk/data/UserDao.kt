package com.myapps.mytalk.data

import androidx.room.*
import com.myapps.mytalk.data.models.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Delete
    suspend fun delete(user: User)
}
