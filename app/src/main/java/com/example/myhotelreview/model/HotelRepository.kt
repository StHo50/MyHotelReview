package com.example.myhotelreview.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HotelRepository(context: Context) {

    private val hotelDao: HotelDao

    init {
        val database = HotelDatabase.getDatabase(context)
        hotelDao = database.hotelDao()
    }

    suspend fun insertHotel(hotel: Hotel) {
        withContext(Dispatchers.IO) {
            hotelDao.insertHotel(hotel)
        }
    }

    suspend fun getAllHotels(): List<Hotel> {
        return withContext(Dispatchers.IO) {
            hotelDao.getAllHotels()
        }
    }
}
