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
    private lateinit var btnAdminPanel: Button
    private lateinit var titleText: TextView
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository(requireContext())

        setupViews(view)
        setupAnimations()
        setupListeners()
        checkAdminStatus() // ✅ Verificar si es admin
    }

    private fun setupViews(view: View) {
        titleText = view.findViewById(R.id.titleText)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnAdminPanel = view.findViewById(R.id.btnAdminPanel)
    }

    private fun setupAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        titleText.startAnimation(fadeIn)
        btnLogout.startAnimation(slideUp)

        if (authRepository.isAdmin()) {
            btnAdminPanel.startAnimation(slideUp)
        }
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            animateButtonClick(it)
            logout()
        }

        btnAdminPanel.setOnClickListener {
            animateButtonClick(it)
            findNavController().navigate(R.id.action_homeFragment_to_adminPanelFragment)
        }
    }

    private fun checkAdminStatus() {
        // ✅ Mostrar botón de Admin Panel solo si el usuario es admin
        if (authRepository.isAdmin()) {
            btnAdminPanel.visibility = View.VISIBLE
        } else {
            btnAdminPanel.visibility = View.GONE
        }
    }

    private fun logout() {
        authRepository.logout()
        // Navegar al splash y limpiar todo el back stack
        findNavController().navigate(R.id.action_homeFragment_to_splashFragment)
    }

    private fun animateButtonClick(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        view.startAnimation(animation)
    }
}