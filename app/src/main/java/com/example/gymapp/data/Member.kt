package com.example.gymapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val photoPath: String? = null,
    val joinDate: Long,
    val feeAmount: Double,
    val feeDueDate: Long
)
