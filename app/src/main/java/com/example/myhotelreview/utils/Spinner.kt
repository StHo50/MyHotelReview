package com.example.myhotelreview.utils

import android.view.View
import android.widget.ProgressBar
import com.example.myhotelreview.R

fun View.showLoadingOverlay() {
    this.visibility = View.VISIBLE
    this.findViewById<View>(R.id.blurView)?.visibility = View.VISIBLE
    this.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE
}

fun View.hideLoadingOverlay() {
    this.visibility = View.GONE
    this.findViewById<View>(R.id.blurView)?.visibility = View.GONE
    this.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
}