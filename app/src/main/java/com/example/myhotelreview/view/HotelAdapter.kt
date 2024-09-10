package com.example.myhotelreview.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myhotelreview.databinding.ItemHotelBinding
import com.example.myhotelreview.model.Hotel
import com.squareup.picasso.Picasso

class HotelAdapter(private val hotels: List<Hotel>) :
    RecyclerView.Adapter<HotelAdapter.HotelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val binding = ItemHotelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HotelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        holder.bind(hotels[position])
    }

    override fun getItemCount(): Int = hotels.size

    inner class HotelViewHolder(private val binding: ItemHotelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hotel: Hotel) {
            binding.tvHotelName.text = hotel.name
            binding.tvHotelLocation.text = hotel.location
            binding.tvHotelRating.text = hotel.rating.toString()
            binding.tvHotelStars.text = hotel.stars.toString()
            Picasso.get().load(hotel.image).into(binding.ivHotelImage)
        }
    }
}
