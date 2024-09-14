package com.example.myhotelreview.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myhotelreview.model.FirebaseRepository
import com.example.myhotelreview.model.Hotel
import com.example.myhotelreview.model.HotelRepository
import kotlinx.coroutines.launch

class HotelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HotelRepository = HotelRepository(application)

    private val _hotels = MutableLiveData<List<Hotel>>()
    val hotels: LiveData<List<Hotel>> get() = _hotels

    fun fetchHotels() {
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
        }
    }


    fun insertDummyHotels() {

        viewModelScope.launch {

            //repository.deleteAllHotels()

            val dummyHotels = listOf(
                Hotel(name = "Hotel A", description = "A cozy place to stay.", location = "City A", image = "https://cf.bstatic.com/xdata/images/hotel/max1024x768/516222741.jpg?k=e35a1f06b6598e0b99fd0bda1af04c424ca107906c2fd8c196030a0400d88093&o=&hp=1", stars = 5, rating = 9.0f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(name = "Hotel B", description = "Luxurious and spacious.", location = "City B", image = "https://cf.bstatic.com/xdata/images/hotel/square240/528908425.webp?k=40444b0d9750d99141642efa164fb48e789434e807543b00866b7e29dd5f9048&o=", stars = 4, rating = 8.5f, freeCancellation = true, prePayment = false, breakfast = true),
                Hotel(name = "Hotel C", description = "Affordable and comfortable.", location = "City C", image = "https://cf.bstatic.com/xdata/images/hotel/square240/242357546.webp?k=9bce9d45c9df284948f846311bdb74fb4e", stars = 3, rating = 7.5f, freeCancellation = true, prePayment = false, breakfast = false),
                Hotel(name = "Hotel D", description = "A hidden gem.", location = "City D", image = "https://cf.bstatic.com/xdata/images/hotel/square240/527870382.webp?k=7a6b327a7a03520c66ac0f3c53585a40d6258449a39f006621a5b274503ffbec&o=", stars = 4, rating = 8.0f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(name = "Hotel E", description = "Great location with excellent service.", location = "City E", image = "https://cf.bstatic.com/xdata/images/hotel/square240/270155310.webp?k=5fe9c46b1b882de6f00aafe2c602afd372c7e40c510013c6bd9df5677ffb0e80&o=", stars = 5, rating = 9.5f, freeCancellation = true, prePayment = false, breakfast = true),
                Hotel(name = "Hotel F", description = "Simple but comfortable.", location = "City F", image = "https://cf.bstatic.com/xdata/images/hotel/square240/50756453.webp?k=0741110f13ca19d364906be8bd187cfbf30f2a0e344487f87d96cdaa0eaba4fa&o=", stars = 3, rating = 7.0f, freeCancellation = false, prePayment = true, breakfast = false),
                Hotel(name = "Hotel G", description = "Perfect for a weekend getaway.", location = "City G", image = "https://cf.bstatic.com/xdata/images/hotel/square240/464997594.webp?k=143ba7eb0bbb40ce8c4b986c8bdf1c71df6dc9e5fdac7c5b5488508924e79d90&o=", stars = 4, rating = 8.7f, freeCancellation = true, prePayment = false, breakfast = true),
                Hotel(name = "Hotel H", description = "Budget-friendly with great amenities.", location = "City H", image = "https://cf.bstatic.com/xdata/images/hotel/square240/587251146.webp?k=f46657e0ca4128d606dae6c204963a2decb8f7d0fc5ee0e50c3a1e1e5eb12b8d&o=", stars = 2, rating = 6.5f, freeCancellation = true, prePayment = false, breakfast = false),
                Hotel(name = "Hotel I", description = "Stylish and modern.", location = "City I", image = "https://cf.bstatic.com/xdata/images/hotel/square240/550218320.webp?k=52d0adc7f165d36f18701660b5ba57ae66ac562b2dc58092e23d6837d2752813&o=", stars = 4, rating = 8.3f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(name = "Hotel J", description = "Luxury at its best.", location = "City J", image = "https://cf.bstatic.com/xdata/images/hotel/square240/95765235.webp?k=d9dfa173e1b3c345275bea5d896123890e1e7e341a5666fc92dba1e7967269af&o=", stars = 5, rating = 9.8f, freeCancellation = true, prePayment = true, breakfast = true),
                Hotel(name = "Hotel K", description = "A peaceful retreat.", location = "City K", image = "https://cf.bstatic.com/xdata/images/hotel/square240/478321961.webp?k=8b6b9a2bb026ed8710d36dd9a8a892491c7c503b63739134eac1f7c49b78e690&o=", stars = 4, rating = 8.2f, freeCancellation = false, prePayment = false, breakfast = true),
                Hotel(name = "Hotel L", description = "Comfortable and convenient.", location = "City L", image = "https://cf.bstatic.com/xdata/images/hotel/square240/471551245.webp?k=b72bd70e043de34d95d9c764858c5d8ddb6590afed431ea038b084f955243a2f&o=", stars = 3, rating = 7.4f, freeCancellation = true, prePayment = true, breakfast = false),
                Hotel(name = "Hotel M", description = "A small, charming hotel.", location = "City M", image = "https://cf.bstatic.com/xdata/images/hotel/square240/42214560.webp?k=c2a38f3e60807c12bf42887fbf03f913f77e66f3511805c652a8a454ab3b433b&o=", stars = 4, rating = 8.6f, freeCancellation = true, prePayment = false, breakfast = true),
                Hotel(name = "Hotel N", description = "Great value for money.", location = "City N", image = "https://cf.bstatic.com/xdata/images/hotel/square240/481070665.webp?k=2bfd334cf17af07b8e380d5a16511aa8bff464b38e20f75762f827601d744943&o=", stars = 3, rating = 7.2f, freeCancellation = false, prePayment = true, breakfast = false),
                Hotel(name = "Hotel O", description = "Located near major attractions.", location = "City O", image = "https://cf.bstatic.com/xdata/images/hotel/square240/258316153.webp?k=27ea82f75495ef2036a21f7a2d78f2195aa8b1f55a571f58ecf1175f2639f639&o=", stars = 4, rating = 8.9f, freeCancellation = true, prePayment = true, breakfast = true),
                Hotel(name = "Hotel P", description = "An elegant and sophisticated stay.", location = "City P", image = "https://cf.bstatic.com/xdata/images/hotel/square240/589789757.webp?k=3ddea4981c6b49e99e3e73e3a99878305ed42a2a398b22a8c9e8fc5fa991df6d&o=", stars = 5, rating = 9.2f, freeCancellation = false, prePayment = false, breakfast = true),
                Hotel(name = "Hotel Q", description = "Affordable luxury.", location = "City Q", image = "https://cf.bstatic.com/xdata/images/hotel/square240/486740856.webp?k=66efd12346c43bb2b9993eb540fa7987d2751534ddfba8cd5bf197eb17e62645&o=", stars = 3, rating = 7.7f, freeCancellation = true, prePayment = true, breakfast = true),
                Hotel(name = "Hotel R", description = "Ideal for business travelers.", location = "City R", image = "https://cf.bstatic.com/xdata/images/hotel/square240/456059135.webp?k=e2329dc89ba08d5be0e9d4c5cf6cb893fd40f4ed521a26eb324bb60a4c6425ef&o=", stars = 4, rating = 8.4f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(name = "Hotel S", description = "Modern amenities in a historic setting.", location = "City S", image = "https://cf.bstatic.com/xdata/images/hotel/square240/139931345.webp?k=2cca17ea1068539c4f58aeb14874b719f7abe3b219017f443a41a7329feddefd&o=", stars = 4, rating = 8.1f, freeCancellation = true, prePayment = false, breakfast = true),
                Hotel(name = "Hotel T", description = "A relaxing getaway.", location = "City T", image = "https://cf.bstatic.com/xdata/images/hotel/square240/493257528.webp?k=c34216c59d29c5e0f403dc485e1539891084d8b39f2c0e257248d6d03354768a&o=", stars = 5, rating = 9.1f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(name = "Hotel U", description = "Simple elegance.", location = "City U", image = "https://cf.bstatic.com/xdata/images/hotel/square240/547250714.webp?k=7cbbb6b8204acb071e8098494b4c86576fcffc66279b84f2e3778d6b9ab7cd6c&o=", stars = 3, rating = 7.6f, freeCancellation = true, prePayment = false, breakfast = false),
                Hotel(name = "Hotel V", description = "Comfortable and affordable.", location = "City V", image = "https://cf.bstatic.com/xdata/images/hotel/square240/581411110.webp?k=2c3ecfeb15cadf26f9a0859b92d0e05ad3cac1416765394d72fa366ea3a0f1b7&o=", stars = 2, rating = 6.8f, freeCancellation = false, prePayment = true, breakfast = false),
                Hotel(name = "Hotel W", description = "A modern masterpiece.", location = "City W", image = "https://cf.bstatic.com/xdata/images/hotel/square240/274240203.webp?k=300d1f04f8a0d91021e526d3f34b844dcac9e99a1ac74a61d1e96d5398d5eb1d&o=", stars = 5, rating = 9.4f, freeCancellation = true, prePayment = true, breakfast = true),
                Hotel(name = "Hotel X", description = "Budget-friendly comfort.", location = "City X", image = "https://cf.bstatic.com/xdata/images/hotel/square240/116393405.webp?k=bd42fba45d3a3d37d9e787867b8038f6e6b30360743fcb6614523f8aa196b17a&o=", stars = 2, rating = 6.2f, freeCancellation = true, prePayment = false, breakfast = false),
                Hotel(name = "Hotel Y", description = "Relax in style.", location = "City Y", image = "https://cf.bstatic.com/xdata/images/hotel/square240/483769869.webp?k=f6d271fe301a8d7761d7af5a912bad9bf7ad0b76495583293906485c99433ecb&o=", stars = 4, rating = 8.8f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(name = "Hotel Z", description = "A welcoming atmosphere.", location = "City Z", image = "https://cf.bstatic.com/xdata/images/hotel/square240/574316786.webp?k=fb8b523b7086704a66dd20dc44f8ba940c612814901c5d7b3bedd6b97012cf9f&o=", stars = 3, rating = 7.3f, freeCancellation = true, prePayment = false, breakfast = true),
                Hotel(name = "Hotel AA", description = "Luxury and convenience.", location = "City AA", image = "https://cf.bstatic.com/xdata/images/hotel/square240/246596022.webp?k=92f564599c733a0bd62be44e717212e5fa6a6579845155e2a29e41f672611916&o=", stars = 5, rating = 9.7f, freeCancellation = false, prePayment = true, breakfast = true),
                Hotel(name = "Hotel BB", description = "Simple and clean.", location = "City BB", image = "https://cf.bstatic.com/xdata/images/hotel/square240/263662264.webp?k=195f589b0ee977c22edefe50a4ea47b6b30d0586f825cf7224fabc4cd46c4de6&o=", stars = 3, rating = 7.1f, freeCancellation = true, prePayment = false, breakfast = false),
                Hotel(name = "Hotel CC", description = "Affordable and cheerful.", location = "City CC", image = "https://cf.bstatic.com/xdata/images/hotel/square240/484051068.webp?k=5c63db659969c1d25c5be4f879d7535e6a732e601197572bb34274b0236b9f90&o=", stars = 2, rating = 6.9f, freeCancellation = false, prePayment = true, breakfast = false),
                Hotel(name = "Hotel DD", description = "Close to major attractions.", location = "City DD", image = "https://cf.bstatic.com/xdata/images/hotel/square240/320805814.webp?k=cc4348180ba06779a006a2f2affb2df6ae16bd2b12645001ea6418b2204603fe&o=", stars = 4, rating = 8.3f, freeCancellation = true, prePayment = true, breakfast = true)
            )

            dummyHotels.forEach {
                repository.insertHotel(it) // Insert into both Room and Firestore
            }
            _hotels.postValue(dummyHotels)
        }
    }

    fun getHotelById(id: Int): LiveData<Hotel> {
        return repository.getHotelById(id)
    }

}
