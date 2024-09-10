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

        val hotelId = arguments?.getInt(ARG_HOTEL_ID)
        if (hotelId != null) {
            // Observe the hotel detail
            hotelViewModel.getHotelById(hotelId).observe(viewLifecycleOwner, Observer { hotel ->
                if (hotel != null) {
                    // Populate the UI with the hotel details
                    view.findViewById<TextView>(R.id.tvHotelName).text = hotel.name
                    view.findViewById<TextView>(R.id.tvHotelLocation).text = hotel.location
                    view.findViewById<TextView>(R.id.tvHotelRating).text = hotel.rating.toString()
                    view.findViewById<TextView>(R.id.tvHotelStars).text = hotel.stars.toString()
                    view.findViewById<TextView>(R.id.tvHotelDescription).text = hotel.description

                    // Load image with Picasso
                    val imageView: ImageView = view.findViewById(R.id.ivHotelImage)
                    Picasso.get().load(hotel.image).into(imageView)
                } else {
                    // Handle the case where the hotel was not found
                    showError("Hotel details not found.")
                }
            })
        } else {
            // Handle the case where hotelId is null
            showError("Invalid hotel ID.")
        }

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }



    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    }


    companion object {
        private const val ARG_HOTEL_ID = "hotel_id"

        fun newInstance(hotelId: Int): HotelDetailFragment {
            val fragment = HotelDetailFragment()
            val args = Bundle()
            args.putInt(ARG_HOTEL_ID, hotelId)
            fragment.arguments = args
            return fragment
        }
    }
}
