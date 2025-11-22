package com.example.kkarhua.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kkarhua.R
import com.example.kkarhua.data.repository.AuthRepository

class AdminPanelFragment : Fragment() {

    private lateinit var titleText: TextView
    private lateinit var btnAddProduct: Button
    private lateinit var btnBackToClient: Button
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository(requireContext())

        // ✅ Verificar que el usuario es admin
        if (!authRepository.isAdmin()) {
            Toast.makeText(
                requireContext(),
                "⚠️ Acceso denegado: Solo administradores",
                Toast.LENGTH_LONG
            ).show()
            findNavController().navigateUp()
            return
        }

        setupViews(view)
        setupAnimations()
        setupListeners()
    }

    private fun setupViews(view: View) {
        titleText = view.findViewById(R.id.titleText)
        btnAddProduct = view.findViewById(R.id.btnAddProduct)
        btnBackToClient = view.findViewById(R.id.btnBackToClient)
    }

    private fun setupAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        titleText.startAnimation(fadeIn)
        btnAddProduct.startAnimation(slideUp)
        btnBackToClient.startAnimation(slideUp)
    }

    private fun setupListeners() {
        btnAddProduct.setOnClickListener {
            animateButton(it)
            findNavController().navigate(R.id.action_adminPanelFragment_to_addProductFragment)
        }

        btnBackToClient.setOnClickListener {
            animateButton(it)
            findNavController().navigate(R.id.action_adminPanelFragment_to_homeFragment)
        }
    }

    private fun animateButton(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        view.startAnimation(animation)
    }
}