package com.example.myhotelreview.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun registerUser(
        email: String,
        password: String,
        name: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val user = hashMapOf(
                            "email" to email,
                            "name" to name
                        )
                        firestore.collection("Users").document(userId)
                            .set(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    onComplete(true, null)
                                } else {
                                    onComplete(false, dbTask.exception?.message)
                                }
                            }
                    } else {
                        onComplete(false, "User ID not found")
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun getUserByIdFromFirestore(userId: String, onComplete: (User?) -> Unit) {
        firestore.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val email = document.getString("email") ?: ""
                    val imageUrl = document.getString("imageUrl")

                    val user = User(
                        id = userId,
                        name = name,
                        email = email,
                        imageUrl = imageUrl
                    )
                    onComplete(user)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun updateUserProfile(user: User, onComplete: (Boolean, String?) -> Unit) {
        val userId = user.id
        val userUpdates = mutableMapOf<String, Any>(
            "name" to user.name,
            "email" to user.email
        )

        if (!user.imageUrl.isNullOrEmpty()) {
            userUpdates["imageUrl"] = user.imageUrl
        }

        firestore.collection("Users").document(userId)
            .update(userUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun getAllHotelsFromFirestore(): List<Hotel>? {
        return try {
            val result = firestore.collection("Hotels").get().await()
            val hotels = result.map { document ->
                Hotel(
                    id = document.getLong("id")?.toInt() ?: 0,
                    name = document.getString("name") ?: "",
                    description = document.getString("description") ?: "",
                    location = document.getString("location") ?: "",
                    image = document.getString("image") ?: "",
                    stars = document.getLong("stars")?.toInt() ?: 0,
                    rating = document.getDouble("rating")?.toFloat() ?: 0f,
                    freeCancellation = document.getBoolean("freeCancellation") ?: false,
                    prePayment = document.getBoolean("prePayment") ?: false,
                    breakfast = document.getBoolean("breakfast") ?: false
                )
            }
            hotels
        } catch (e: Exception) {
            null
        }
    }

    suspend fun insertHotelToFirestore(hotel: Hotel): Boolean {
        return try {
            val hotelData = hashMapOf(
                "id" to hotel.id,
                "name" to hotel.name,
                "description" to hotel.description,
                "location" to hotel.location,
                "image" to hotel.image,
                "stars" to hotel.stars,
                "rating" to hotel.rating,
                "freeCancellation" to hotel.freeCancellation,
                "prePayment" to hotel.prePayment,
                "breakfast" to hotel.breakfast
            )
            // Use the hotel id as the Firestore document id
            firestore.collection("Hotels").document(hotel.id.toString()).set(hotelData).await()
            true
        } catch (e: Exception) {
            false
        }
    }


    fun insertCommentToFirestore(comment: Comment, onComplete: (Boolean) -> Unit) {
        val commentData = hashMapOf(
            "hotelId" to comment.hotelId,
            "userId" to comment.userId,
            "userName" to comment.userName,
            "text" to comment.text,
            "imageUrl" to comment.imageUrl,
            "timestamp" to comment.timestamp
        )

        firestore.collection("Comments").add(commentData)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun updateCommentInFirestore(comment: Comment, onComplete: (Boolean) -> Unit) {
        firestore.collection("Comments")
            .whereEqualTo("hotelId", comment.hotelId)
            .whereEqualTo("timestamp", comment.timestamp)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // There should be exactly one comment with this hotelId and timestamp
                    val document = querySnapshot.documents[0]
                    firestore.collection("Comments")
                        .document(document.id)
                        .set(comment)
                        .addOnCompleteListener { task ->
                            onComplete(task.isSuccessful)
                        }
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }


    fun deleteCommentFromFirestore(comment: Comment, onComplete: (Boolean) -> Unit) {
        firestore.collection("Comments")
            .whereEqualTo("hotelId", comment.hotelId)
            .whereEqualTo("timestamp", comment.timestamp)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // There should be exactly one comment with this hotelId and timestamp
                    val document = querySnapshot.documents[0]
                    firestore.collection("Comments")
                        .document(document.id)
                        .delete()
                        .addOnCompleteListener { task ->
                            onComplete(task.isSuccessful)
                        }
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }


    fun getCommentsForHotel(hotelId: Int, onComplete: (List<Comment>?) -> Unit) {
        firestore.collection("Comments")
            .whereEqualTo("hotelId", hotelId)
            .get()
            .addOnSuccessListener { result ->
                val comments = result.map { document ->
                    Comment(
                        id = document.getLong("id")?.toInt() ?: 0,
                        hotelId = hotelId,
                        userId = document.getString("userId") ?: "",
                        userName = document.getString("userName") ?: "",
                        text = document.getString("text") ?: "",
                        imageUrl = document.getString("imageUrl"),
                        timestamp = document.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                }
                onComplete(comments)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    suspend fun deleteAllHotelsFromFirestore() {
        val hotels = firestore.collection("Hotels").get().await()
        for (hotel in hotels) {
            firestore.collection("Hotels").document(hotel.id).delete().await()
        }
    }

    // Function to delete all comments from Firestore
    suspend fun deleteAllCommentsFromFirestore() {
        val comments = firestore.collection("Comments").get().await()
        for (comment in comments) {
            firestore.collection("Comments").document(comment.id).delete().await()
        }
    }

}

