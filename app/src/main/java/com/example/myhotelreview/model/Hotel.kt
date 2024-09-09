package com.example.myhotelreview.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hotels")
data class Hotel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val location: String,
    val image: String,
    val stars: Int,
    val rating: Float,
    val freeCancellation: Boolean,
    val prePayment: Boolean,
    val breakfast: Boolean
)
