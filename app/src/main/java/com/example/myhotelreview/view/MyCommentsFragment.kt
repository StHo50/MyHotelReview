package com.example.myhotelreview.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myhotelreview.R
import com.example.myhotelreview.viewmodel.LoginViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhotelreview.model.Comment
import com.example.myhotelreview.utils.hideLoadingOverlay
import com.example.myhotelreview.utils.showLoadingOverlay
import com.example.myhotelreview.viewmodel.MyCommentsViewModel


class MyCommentsFragment : Fragment() {

    private val viewModel: MyCommentsViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter
    private var currentUserId: String? = null

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

        // Initialize the CommentAdapter with the necessary callbacks
        commentAdapter = CommentAdapter(
            emptyList(),
            currentUserId = currentUserId ?: "",
            onEditClick = { comment ->
                // Handle edit comment logic here
                showEditCommentDialog(comment)
            },
            onDeleteClick = { comment ->
                // Handle delete comment logic here
                viewModel.deleteComment(comment)
            }
        )
        rvMyComments.adapter = commentAdapter

        // Observe the comments and update the RecyclerView
        viewModel.getCommentsForUser().observe(viewLifecycleOwner, { comments ->
            commentAdapter.updateComments(comments)
        })

        // Observe the loading state and show/hide the spinner accordingly
        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                view.findViewById<View>(R.id.loading_overlay)?.showLoadingOverlay()
            } else {
                view.findViewById<View>(R.id.loading_overlay)?.hideLoadingOverlay()
            }
        })
    }

    // Function to handle showing the edit dialog
    private fun showEditCommentDialog(comment: Comment) {
        // Create an AlertDialog builder
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Comment")

        // Create an EditText to display the existing comment text
        val input = EditText(requireContext())
        input.setText(comment.text)  // Set the current comment text in the EditText
        builder.setView(input)  // Add the EditText to the dialog

        // Set up the Save button
        builder.setPositiveButton("Save") { dialog, _ ->
            val updatedText = input.text.toString()

            // Check if the updated text is not empty
            if (updatedText.isNotEmpty()) {
                // Update the comment with the new text
                val updatedComment = comment.copy(text = updatedText)

                // Call ViewModel to update the comment
                viewModel.updateComment(updatedComment)

                // Optionally, show a success message
                Toast.makeText(requireContext(), "Comment updated", Toast.LENGTH_SHORT).show()
            } else {
                // Optionally, show an error message if the text is empty
                Toast.makeText(requireContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        // Set up the Cancel button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        // Show the dialog
        builder.show()
    }
}
