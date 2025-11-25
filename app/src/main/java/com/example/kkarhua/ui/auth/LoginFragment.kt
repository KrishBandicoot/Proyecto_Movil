package com.example.kkarhua.ui.auth

import android.os.Bundle
import android.util.Log
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

    companion object {
        private const val TAG = "LoginFragment"
    }

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

        // ✅ Deshabilitar completamente autofill
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            etEmail.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            etPassword.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }
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
                Log.d(TAG, "========================================")
                Log.d(TAG, "LOGIN SUCCESS OBSERVER")
                Log.d(TAG, "========================================")
                Log.d(TAG, "AuthResponse recibido: ${it != null}")
                Log.d(TAG, "User data: ${it.user}")
                Log.d(TAG, "User name: ${it.user?.name}")
                Log.d(TAG, "User email: ${it.user?.email}")
                Log.d(TAG, "User role: ${it.user?.role}")
                Log.d(TAG, "User state: ${it.user?.state}")
                Log.d(TAG, "========================================")

                val userName = it.user?.name ?: "Usuario"
                val userRole = it.user?.role ?: "member"

                Toast.makeText(
                    requireContext(),
                    "✓ Bienvenido $userName! (Rol: $userRole)",
                    Toast.LENGTH_LONG
                ).show()

                authViewModel.clearSuccessEvents()

                // Navegar a home
                findNavController().navigate(R.id.homeFragment)
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Log.e(TAG, "========================================")
                Log.e(TAG, "ERROR EN LOGIN")
                Log.e(TAG, "Error message: '$it'")
                Log.e(TAG, "========================================")

                // ✅ VERIFICAR SI EL USUARIO ESTÁ BLOQUEADO
                if (it == "USUARIO BLOQUEADO") {
                    Toast.makeText(
                        requireContext(),
                        "❌ USUARIO BLOQUEADO\n\nTu cuenta ha sido bloqueada.\nContacta al administrador.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Redirigir a SplashFragment y limpiar back stack
                    findNavController().navigate(
                        R.id.splashFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build()
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        "✗ $it",
                        Toast.LENGTH_LONG
                    ).show()
                }

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

        Log.d(TAG, "Intentando login con email: $email")

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