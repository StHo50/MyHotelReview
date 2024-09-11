package com.example.myhotelreview.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.myhotelreview.R
import com.example.myhotelreview.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.view.View

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var imgProfilePicture: ImageView
    private lateinit var etUserName: EditText
    private lateinit var btnSaveProfile: Button
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgProfilePicture = view.findViewById(R.id.imgProfilePicture)
        etUserName = view.findViewById(R.id.etUserName)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)

        // Get the current user ID
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Fetch user data from Firebase Realtime Database
            database.child("users").child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        etUserName.setText(user.name)
                        imageUri = Uri.parse(user.imageUrl)
                        imgProfilePicture.setImageURI(imageUri)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Error fetching user data", error.toException())
                }
            })
        } else {
            // Redirect to login if user is not authenticated
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        // Allow the user to pick an image
        imgProfilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Save user data on button click
        btnSaveProfile.setOnClickListener {
            val updatedName = etUserName.text.toString()
            val updatedImageUrl = imageUri.toString()

            if (currentUser != null) {
                val updatedUser = User(currentUser.uid, updatedName, updatedImageUrl)
                // Update user data in Firebase Realtime Database
                database.child("users").child(currentUser.uid).setValue(updatedUser)
                    .addOnSuccessListener {
                        // Successfully updated user profile
                        Log.d("ProfileFragment", "User profile updated successfully.")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ProfileFragment", "Failed to update user profile", exception)
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imgProfilePicture.setImageURI(imageUri)
        }
    }
}
