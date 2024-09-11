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

            hotelViewModel.getHotelById(hotelId).observe(viewLifecycleOwner, Observer { hotel ->
                if (hotel != null) {
                    view.findViewById<TextView>(R.id.tvHotelName).text = hotel.name

                    view.findViewById<TextView>(R.id.tvHotelDescription).text = hotel.description

                    view.findViewById<TextView>(R.id.tvHotelLocation).text =
                        getString(R.string.label_location, hotel.location)

                    view.findViewById<TextView>(R.id.tvHotelRating).text =
                        getString(R.string.label_rating, hotel.rating.toString())

                    view.findViewById<TextView>(R.id.tvHotelStars).text =
                        getString(R.string.label_stars, hotel.stars.toString())

                    val imageView: ImageView = view.findViewById(R.id.ivHotelImage)
                    Picasso.get().load(hotel.image).into(imageView)

                    if (hotel.freeCancellation) {
                        view.findViewById<TextView>(R.id.tvFreeCancellation).apply {
                            text = getString(R.string.free_cancellation)
                            visibility = View.VISIBLE
                        }
                    }

                    if (hotel.prePayment) {
                        view.findViewById<TextView>(R.id.tvPrePayment).apply {
                            text = getString(R.string.pre_payment)
                            visibility = View.VISIBLE
                        }
                    }

                    if (hotel.breakfast) {
                        view.findViewById<TextView>(R.id.tvBreakfastIncluded).apply {
                            text = getString(R.string.breakfast_included)
                            visibility = View.VISIBLE
                        }
                    }
                } else {
                    showError("Hotel details not found.")
                }
            })
        } else {
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
