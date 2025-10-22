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
import com.example.kkarhua.data.repository.AuthRepository

class HomeFragment : Fragment() {

    private lateinit var btnLogout: Button
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
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    private fun setupAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        titleText.startAnimation(fadeIn)
        btnLogout.startAnimation(slideUp)
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            animateButtonClick(it)
            logout()
        }
    }

    private fun logout() {
        val authRepository = AuthRepository(requireContext())
        authRepository.logout()

        // Navegar de vuelta a Splash eliminando HomeFragment del stack
        findNavController().popBackStack(R.id.splashFragment, false)
    }

    private fun animateButtonClick(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        view.startAnimation(animation)
    }
}