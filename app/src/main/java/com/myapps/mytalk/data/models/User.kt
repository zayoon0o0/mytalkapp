package com.myapps.mytalk.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String
)
