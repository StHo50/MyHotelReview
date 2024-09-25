package com.example.myhotelreview.model.hotel

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hotels")
data class Hotel(
    @PrimaryKey
    val id: Int,
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
