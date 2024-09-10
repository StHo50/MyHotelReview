package com.example.myhotelreview.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HotelDao {
    @Insert
    suspend fun insertHotel(hotel: Hotel)

    @Query("SELECT * FROM hotels")
    suspend fun getAllHotels(): List<Hotel>

    @Query("DELETE FROM hotels")
    suspend fun deleteAllHotels()

    @Query("SELECT * FROM hotels WHERE id = :id")
    fun getHotelById(id: Int): LiveData<Hotel>
}
