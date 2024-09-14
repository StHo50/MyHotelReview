package com.example.myhotelreview.model

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HotelRepository(context: Context) {

    private val hotelDao: HotelDao
    private val firebaseRepository = FirebaseRepository()

    init {
        val database = HotelDatabase.getDatabase(context)
        hotelDao = database.hotelDao()
    }

    suspend fun syncHotelsWithFirestore(): Boolean {
        return try {
            val hotels = firebaseRepository.getAllHotelsFromFirestore()
            if (hotels != null && hotels.isNotEmpty()) {
                hotelDao.deleteAllHotels()
                hotels.forEach { hotelDao.insertHotel(it) }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun insertHotel(hotel: Hotel) {
        withContext(Dispatchers.IO) {
            hotelDao.insertHotel(hotel) // Insert into Room
            firebaseRepository.insertHotelToFirestore(hotel) // Insert into Firestore
        }
    }

    suspend fun getAllHotels(): List<Hotel> {
        return hotelDao.getAllHotels() // Fetch from Room for offline access
    }

    suspend fun deleteAllHotels() {
        hotelDao.deleteAllHotels()
    }

    fun getHotelById(id: Int): LiveData<Hotel> {
        return hotelDao.getHotelById(id)
    }
}
