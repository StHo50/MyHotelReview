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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhotelreview.R
import com.example.myhotelreview.model.Comment
import com.example.myhotelreview.service.ImgurAPIservice
import com.example.myhotelreview.viewmodel.HotelViewModel
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
        val etComment = view.findViewById<EditText>(R.id.etComment)
        val btnSubmitComment = view.findViewById<Button>(R.id.btnSubmitComment)
        val btnSelectImage = view.findViewById<Button>(R.id.btnSelectImage)
        val ivSelectedImage = view.findViewById<ImageView>(R.id.ivSelectedImage)

        val currentUserId = hotelViewModel.getCurrentUserId()

        commentAdapter = CommentAdapter(emptyList(), currentUserId, { comment ->
            editComment(comment)
        }, { comment ->

            deleteComment(comment)
        })


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
                    showError("Hotel details not found.")
                }
            })
            hotelViewModel.getCommentsForHotel(hotelId).observe(viewLifecycleOwner, Observer { comments ->
                println("Comments observed for hotelId $hotelId: $comments")
                commentAdapter.updateComments(comments)
            })
        } else {
            showError("Invalid hotel ID.")
        }

        btnSelectImage.setOnClickListener {
            pickImage()
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
                                    showError("Image upload failed.")
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
                showError("Comment cannot be empty.")
            }
        }

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
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
            view?.findViewById<EditText>(R.id.etComment)?.text?.clear()
            commentImageUri = null
            isCommentSubmitting = false
            showError("Comment submitted successfully.")
        }
    }

    private fun editComment(comment: Comment) {
        // AlertDialog for editing the comment
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Comment")

        // EditText for the user to modify the comment
        val input = EditText(requireContext())
        input.setText(comment.text)
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, _ ->
            val updatedText = input.text.toString()

            if (updatedText.isNotEmpty()) {
                val updatedComment = comment.copy(text = updatedText)

                // Call the ViewModel to update the comment
                hotelViewModel.updateComment(updatedComment)

                Toast.makeText(requireContext(), "Comment updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        // Set the Cancel button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss() // Dismiss the dialog without changes
        }

        // Show the dialog
        builder.show()
    }


    private fun deleteComment(comment: Comment) {
        hotelViewModel.deleteComment(comment)
        Toast.makeText(requireContext(), "Comment deleted", Toast.LENGTH_SHORT).show()
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
            commentImageUri = data.data ?: return
            val ivSelectedImage = view?.findViewById<ImageView>(R.id.ivSelectedImage)
            Picasso.get().load(commentImageUri).into(ivSelectedImage)
            ivSelectedImage?.visibility = View.VISIBLE
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


    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


}