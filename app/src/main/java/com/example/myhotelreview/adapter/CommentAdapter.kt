package com.example.myhotelreview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myhotelreview.R
import com.example.myhotelreview.model.comment.Comment
import com.example.myhotelreview.repository.UserRepository
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentAdapter(
    private var comments: List<Comment>,
    private val currentUserId: String,
    private val userRepository: UserRepository,
    private val coroutineScope: CoroutineScope,
    private val onEditClick: (Comment) -> Unit,
    private val onDeleteClick: (Comment) -> Unit,
    private val onCommentClick: (Comment) -> Unit
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
            val ivProfileImage = itemView.findViewById<ImageView>(R.id.ivProfileImage)
            val tvUserName = itemView.findViewById<TextView>(R.id.tvUserName)
            val tvComment = itemView.findViewById<TextView>(R.id.tvCommentText)
            val ivCommentImage = itemView.findViewById<ImageView>(R.id.ivCommentImage)
            val tvCommentDate = itemView.findViewById<TextView>(R.id.tvCommentDate)
            val btnEditComment = itemView.findViewById<ImageButton>(R.id.btnEditComment)
            val btnDeleteComment = itemView.findViewById<ImageButton>(R.id.btnDeleteComment)

            itemView.setOnClickListener {
                onCommentClick(comment)  // Trigger the comment click
            }
            // Set comment text without the user name
            tvComment.text = comment.text

            // Fetch user information asynchronously
            coroutineScope.launch {
                val user = userRepository.getUserById(comment.userId)
                withContext(Dispatchers.Main) {
                    user?.let {
                        tvUserName.text = it.name
                        val profileImageUrl = it.imageUrl
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Picasso.get().load(profileImageUrl).placeholder(R.drawable.default_profile_image).into(ivProfileImage)
                        } else {
                            ivProfileImage.setImageResource(R.drawable.default_profile_image)
                        }
                    }
                }
            }

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

                btnEditComment.setOnClickListener {
                    onEditClick(comment)
                }

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
