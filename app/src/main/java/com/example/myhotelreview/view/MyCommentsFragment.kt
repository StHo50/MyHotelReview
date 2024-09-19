package com.example.myhotelreview.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myhotelreview.R
import com.example.myhotelreview.viewmodel.LoginViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhotelreview.utils.hideLoadingOverlay
import com.example.myhotelreview.utils.showLoadingOverlay
import com.example.myhotelreview.viewmodel.MyCommentsViewModel


class MyCommentsFragment : Fragment() {

    private val viewModel: MyCommentsViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvMyComments = view.findViewById<RecyclerView>(R.id.rvMyComments)
        rvMyComments.layoutManager = LinearLayoutManager(context)

        commentAdapter = CommentAdapter(emptyList())
        rvMyComments.adapter = commentAdapter

        // Observe the comments and update the RecyclerView
        viewModel.getCommentsForUser().observe(viewLifecycleOwner, { comments ->
            commentAdapter.updateComments(comments)
        })

        // Observe the loading state and show/hide the spinner accordingly
        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                view.findViewById<View>(R.id.loading_overlay)?.showLoadingOverlay()
            } else {
                view.findViewById<View>(R.id.loading_overlay)?.hideLoadingOverlay()
            }
        })
    }
}
