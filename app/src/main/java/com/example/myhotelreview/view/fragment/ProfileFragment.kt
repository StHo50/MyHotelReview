package com.example.myhotelreview.view.fragment

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.myhotelreview.model.user.User
import com.example.myhotelreview.service.ImgurAPIservice
import com.example.myhotelreview.utils.hideLoadingOverlay
import com.example.myhotelreview.utils.showLoadingOverlay
import com.example.myhotelreview.viewmodel.ProfileViewModel
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var lockIcon: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var editNameEditText: EditText
    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private val viewModel: ProfileViewModel by viewModels()
    private var profileImageUri: Uri? = null
    private var currentUser: User? = null
    private val imgurService = ImgurAPIservice()

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                view.findViewById<View>(R.id.loading_overlay)?.showLoadingOverlay()
            } else {
                view.findViewById<View>(R.id.loading_overlay)?.hideLoadingOverlay()
            }
        }

        profileImageView = view.findViewById(R.id.profile_imageview)
        lockIcon = view.findViewById(R.id.lock_icon)
        userNameTextView = view.findViewById(R.id.user_name_textview)
        editNameEditText = view.findViewById(R.id.edit_name_edittext)
        editButton = view.findViewById(R.id.edit_button)
        saveButton = view.findViewById(R.id.save_button)

        saveButton.visibility = View.GONE
        removeImageClickListener()

        setupUserProfile()

        editButton.setOnClickListener {
            enableEditMode()
        }

        saveButton.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun setupUserProfile() {
        viewModel.getUserProfile().observe(viewLifecycleOwner) { user ->
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
        }
    }

    private fun enableEditMode() {
        editNameEditText.isEnabled = true
        editButton.visibility = View.GONE
        saveButton.visibility = View.VISIBLE
        lockIcon.visibility = View.GONE
        addImageClickListener()
    }

    private fun removeImageClickListener() {
        profileImageView.setOnClickListener(null)
    }

    private fun addImageClickListener() {
        profileImageView.setOnClickListener {
            pickImage()
        }
    }

    private var isProfileUpdating = false

    private fun saveProfileChanges() {
        if (isProfileUpdating) return
        isProfileUpdating = true

        val newName = editNameEditText.text.toString()
        if (newName.isBlank()) {
            Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            isProfileUpdating = false
            return
        }

        currentUser?.let { user ->
            val updatedUser = user.copy(
                name = newName,
                imageUrl = profileImageUri?.toString() ?: user.imageUrl
            )

            profileImageUri?.let { uri ->
                val imageFile = convertUriToFile(uri)
                imageFile?.let {
                    imgurService.uploadImage(it) { success, imageUrl ->
                        if (success && imageUrl != null) {
                            val updatedUserWithImage = updatedUser.copy(imageUrl = imageUrl)
                            activity?.runOnUiThread {
                                viewModel.saveUserProfile(updatedUserWithImage)
                                updateProfile(newName, imageUrl)
                            }
                        } else {
                            showError("Image upload failed")
                        }
                    }
                } ?: run {
                    showError("Image file conversion failed.")
                    isProfileUpdating = false
                }
            } ?: run {
                activity?.runOnUiThread {
                    viewModel.saveUserProfile(updatedUser)
                    updateProfile(newName, updatedUser.imageUrl)
                }
            }
        }
    }

    private fun updateProfile(newName: String, imageUrl: String?) {
        userNameTextView.text = newName
        Picasso.get().load(imageUrl).into(profileImageView)
        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()

        editNameEditText.isEnabled = false
        editButton.visibility = View.VISIBLE
        saveButton.visibility = View.GONE
        lockIcon.visibility = View.VISIBLE
        removeImageClickListener()

        isProfileUpdating = false
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        isProfileUpdating = false
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            profileImageUri = data.data ?: return
            Picasso.get().load(profileImageUri).into(profileImageView)
        }
    }

    private fun convertUriToFile(uri: Uri): File? {
        val contentResolver: ContentResolver = requireContext().contentResolver
        val tempFile = File.createTempFile("temp_image", ".jpg", requireContext().cacheDir)

        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

