package com.example.myhotelreview.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.myhotelreview.R
import com.example.myhotelreview.viewmodel.HotelViewModel

class HotelsFragment : Fragment() {

    private val hotelViewModel: HotelViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hotels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvHotels = view.findViewById<TextView>(R.id.tvHotels)

        hotelViewModel.insertDummyHotels()

        hotelViewModel.fetchHotels()

        hotelViewModel.hotels.observe(viewLifecycleOwner, Observer { hotels ->
            tvHotels.text = hotels.joinToString("\n") { "${it.name}, ${it.location},${it.stars}, Rating: ${it.rating}" }
        })


    }
}
