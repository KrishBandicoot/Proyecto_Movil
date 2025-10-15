package com.example.kkarhua.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kkarhua.R

class HomeFragment : Fragment() {

    private lateinit var btnVerProducts: Button
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var titleText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupAnimations()
        setupListeners()
    }

    private fun setupViews(view: View) {
        titleText = view.findViewById(R.id.titleText)
        btnVerProducts = view.findViewById(R.id.btnVerProducts)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnRegister = view.findViewById(R.id.btnRegister)
    }

    private fun setupAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        titleText.startAnimation(fadeIn)
        btnVerProducts.startAnimation(slideUp)
        btnLogin.startAnimation(slideUp)
        btnRegister.startAnimation(slideUp)
    }

    private fun setupListeners() {
        btnVerProducts.setOnClickListener {
            animateButtonClick(it)
            findNavController().navigate(R.id.action_homeFragment_to_productListFragment)
        }

        btnLogin.setOnClickListener {
            animateButtonClick(it)
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }

        btnRegister.setOnClickListener {
            animateButtonClick(it)
            findNavController().navigate(R.id.action_homeFragment_to_registerFragment)
        }
    }

    private fun animateButtonClick(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        view.startAnimation(animation)
    }
}