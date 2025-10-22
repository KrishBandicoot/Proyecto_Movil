package com.example.kkarhua.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kkarhua.R
import com.example.kkarhua.data.repository.AuthRepository
import com.example.kkarhua.utils.ValidationUtils
import com.example.kkarhua.viewmodel.AuthViewModel
import com.example.kkarhua.viewmodel.AuthViewModelFactory
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtRegister: TextView
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var authViewModel: AuthViewModel
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupViewModel()
        setupAnimations()
        setupValidation()
        setupListeners()
        observeViewModel()
    }

    private fun setupViews(view: View) {
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        txtRegister = view.findViewById(R.id.txtRegister)
        tilEmail = view.findViewById(R.id.tilEmail)
        tilPassword = view.findViewById(R.id.tilPassword)
        progressBar = view.findViewById(R.id.progressBar)

        // Deshabilitar autofill para evitar Samsung Pass
        etEmail.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        etPassword.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
    }

    private fun setupViewModel() {
        authRepository = AuthRepository(requireContext())
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
    }

    private fun setupAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        etEmail.startAnimation(slideUp)
        etPassword.startAnimation(slideUp)
        btnLogin.startAnimation(fadeIn)
    }

    private fun setupValidation() {
        etEmail.addTextChangedListener {
            val result = ValidationUtils.validateEmail(it.toString())
            if (result.isValid) {
                tilEmail.error = null
                tilEmail.setStartIconDrawable(android.R.drawable.ic_dialog_email)
            } else {
                tilEmail.error = result.message
            }
        }

        etPassword.addTextChangedListener {
            val result = ValidationUtils.validatePassword(it.toString())
            if (result.isValid) {
                tilPassword.error = null
                tilPassword.setStartIconDrawable(android.R.drawable.ic_lock_lock)
            } else {
                tilPassword.error = result.message
            }
        }
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            animateButton(it)
            attemptLogin()
        }

        txtRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeViewModel() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !isLoading
            btnLogin.text = if (isLoading) "Iniciando sesión..." else "Iniciar sesión"
        }

        authViewModel.loginSuccess.observe(viewLifecycleOwner) { authResponse ->
            authResponse?.let {
                val userName = it.user?.name ?: "Usuario"
                Toast.makeText(
                    requireContext(),
                    "✓ Bienvenido $userName!",
                    Toast.LENGTH_LONG
                ).show()

                authViewModel.clearSuccessEvents()
                findNavController().navigate(R.id.homeFragment)
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(
                    requireContext(),
                    "✗ $it",
                    Toast.LENGTH_LONG
                ).show()
                authViewModel.clearError()
            }
        }
    }

    private fun animateButton(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        view.startAnimation(animation)
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        val emailValidation = ValidationUtils.validateEmail(email)
        val passwordValidation = ValidationUtils.validatePassword(password)

        if (!emailValidation.isValid) {
            tilEmail.error = emailValidation.message
            etEmail.requestFocus()
            return
        }

        if (!passwordValidation.isValid) {
            tilPassword.error = passwordValidation.message
            etPassword.requestFocus()
            return
        }

        authViewModel.login(email, password)
    }
}