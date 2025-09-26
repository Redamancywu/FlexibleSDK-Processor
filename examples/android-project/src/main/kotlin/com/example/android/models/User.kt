package com.example.android.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * User entity for Room database
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Date = Date(),
    val isActive: Boolean = true
)

/**
 * User creation data class
 */
data class CreateUserData(
    val name: String,
    val email: String
)

/**
 * User update data class
 */
data class UpdateUserData(
    val name: String? = null,
    val email: String? = null,
    val isActive: Boolean? = null
)