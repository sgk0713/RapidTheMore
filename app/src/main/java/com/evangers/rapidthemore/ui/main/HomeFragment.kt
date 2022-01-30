package com.evangers.rapidthemore.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.evangers.rapidthemore.R
import com.evangers.rapidthemore.databinding.FragmentHomeBinding
import com.evangers.rapidthemore.ui.base.ParentFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : ParentFragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var binding: FragmentHomeBinding
    override fun bindView(view: View) {
        binding = FragmentHomeBinding.bind(view)
    }

    override fun initUi() {
//        TODO("Not yet implemented")
    }

    override fun initBinding() {
//        TODO("Not yet implemented")
    }

    override fun onViewCreatedSg(view: View, savedInstanceState: Bundle?) {
        super.onViewCreatedSg(view, savedInstanceState)
        initUi()
        initBinding()
    }
}