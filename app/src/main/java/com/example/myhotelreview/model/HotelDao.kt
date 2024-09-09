package com.example.myhotelreview.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HotelDao {
    @Insert
    suspend fun insertHotel(hotel: Hotel)

    @Query("SELECT * FROM hotels")
    suspend fun getAllHotels(): List<Hotel>
}
