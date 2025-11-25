package com.example.kkarhua.ui.home

import android.os.Bundle
import android.util.Log
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
import android.widget.Toast

class HomeFragment : Fragment() {

    private lateinit var btnLogout: Button
    private lateinit var btnAdminPanel: Button
    private lateinit var btnMyPurchases: Button // ✅ NUEVO
    private lateinit var titleText: TextView
    private lateinit var authRepository: AuthRepository

    companion object {
        private const val TAG = "HomeFragment"
    }

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
        checkAdminStatus()
    }

    private fun setupViews(view: View) {
        titleText = view.findViewById(R.id.titleText)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnAdminPanel = view.findViewById(R.id.btnAdminPanel)
        btnMyPurchases = view.findViewById(R.id.btnMyPurchases) // ✅ NUEVO
    }

    private fun setupAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        titleText.startAnimation(fadeIn)
        btnLogout.startAnimation(slideUp)
        btnMyPurchases.startAnimation(slideUp) // ✅ NUEVO

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

        // ✅ NUEVO: Navegar a Mis Compras
        btnMyPurchases.setOnClickListener {
            animateButtonClick(it)
            findNavController().navigate(R.id.action_homeFragment_to_myPurchasesFragment)
        }
    }

    private fun checkAdminStatus() {
        val role = authRepository.getUserRole()
        val isAdmin = authRepository.isAdmin()

        Log.d(TAG, "========================================")
        Log.d(TAG, "CHECK ADMIN STATUS EN HOME")
        Log.d(TAG, "========================================")
        Log.d(TAG, "Usuario autenticado: ${authRepository.isAuthenticated()}")
        Log.d(TAG, "Nombre: ${authRepository.getUserName()}")
        Log.d(TAG, "Email: ${authRepository.getUserEmail()}")
        Log.d(TAG, "Rol del usuario: '$role'")
        Log.d(TAG, "Es admin: $isAdmin")
        Log.d(TAG, "Botón existe: ${::btnAdminPanel.isInitialized}")
        Log.d(TAG, "========================================")

        if (isAdmin) {
            Log.d(TAG, "✓ MOSTRANDO BOTÓN DE ADMIN")
            btnAdminPanel.visibility = View.VISIBLE
        } else {
            Log.d(TAG, "✗ OCULTANDO BOTÓN DE ADMIN")
            btnAdminPanel.visibility = View.GONE
        }
    }

    private fun logout() {
        Log.d(TAG, "→ Cerrando sesión")
        authRepository.logout()

        Toast.makeText(
            requireContext(),
            "✓ Sesión cerrada",
            Toast.LENGTH_SHORT
        ).show()

        findNavController().navigate(
            R.id.splashFragment,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
    }

    private fun animateButtonClick(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        view.startAnimation(animation)
    }
}