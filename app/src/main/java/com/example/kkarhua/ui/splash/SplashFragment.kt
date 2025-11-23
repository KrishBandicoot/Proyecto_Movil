package com.example.kkarhua.ui.splash

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

class SplashFragment : Fragment() {

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository(requireContext())

        // ✅ Verificar si ya hay sesión activa
        if (authRepository.isAuthenticated()) {
            // Usuario ya está logueado, redirigir a Home
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
            return
        }

        // Si no hay sesión, mostrar botones de login/registro
        setupViews(view)
        setupAnimations()
        setupListeners()
    }

    private fun setupViews(view: View) {
        titleText = view.findViewById(R.id.titleText)
        subtitleText = view.findViewById(R.id.subtitleText)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnRegister = view.findViewById(R.id.btnRegister)
    }

    private fun setupAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        titleText.startAnimation(fadeIn)
        subtitleText.startAnimation(fadeIn)
        btnLogin.startAnimation(slideUp)
        btnRegister.startAnimation(slideUp)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            animateButtonClick(it)
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }

        btnRegister.setOnClickListener {
            animateButtonClick(it)
            findNavController().navigate(R.id.action_splashFragment_to_registerFragment)
        }
    }

    private fun animateButtonClick(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        view.startAnimation(animation)
    }
}