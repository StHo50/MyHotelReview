package com.example.myhotelreview.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myhotelreview.R
import com.example.myhotelreview.databinding.ItemHotelBinding
import com.example.myhotelreview.model.Comment
import com.example.myhotelreview.model.Hotel
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class CommentAdapter(
    private var comments: List<Comment>,
    private val currentUserId: String,
    private val onEditClick: (Comment) -> Unit,
    private val onDeleteClick: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(comment: Comment) {
            val tvComment = itemView.findViewById<TextView>(R.id.tvCommentText)
            val ivCommentImage = itemView.findViewById<ImageView>(R.id.ivCommentImage)
            val tvCommentDate = itemView.findViewById<TextView>(R.id.tvCommentDate)
            val btnEditComment = itemView.findViewById<ImageButton>(R.id.btnEditComment)
            val btnDeleteComment = itemView.findViewById<ImageButton>(R.id.btnDeleteComment)

            tvComment.text = "${comment.userName}: ${comment.text}"

            if (comment.imageUrl != null) {
                ivCommentImage.visibility = View.VISIBLE
                Picasso.get().load(comment.imageUrl).into(ivCommentImage)
            } else {
                ivCommentImage.visibility = View.GONE
            }

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = Date(comment.timestamp)
            tvCommentDate.text = sdf.format(date)

            // Show edit and delete buttons only for the authenticated user's comments
            if (comment.userId == currentUserId) {
                btnEditComment.visibility = View.VISIBLE
                btnDeleteComment.visibility = View.VISIBLE

                // Handle edit button click
                btnEditComment.setOnClickListener {
                    onEditClick(comment)
                }

                // Handle delete button click
                btnDeleteComment.setOnClickListener {
                    onDeleteClick(comment)
                }
            } else {
                btnEditComment.visibility = View.GONE
                btnDeleteComment.visibility = View.GONE
            }
        }
    }
}
