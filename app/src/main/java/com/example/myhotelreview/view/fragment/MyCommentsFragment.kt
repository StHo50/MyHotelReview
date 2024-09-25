package com.example.myhotelreview.view.fragment

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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myhotelreview.R
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhotelreview.adapter.CommentAdapter
import com.example.myhotelreview.model.comment.Comment
import com.example.myhotelreview.repository.UserRepository
import com.example.myhotelreview.utils.hideLoadingOverlay
import com.example.myhotelreview.utils.showLoadingOverlay
import com.example.myhotelreview.viewmodel.MyCommentsViewModel
import com.squareup.picasso.Picasso
import com.example.myhotelreview.service.ImgurAPIservice
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class MyCommentsFragment : Fragment() {

    private val viewModel: MyCommentsViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter
    private var currentUserId: String? = null
    private var commentImageUri: Uri? = null
    private var onImageSelected: ((Uri) -> Unit)? = null
    private val imgurService = ImgurAPIservice()
    private lateinit var userRepository: UserRepository

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvMyComments = view.findViewById<RecyclerView>(R.id.rvMyComments)
        rvMyComments.layoutManager = LinearLayoutManager(context)

        currentUserId = viewModel.getCurrentUserId()
        userRepository = UserRepository(requireContext())

        // Update the commentAdapter initialization to pass UserRepository and lifecycleScope
        commentAdapter = CommentAdapter(
            emptyList(),
            currentUserId = currentUserId ?: "",
            userRepository = userRepository,  // Pass userRepository
            coroutineScope = viewLifecycleOwner.lifecycleScope,  // Pass lifecycleScope
            onEditClick = { comment -> showEditCommentDialog(comment) },
            onDeleteClick = { comment -> viewModel.deleteComment(comment) },
            onCommentClick = { comment ->
                // Navigate to HotelDetailFragment with the hotelId
                val action = MyCommentsFragmentDirections.actionMyCommentsFragmentToHotelDetailFragment(comment.hotelId)
                findNavController().navigate(action)
            }
        )

        rvMyComments.adapter = commentAdapter

        viewModel.getCommentsForUser().observe(viewLifecycleOwner, { comments ->
            commentAdapter.updateComments(comments)
        })

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                view.findViewById<View>(R.id.loading_overlay)?.showLoadingOverlay()
            } else {
                view.findViewById<View>(R.id.loading_overlay)?.hideLoadingOverlay()
            }
        }
    }

    private fun showEditCommentDialog(comment: Comment) {
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
            pickImageForEdit { uri ->
                commentImageUri = uri
                Picasso.get().load(commentImageUri).into(imageView) // Update image in the dialog
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
                                viewModel.updateComment(updatedComment)
                                showToast("Comment updated successfully")
                            } else {
                                showToast("Image upload failed.")
                            }
                        }
                    }
                } ?: run {
                    val updatedComment = comment.copy(text = updatedText)
                    viewModel.updateComment(updatedComment)
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

    private fun pickImageForEdit(onImageSelected: (Uri) -> Unit) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
        this.onImageSelected = onImageSelected
    }

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
