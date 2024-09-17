package com.example.myhotelreview.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import com.example.myhotelreview.viewmodel.HotelViewModel
import com.squareup.picasso.Picasso

class HotelDetailFragment : Fragment() {

    private val hotelViewModel: HotelViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hotel_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hotelId = arguments?.let {
            HotelDetailFragmentArgs.fromBundle(it).hotelId
        }
        val rvComments = view.findViewById<RecyclerView>(R.id.rvComments)
        val etComment = view.findViewById<EditText>(R.id.etComment)
        val btnSubmitComment = view.findViewById<Button>(R.id.btnSubmitComment)
        val btnSelectImage = view.findViewById<Button>(R.id.btnSelectImage)

        commentAdapter = CommentAdapter(emptyList())
        rvComments.layoutManager = LinearLayoutManager(context)
        rvComments.adapter = commentAdapter

        if (hotelId != null) {
            hotelViewModel.getHotelById(hotelId).observe(viewLifecycleOwner, Observer { hotel ->
                if (hotel != null) {
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
                commentAdapter.updateComments(comments)
            })
        } else {
            showError("Invalid hotel ID.")
        }

        btnSelectImage.setOnClickListener {
            // Need to implement logic to select an image
        }

        btnSubmitComment.setOnClickListener {
            val commentText = etComment.text.toString()

            if (commentText.isNotEmpty()) {
                val userId = hotelViewModel.getCurrentUserId()

                // Fetch the user name
                hotelViewModel.getCurrentUserName { userName ->
                    val comment = Comment(
                        hotelId = hotelId ?: 0,
                        userId = userId,
                        userName = userName,
                        text = commentText,
                        imageUrl = selectedImageUri?.toString(),
                        timestamp = System.currentTimeMillis()
                    )

                    hotelViewModel.addComment(comment)

                    etComment.text.clear()
                    selectedImageUri = null
                }
            }
        }

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}