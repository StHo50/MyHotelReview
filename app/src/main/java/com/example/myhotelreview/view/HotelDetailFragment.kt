package com.example.myhotelreview.view

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhotelreview.R
import com.example.myhotelreview.model.Comment
import com.example.myhotelreview.model.UserRepository
import com.example.myhotelreview.service.ImgurAPIservice
import com.example.myhotelreview.viewmodel.HotelViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class HotelDetailFragment : Fragment() {

    private val hotelViewModel: HotelViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter
    private var commentImageUri: Uri? = null
    private val imgurService = ImgurAPIservice()
    private var isCommentSubmitting = false
    private var editingComment: Comment? = null // To track the comment being edited
    private lateinit var userRepository: UserRepository
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hotel_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userRepository = UserRepository(requireContext())

        val loadingOverlay = view.findViewById<FrameLayout>(R.id.loading_overlay)

        hotelViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                loadingOverlay.visibility = View.VISIBLE
            } else {
                loadingOverlay.visibility = View.GONE
            }
        })

        val hotelId = arguments?.let {
            HotelDetailFragmentArgs.fromBundle(it).hotelId
        }
        println("Current hotelId: $hotelId")

        val rvComments = view.findViewById<RecyclerView>(R.id.rvComments)
        val etComment = view.findViewById<TextInputEditText>(R.id.etComment)
        val btnSubmitComment = view.findViewById<MaterialButton>(R.id.btnSubmitComment)
        val etCommentLayout = view.findViewById<TextInputLayout>(R.id.etCommentLayout)
        val ivSelectedImage = view.findViewById<ImageView>(R.id.ivSelectedImage)
        val currentUserId = hotelViewModel.getCurrentUserId()

        commentAdapter = CommentAdapter(
            emptyList(),
            currentUserId,
            userRepository,
            viewLifecycleOwner.lifecycleScope,
            { comment -> editComment(comment) },
            { comment -> deleteComment(comment) },
            {}
        )

        rvComments.layoutManager = LinearLayoutManager(context)
        rvComments.adapter = commentAdapter

        if (hotelId != null) {
            hotelViewModel.getHotelById(hotelId).observe(viewLifecycleOwner, Observer { hotel ->
                if (hotel != null) {
                    println("Hotel loaded with ID: ${hotel.id}")
                    view.findViewById<TextView>(R.id.tvHotelName).text = hotel.name
                    view.findViewById<TextView>(R.id.tvHotelDescription).text = hotel.description
                    view.findViewById<TextView>(R.id.tvHotelLocation).text = getString(R.string.label_location, hotel.location)
                    view.findViewById<TextView>(R.id.tvHotelRating).text = getString(R.string.label_rating, hotel.rating.toString())
                    view.findViewById<TextView>(R.id.tvHotelStars).text = getString(R.string.label_stars, hotel.stars.toString())

                    val imageView: ImageView = view.findViewById(R.id.ivHotelImage)
                    Picasso.get().load(hotel.image).into(imageView)

                    // Show additional details if available
                    if (hotel.freeCancellation) {
                        view.findViewById<TextView>(R.id.tvFreeCancellation).visibility = View.VISIBLE
                    }
                    if (hotel.prePayment) {
                        view.findViewById<TextView>(R.id.tvPrePayment).visibility = View.VISIBLE
                    }
                    if (hotel.breakfast) {
                        view.findViewById<TextView>(R.id.tvBreakfastIncluded).visibility = View.VISIBLE
                    }
                } else {
                    showToast("Hotel details not found.")
                }
            })
            hotelViewModel.getCommentsForHotel(hotelId).observe(viewLifecycleOwner, Observer { comments ->
                println("Comments observed for hotelId $hotelId: $comments")
                commentAdapter.updateComments(comments)
            })
        } else {
            showToast("Invalid hotel ID.")
        }

        etCommentLayout.setEndIconOnClickListener {
            pickImage { uri ->
                commentImageUri = uri
                Picasso.get().load(commentImageUri).into(ivSelectedImage) // Update image preview
                ivSelectedImage.visibility = View.VISIBLE // Ensure the image is visible
            }
        }
        btnSubmitComment.setOnClickListener {
            val commentText = etComment.text.toString()

            if (commentText.isNotEmpty() && !isCommentSubmitting) {
                isCommentSubmitting = true

                val userId = hotelViewModel.getCurrentUserId()

                hotelViewModel.getCurrentUserName { userName ->

                    commentImageUri?.let { uri ->
                        val imageFile = convertUriToFile(uri)
                        imageFile?.let {
                            imgurService.uploadImage(it) { success, imageUrl ->
                                if (success && imageUrl != null) {
                                    if (isAdded && activity != null) {
                                        activity?.runOnUiThread {
                                            submitComment(hotelId ?: 0, userId, userName, commentText, imageUrl)
                                        }
                                    }
                                } else {
                                    showToast("Image upload failed.")
                                    isCommentSubmitting = false
                                }
                            }
                        }
                    } ?: run {
                        if (isAdded && activity != null) {
                            activity?.runOnUiThread {
                                submitComment(hotelId ?: 0, userId, userName, commentText, null)
                            }
                        }
                    }
                }
            } else {
                showToast("Comment cannot be empty.")
            }
        }
    }

    private fun submitComment(hotelId: Int, userId: String, userName: String, text: String, imageUrl: String?) {
        println("Submitting comment for hotelId: $hotelId")
        val comment = Comment(
            hotelId = hotelId,
            userId = userId,
            userName = userName,
            text = text,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )

        hotelViewModel.addComment(comment)

        requireActivity().runOnUiThread {
            // Clearing the text input
            view?.findViewById<TextInputEditText>(R.id.etComment)?.text?.clear()

            // Clearing and hiding the image preview
            commentImageUri = null
            val ivSelectedImage = view?.findViewById<ImageView>(R.id.ivSelectedImage)
            ivSelectedImage?.setImageDrawable(null)
            ivSelectedImage?.visibility = View.GONE

            isCommentSubmitting = false

            showToast("Comment submitted successfully.")
        }
    }

    private fun editComment(comment: Comment) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Comment")

        val dialogLayout = LinearLayout(requireContext())
        dialogLayout.orientation = LinearLayout.VERTICAL

        val input = EditText(requireContext())
        input.setText(comment.text)
        dialogLayout.addView(input)

        val imageView = ImageView(requireContext())
        if (!comment.imageUrl.isNullOrEmpty()) {
            Picasso.get().load(comment.imageUrl).into(imageView)
        }
        dialogLayout.addView(imageView)

        val btnSelectImage = Button(requireContext())
        btnSelectImage.text = "Change Image"
        dialogLayout.addView(btnSelectImage)

        builder.setView(dialogLayout)

        btnSelectImage.setOnClickListener {
            // Triggering image picker and update the ImageView when a new image is selected
            pickImage { uri ->
                commentImageUri = uri
                Picasso.get().load(commentImageUri).into(imageView)
            }
        }

        builder.setPositiveButton("Save") { dialog, _ ->
            val updatedText = input.text.toString()
            if (updatedText.isNotEmpty()) {
                commentImageUri?.let { uri ->
                    val imageFile = convertUriToFile(uri)
                    imageFile?.let {
                        imgurService.uploadImage(it) { success, imageUrl ->
                            if (success && imageUrl != null) {
                                val updatedComment = comment.copy(text = updatedText, imageUrl = imageUrl)
                                hotelViewModel.updateComment(updatedComment)
                                showToast("Comment updated successfully")
                            } else {
                                showToast("Image upload failed.")
                            }
                        }
                    }
                } ?: run {
                    val updatedComment = comment.copy(text = updatedText)
                    hotelViewModel.updateComment(updatedComment)
                    showToast("Comment updated successfully")
                }
            } else {
                showToast("Comment cannot be empty")
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun deleteComment(comment: Comment) {
        hotelViewModel.deleteComment(comment)
        Toast.makeText(requireContext(), "Comment deleted", Toast.LENGTH_SHORT).show()
    }

    private fun pickImage(onImageSelected: (Uri) -> Unit) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
        this.onImageSelected = onImageSelected
    }

    private var onImageSelected: ((Uri) -> Unit)? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            selectedImageUri?.let {
                onImageSelected?.invoke(it)
            }
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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}