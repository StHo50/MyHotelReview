package com.example.myhotelreview.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myhotelreview.model.Comment
import com.example.myhotelreview.model.CommentRepository
import com.example.myhotelreview.model.FirebaseRepository
import com.example.myhotelreview.model.Hotel
import com.example.myhotelreview.model.HotelRepository
import com.example.myhotelreview.model.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HotelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HotelRepository = HotelRepository(application)
    private val commentsRepository: CommentRepository = CommentRepository(application)
    private val userRepository: UserRepository = UserRepository(application)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _hotels = MutableLiveData<List<Hotel>>()
    val hotels: LiveData<List<Hotel>> get() = _hotels

    fun fetchHotels() {
        _isLoading.value = true
        viewModelScope.launch {
            var hotelList = repository.getAllHotels() // Fetch hotels from Room


            if (hotelList.isNotEmpty()) {
                _hotels.postValue(hotelList)

                // Sync with Firestore to check if there is more updated data
                val firestoreHasUpdatedData = repository.syncHotelsWithFirestore()

                if (firestoreHasUpdatedData) {
                    // If Firestore has updated data, fetch it again from Room
                    hotelList = repository.getAllHotels()
                    _hotels.postValue(hotelList)  // Update UI with the latest data from Firestore
                }

            } else {
                val success = repository.syncHotelsWithFirestore() // If Room is empty, sync directly with Firestore

                if (success) {
                    val syncedHotelList = repository.getAllHotels() // Fetch from Room after syncing with Firestore
                    _hotels.postValue(syncedHotelList)
                } else {
                    insertDummyHotels() // If Firestore is also empty, insert dummy data
                }
            }
            _isLoading.value = false
        }
    }

    fun insertDummyHotels() {

        viewModelScope.launch {

            //repository.deleteAllHotels()

            val dummyHotels = listOf(
                Hotel(id = 1, name = "Hilton Hotel", description = "Luxurious stay in the heart of New York.", location = "New York", image = "https://cf.bstatic.com/xdata/images/hotel/max1024x768/516222741.jpg?k=e35a1f06b6598e0b99fd0bda1af04c424ca107906c2fd8c196030a0400d88093&o=&hp=1", stars = 5, rating = 9.0f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(id = 2, name = "Marriott", description = "Comfort and convenience in Los Angeles.", location = "Los Angeles", image = "https://cf.bstatic.com/xdata/images/hotel/square240/528908425.webp?k=40444b0d9750d99141642efa164fb48e789434e807543b00866b7e29dd5f9048&o=", stars = 4, rating = 8.5f, freeCancellation = true, prePayment = false, breakfast = true),
                Hotel(id = 3, name = "Sheraton", description = "Affordable and cozy stay in Chicago.", location = "Chicago", image = "https://cf.bstatic.com/xdata/images/hotel/max1024x768/435768323.jpg?k=657883b7ff684533cd9397b851bb83a5e7a005337f75621cd5c9966e5505bce2&o=&hp=1", stars = 3, rating = 7.5f, freeCancellation = true, prePayment = false, breakfast = false),
                Hotel(id = 4, name = "Ritz-Carlton", description = "A luxurious experience in San Francisco.", location = "San Francisco", image = "https://cf.bstatic.com/xdata/images/hotel/square240/527870382.webp?k=7a6b327a7a03520c66ac0f3c53585a40d6258449a39f006621a5b274503ffbec&o=", stars = 5, rating = 9.5f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(id = 5, name = "Four Seasons", description = "A premium experience in Miami.", location = "Miami", image = "https://cf.bstatic.com/xdata/images/hotel/square240/270155310.webp?k=5fe9c46b1b882de6f00aafe2c602afd372c7e40c510013c6bd9df5677ffb0e80&o=", stars = 5, rating = 9.5f, freeCancellation = true, prePayment = true, breakfast = true),
                Hotel(id = 6, name = "Holiday Inn", description = "Simple but comfortable.", location = "Houston", image = "https://cf.bstatic.com/xdata/images/hotel/square240/50756453.webp?k=0741110f13ca19d364906be8bd187cfbf30f2a0e344487f87d96cdaa0eaba4fa&o=", stars = 3, rating = 7.0f, freeCancellation = false, prePayment = true, breakfast = false),
                Hotel(id = 7, name = "Hyatt Regency", description = "Perfect for a weekend getaway.", location = "Seattle", image = "https://cf.bstatic.com/xdata/images/hotel/square240/464997594.webp?k=143ba7eb0bbb40ce8c4b986c8bdf1c71df6dc9e5fdac7c5b5488508924e79d90&o=", stars = 4, rating = 8.7f, freeCancellation = true, prePayment = false, breakfast = true),
                Hotel(id = 8, name = "Budget Inn", description = "Budget-friendly with great amenities.", location = "Las Vegas", image = "https://cf.bstatic.com/xdata/images/hotel/square240/587251146.webp?k=f46657e0ca4128d606dae6c204963a2decb8f7d0fc5ee0e50c3a1e1e5eb12b8d&o=", stars = 2, rating = 6.5f, freeCancellation = true, prePayment = false, breakfast = false),
                Hotel(id = 9, name = "Radisson Blu", description = "Stylish and modern.", location = "Boston", image = "https://cf.bstatic.com/xdata/images/hotel/square240/550218320.webp?k=52d0adc7f165d36f18701660b5ba57ae66ac562b2dc58092e23d6837d2752813&o=", stars = 4, rating = 8.3f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(id = 10, name = "Waldorf Astoria", description = "Luxury at its best.", location = "Washington, D.C.", image = "https://cf.bstatic.com/xdata/images/hotel/square240/95765235.webp?k=d9dfa173e1b3c345275bea5d896123890e1e7e341a5666fc92dba1e7967269af&o=", stars = 5, rating = 9.8f, freeCancellation = true, prePayment = true, breakfast = true),
                Hotel(id = 11, name = "Shangri-La", description = "A peaceful retreat.", location = "Hawaii", image = "https://cf.bstatic.com/xdata/images/hotel/square240/478321961.webp?k=8b6b9a2bb026ed8710d36dd9a8a892491c7c503b63739134eac1f7c49b78e690&o=", stars = 4, rating = 8.2f, freeCancellation = false, prePayment = false, breakfast = true),
                Hotel(id = 12, name = "Best Western", description = "Comfortable and convenient.", location = "Dallas", image = "https://cf.bstatic.com/xdata/images/hotel/square240/471551245.webp?k=b72bd70e043de34d95d9c764858c5d8ddb6590afed431ea038b084f955243a2f&o=", stars = 3, rating = 7.4f, freeCancellation = true, prePayment = true, breakfast = false),
                Hotel(id = 13, name = "Kimpton Hotel", description = "A small, charming hotel.", location = "Philadelphia", image = "https://cf.bstatic.com/xdata/images/hotel/square240/42214560.webp?k=c2a38f3e60807c12bf42887fbf03f913f77e66f3511805c652a8a454ab3b433b&o=", stars = 4, rating = 8.6f, freeCancellation = true, prePayment = false, breakfast = true),
                Hotel(id = 14, name = "La Quinta Inn", description = "Great value for money.", location = "Phoenix", image = "https://cf.bstatic.com/xdata/images/hotel/square240/481070665.webp?k=2bfd334cf17af07b8e380d5a16511aa8bff464b38e20f75762f827601d744943&o=", stars = 3, rating = 7.2f, freeCancellation = false, prePayment = true, breakfast = false),
                Hotel(id = 15, name = "Fairmont Hotel", description = "Located near major attractions.", location = "San Diego", image = "https://cf.bstatic.com/xdata/images/hotel/square240/258316153.webp?k=27ea82f75495ef2036a21f7a2d78f2195aa8b1f55a571f58ecf1175f2639f639&o=", stars = 4, rating = 8.9f, freeCancellation = true, prePayment = true, breakfast = true),
                Hotel(id = 16, name = "Omni Hotel", description = "An elegant and sophisticated stay.", location = "Atlanta", image = "https://cf.bstatic.com/xdata/images/hotel/max1024x768/384063427.jpg?k=5fd5e84e8b6ef408da6edcea447a4d0a2619af4b69c474bf22c072430e4bf3e8&o=&hp=1", stars = 5, rating = 9.2f, freeCancellation = false, prePayment = false, breakfast = true),
                Hotel(id = 17, name = "Courtyard by Marriott", description = "Affordable luxury.", location = "Austin", image = "https://cf.bstatic.com/xdata/images/hotel/square240/486740856.webp?k=66efd12346c43bb2b9993eb540fa7987d2751534ddfba8cd5bf197eb17e62645&o=", stars = 3, rating = 7.7f, freeCancellation = true, prePayment = true, breakfast = true),
                Hotel(id = 18, name = "The Westin", description = "Ideal for business travelers.", location = "Denver", image = "https://cf.bstatic.com/xdata/images/hotel/square240/456059135.webp?k=e2329dc89ba08d5be0e9d4c5cf6cb893fd40f4ed521a26eb324bb60a4c6425ef&o=", stars = 4, rating = 8.4f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(id = 19, name = "The Langham", description = "Modern amenities in a historic setting.", location = "Dallas", image = "https://cf.bstatic.com/xdata/images/hotel/square240/139931345.webp?k=2cca17ea1068539c4f58aeb14874b719f7abe3b219017f443a41a7329feddefd&o=", stars = 4, rating = 8.1f, freeCancellation = true, prePayment = false, breakfast = true),
                Hotel(id = 20, name = "The Peninsula", description = "A relaxing getaway.", location = "Orlando", image = "https://cf.bstatic.com/xdata/images/hotel/square240/493257528.webp?k=c34216c59d29c5e0f403dc485e1539891084d8b39f2c0e257248d6d03354768a&o=", stars = 5, rating = 9.1f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(id = 21, name = "The Ritz", description = "Simple elegance.", location = "New Orleans", image = "https://cf.bstatic.com/xdata/images/hotel/square240/547250714.webp?k=7cbbb6b8204acb071e8098494b4c86576fcffc66279b84f2e3778d6b9ab7cd6c&o=", stars = 3, rating = 7.6f, freeCancellation = true, prePayment = false, breakfast = false),
                Hotel(id = 22, name = "Comfort Inn", description = "Comfortable and affordable.", location = "Detroit", image = "https://cf.bstatic.com/xdata/images/hotel/square240/581411110.webp?k=2c3ecfeb15cadf26f9a0859b92d0e05ad3cac1416765394d72fa366ea3a0f1b7&o=", stars = 2, rating = 6.8f, freeCancellation = false, prePayment = true, breakfast = false),
                Hotel(id = 23, name = "W Hotel", description = "A modern masterpiece.", location = "San Antonio", image = "https://cf.bstatic.com/xdata/images/hotel/square240/274240203.webp?k=300d1f04f8a0d91021e526d3f34b844dcac9e99a1ac74a61d1e96d5398d5eb1d&o=", stars = 5, rating = 9.4f, freeCancellation = true, prePayment = true, breakfast = true),
                Hotel(id = 24, name = "Royal Tulip Hotel", description = "Budget-friendly comfort.", location = "Cleveland", image = "https://cf.bstatic.com/xdata/images/hotel/square240/116393405.webp?k=bd42fba45d3a3d37d9e787867b8038f6e6b30360743fcb6614523f8aa196b17a&o=", stars = 2, rating = 6.2f, freeCancellation = true, prePayment = false, breakfast = false),
                Hotel(id = 25, name = "The Drake", description = "Relax in style.", location = "Seattle", image = "https://cf.bstatic.com/xdata/images/hotel/square240/483769869.webp?k=f6d271fe301a8d7761d7af5a912bad9bf7ad0b76495583293906485c99433ecb&o=", stars = 4, rating = 8.8f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(id = 26, name = "The Standard", description = "A welcoming atmosphere.", location = "Charlotte", image = "https://cf.bstatic.com/xdata/images/hotel/square240/574316786.webp?k=fb8b523b7086704a66dd20dc44f8ba940c612814901c5d7b3bedd6b97012cf9f&o=", stars = 3, rating = 7.3f, freeCancellation = true, prePayment = false, breakfast = true),
                Hotel(id = 27, name = "The Roosevelt", description = "Luxury and convenience.", location = "New Orleans", image = "https://cf.bstatic.com/xdata/images/hotel/square240/246596022.webp?k=92f564599c733a0bd62be44e717212e5fa6a6579845155e2a29e41f672611916&o=", stars = 5, rating = 9.7f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(id = 28, name = "Quality Inn", description = "Simple and clean.", location = "Baltimore", image = "https://cf.bstatic.com/xdata/images/hotel/square240/263662264.webp?k=195f589b0ee977c22edefe50a4ea47b6b30d0586f825cf7224fabc4cd46c4de6&o=", stars = 3, rating = 7.1f, freeCancellation = true, prePayment = false, breakfast = false),
                Hotel(id = 29, name = "The Plaza", description = "Affordable and cheerful.", location = "Nashville", image = "https://cf.bstatic.com/xdata/images/hotel/square240/484051068.webp?k=5c63db659969c1d25c5be4f879d7535e6a732e601197572bb34274b0236b9f90&o=", stars = 2, rating = 6.9f, freeCancellation = false, prePayment = true, breakfast = false),
                Hotel(id = 30, name = "The Omni Grove Park Inn", description = "Close to major attractions.", location = "Asheville", image = "https://cf.bstatic.com/xdata/images/hotel/square240/320805814.webp?k=cc4348180ba06779a006a2f2affb2df6ae16bd2b12645001ea6418b2204603fe&o=", stars = 4, rating = 8.3f, freeCancellation = true, prePayment = true, breakfast = true)
            )

            dummyHotels.forEach {
                repository.insertHotel(it) // Insert into both Room and Firestore
            }
            _hotels.postValue(dummyHotels)
        }
    }

    fun resetHotelsAndComments() {
        _isLoading.value = true
        viewModelScope.launch {
            // Delete all hotels and comments from ROOM and Firestore
            repository.deleteAllHotelsAndComments()

            // Insert new dummy hotels after deletion
            insertDummyHotels()

            _isLoading.value = false
        }
    }

    fun getHotelById(id: Int): LiveData<Hotel> {
        return repository.getHotelById(id)
    }

    fun addComment(comment: Comment) {
        viewModelScope.launch {
            // Check if comment already exists before adding
            val existingComments = commentsRepository.getCommentsForHotelSync(comment.hotelId)
            val isDuplicate = existingComments.any { it.text == comment.text }

            if (!isDuplicate) {
                commentsRepository.insertComment(comment)
            } else {
                println("Duplicate comment detected, skipping insertion.")
            }
        }
    }

    fun updateComment(comment: Comment) {
        viewModelScope.launch {
            commentsRepository.updateComment(comment)
        }
    }

    fun deleteComment(comment: Comment) {
        viewModelScope.launch {
            commentsRepository.deleteComment(comment)
        }
    }


    fun getCommentsForHotel(hotelId: Int): LiveData<List<Comment>> {
        return commentsRepository.getCommentsForHotel(hotelId)
    }

    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    fun getCurrentUserName(onComplete: (String) -> Unit) {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            userRepository.getUserByIdLive(userId).observeForever { user ->
                if (user != null) {
                    onComplete(user.name)
                } else {
                    onComplete("Anonymous")
                }
            }
        } else {
            onComplete("Anonymous")
        }
    }

}
