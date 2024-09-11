package com.example.myhotelreview.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myhotelreview.R
import com.example.myhotelreview.viewmodel.ProfileViewModel
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private lateinit var editName: EditText
    private lateinit var profileImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        editName = rootView.findViewById(R.id.editName)
        profileImage = rootView.findViewById(R.id.profileImage)
        val btnSave = rootView.findViewById<Button>(R.id.btnSave)
        val btnEditImage = rootView.findViewById<Button>(R.id.btnEditImage)

        // Observe user data
        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            editName.setText(user.name)
            if (user.imageUrl.isNotEmpty()) {
                // Use Picasso to load the profile image
                Picasso.get()
                    .load(user.imageUrl)
                    .placeholder(R.drawable.default_profile_image) // Optional placeholder
                    .into(profileImage)
            }
        }

        // Fetch current user data
        profileViewModel.fetchUserData()

        btnEditImage.setOnClickListener {
            pickImageFromGallery()
        }

        btnSave.setOnClickListener {
            val updatedName = editName.text.toString()
            val updatedImageUrl = selectedImageUri?.toString() ?: "" // For now, just use the Uri as a String
            profileViewModel.updateUser(updatedName, updatedImageUrl)
        }

        return rootView
    }

    // Open image picker
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            profileImage.setImageURI(selectedImageUri)
        }
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }
}
