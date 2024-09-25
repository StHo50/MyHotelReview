package com.example.myhotelreview.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.myhotelreview.model.hotel.Hotel
import com.example.myhotelreview.model.hotel.HotelDao
import com.example.myhotelreview.model.hotel.HotelDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HotelRepository(context: Context) {

    private val hotelDao: HotelDao
    private val firebaseRepository = FirebaseRepository()
    private val commentRepository: CommentRepository

    init {
        val database = HotelDatabase.getDatabase(context)
        hotelDao = database.hotelDao()
        commentRepository = CommentRepository(context)
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

    suspend fun deleteAllHotelsAndComments() {
        withContext(Dispatchers.IO) {
            // Delete all hotels from Room
            deleteAllHotels()

            // Delete all hotels from Firestore
            firebaseRepository.deleteAllHotelsFromFirestore()

            // Delete all comments associated with hotels from Room and Firestore
            commentRepository.deleteAllComments()
        }
    }

    fun getHotelById(id: Int): LiveData<Hotel> {
        return hotelDao.getHotelById(id)
    }
}
