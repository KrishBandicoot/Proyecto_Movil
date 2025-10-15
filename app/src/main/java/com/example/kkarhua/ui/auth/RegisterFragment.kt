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

class RegisterFragment : Fragment() {

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var txtLogin: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var authViewModel: AuthViewModel

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
        setupViewModel()
        setupValidation()
        setupListeners()
        setupAnimations()
        observeViewModel()
    }

    private fun setupViews(view: View) {
        etNombre = view.findViewById(R.id.etNombre)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegistrar = view.findViewById(R.id.btnRegistrar)
        txtLogin = view.findViewById(R.id.txtLogin)

        // Agregar ProgressBar si no existe en el layout
        progressBar = view.findViewById<ProgressBar?>(R.id.progressBar)
            ?: ProgressBar(requireContext()).apply {
                visibility = View.GONE
            }
    }

    private fun setupViewModel() {
        val repository = AuthRepository(requireContext())
        val factory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
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

    private fun observeViewModel() {
        // Observar estado de carga
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnRegistrar.isEnabled = !isLoading

            if (isLoading) {
                btnRegistrar.text = "Registrando..."
            } else {
                btnRegistrar.text = "Registrarse"
            }
        }

        // Observar registro exitoso
        authViewModel.signupSuccess.observe(viewLifecycleOwner) { authResponse ->
            authResponse?.let {
                // Uso seguro del operador ?. para acceder a user
                val userName = it.user?.name ?: "Usuario"

                Toast.makeText(
                    requireContext(),
                    "✓ Registro exitoso\n¡Bienvenido $userName!",
                    Toast.LENGTH_LONG
                ).show()

                authViewModel.clearSuccessEvents()
                findNavController().navigate(R.id.homeFragment)
            }
        }

        // Observar errores
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
                // Realizar registro con la API
                authViewModel.signup(nombre, email, password)
            }
        }
    }
}