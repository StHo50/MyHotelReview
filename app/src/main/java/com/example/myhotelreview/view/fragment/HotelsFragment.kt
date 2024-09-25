package com.example.myhotelreview.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhotelreview.R
import com.example.myhotelreview.viewmodel.HotelViewModel
import androidx.navigation.fragment.findNavController
import com.example.myhotelreview.adapter.HotelAdapter
import com.example.myhotelreview.utils.hideLoadingOverlay
import com.example.myhotelreview.utils.showLoadingOverlay

class HotelsFragment : Fragment() {

    private val hotelViewModel: HotelViewModel by viewModels()
    private lateinit var hotelAdapter: HotelAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hotels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvHotels = view.findViewById<RecyclerView>(R.id.rvHotels)
        rvHotels.layoutManager = LinearLayoutManager(context)

        hotelViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                view.findViewById<View>(R.id.loading_overlay)?.showLoadingOverlay()
            } else {
                view.findViewById<View>(R.id.loading_overlay)?.hideLoadingOverlay()
            }
        }

        // The function below inserts the dummy hotels data, we need to activate it only once in order to not get duplicated data every run, that's why it's a comment.
        // hotelViewModel.insertDummyHotels()

        // Call this function once to reset hotels and comments, then comment it out after first use.
        //hotelViewModel.resetHotelsAndComments()

        hotelViewModel.fetchHotels()

        hotelViewModel.hotels.observe(viewLifecycleOwner, Observer { hotels ->
            hotelAdapter = HotelAdapter(hotels, { hotel ->
                val action = HotelsFragmentDirections.actionHotelsFragmentToHotelDetailFragment(hotel.id)
                findNavController().navigate(action)
            }, requireContext())
            rvHotels.adapter = hotelAdapter
        })
    }
}

