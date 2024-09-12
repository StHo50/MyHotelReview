package com.example.myhotelreview.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myhotelreview.R
import com.example.myhotelreview.model.User
import com.example.myhotelreview.viewmodel.ProfileViewModel
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var editNameEditText: EditText
    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private val viewModel: ProfileViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var currentUser: User? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImageView = view.findViewById(R.id.profile_imageview)
        userNameTextView = view.findViewById(R.id.user_name_textview)
        editNameEditText = view.findViewById(R.id.edit_name_edittext)
        editButton = view.findViewById(R.id.edit_button)
        saveButton = view.findViewById(R.id.save_button)

        setupUserProfile()

        editButton.setOnClickListener {
            enableEditMode()
        }

        saveButton.setOnClickListener {
            saveProfileChanges()
        }

        profileImageView.setOnClickListener {
            pickImageFromGallery()
        }

        return view
    }

    private fun setupUserProfile() {

        viewModel.getUserProfile().observe(viewLifecycleOwner, { user ->
            user?.let {
                currentUser = it
                userNameTextView.text = it.name
                editNameEditText.setText(it.name)

                val imageUrl = it.imageUrl

                if (!imageUrl.isNullOrEmpty()) {

                    try {
                        Picasso.get().load(imageUrl).into(profileImageView)
                    } catch (e: Exception) {
                        e.printStackTrace()

                        Picasso.get().load(R.drawable.default_profile_image).into(profileImageView)
                    }
                } else {
                    Picasso.get().load(R.drawable.default_profile_image).into(profileImageView)
                }
            }
        })
    }



    private fun enableEditMode() {
        editNameEditText.isEnabled = true
        editButton.visibility = View.GONE
        saveButton.visibility = View.VISIBLE
    }

    private fun saveProfileChanges() {
        val newName = editNameEditText.text.toString()
        if (newName.isBlank()) {
            Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser?.let { user ->
            val updatedUser = user.copy(
                name = newName,
                imageUrl = selectedImageUri?.toString() ?: user.imageUrl
            )

            viewModel.saveUserProfile(updatedUser)

            userNameTextView.text = newName
            if (!updatedUser.imageUrl.isNullOrEmpty()) {
                Picasso.get().load(updatedUser.imageUrl).into(profileImageView)
            } else {
                Picasso.get().load(R.drawable.default_profile_image).into(profileImageView)
            }

            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()

            editNameEditText.isEnabled = false
            editButton.visibility = View.VISIBLE
            saveButton.visibility = View.GONE
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            Picasso.get().load(selectedImageUri).into(profileImageView)
        }
    }
}
