package com.example.myhotelreview.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.myhotelreview.R
import com.example.myhotelreview.viewmodel.HotelViewModel
import com.squareup.picasso.Picasso

class HotelDetailFragment : Fragment() {

    private val hotelViewModel: HotelViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hotel_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch the hotel ID passed as an argument
        val hotelId = arguments?.let {
            HotelDetailFragmentArgs.fromBundle(it).hotelId
        }

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
        } else {
            showError("Invalid hotel ID.")
        }

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}