package com.example.myhotelreview.view

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

        // The function below inserts the dummy hotels data, we need to activate it only once in order to not get duplicated data every run, that's why it's a comment.
        //hotelViewModel.insertDummyHotels()

        hotelViewModel.fetchHotels()

        hotelViewModel.hotels.observe(viewLifecycleOwner, Observer { hotels ->
            hotelAdapter = HotelAdapter(hotels, { hotel ->
                val fragment = HotelDetailFragment.newInstance(hotel.id)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }, requireContext())
            rvHotels.adapter = hotelAdapter
        })
    }
}
