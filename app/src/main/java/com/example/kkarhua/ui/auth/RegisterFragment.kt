package com.example.kkarhua.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kkarhua.R
import com.example.kkarhua.utils.ValidationUtils

class RegisterFragment : Fragment() {

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var txtLogin: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupValidation()
        setupListeners()
        setupAnimations()
    }

    private fun setupViews(view: View) {
        etNombre = view.findViewById(R.id.etNombre)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegistrar = view.findViewById(R.id.btnRegistrar)
        txtLogin = view.findViewById(R.id.txtLogin)
    }

    private fun setupValidation() {
        etNombre.addTextChangedListener {
            val result = ValidationUtils.validateName(it.toString())
            if (!result.isValid) {
                etNombre.error = result.message
            } else {
                etNombre.error = null
            }
        }

        etEmail.addTextChangedListener {
            val result = ValidationUtils.validateEmail(it.toString())
            if (!result.isValid) {
                etEmail.error = result.message
            } else {
                etEmail.error = null
            }
        }

        etPassword.addTextChangedListener {
            val result = ValidationUtils.validatePassword(it.toString())
            if (!result.isValid) {
                etPassword.error = result.message
            } else {
                etPassword.error = null
            }
        }

        etConfirmPassword.addTextChangedListener {
            val result = ValidationUtils.validatePasswordMatch(
                etPassword.text.toString(),
                it.toString()
            )
            if (!result.isValid) {
                etConfirmPassword.error = result.message
            } else {
                etConfirmPassword.error = null
            }
        }
    }

    private fun setupListeners() {
        btnRegistrar.setOnClickListener {
            animateButton(it)
            attemptRegister()
        }

        txtLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        etNombre.startAnimation(slideUp)
        etEmail.startAnimation(slideUp)
        etPassword.startAnimation(slideUp)
        etConfirmPassword.startAnimation(slideUp)
        btnRegistrar.startAnimation(fadeIn)
    }

    private fun animateButton(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        view.startAnimation(animation)
    }

    private fun attemptRegister() {
        val nombre = etNombre.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        val nombreValidation = ValidationUtils.validateName(nombre)
        val emailValidation = ValidationUtils.validateEmail(email)
        val passwordValidation = ValidationUtils.validatePassword(password)
        val matchValidation = ValidationUtils.validatePasswordMatch(password, confirmPassword)

        when {
            !nombreValidation.isValid -> {
                etNombre.error = nombreValidation.message
                etNombre.requestFocus()
            }
            !emailValidation.isValid -> {
                etEmail.error = emailValidation.message
                etEmail.requestFocus()
            }
            !passwordValidation.isValid -> {
                etPassword.error = passwordValidation.message
                etPassword.requestFocus()
            }
            !matchValidation.isValid -> {
                etConfirmPassword.error = matchValidation.message
                etConfirmPassword.requestFocus()
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    "✓ Registro exitoso\n¡Bienvenido $nombre!",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigate(R.id.homeFragment)
            }
        }
    }
}