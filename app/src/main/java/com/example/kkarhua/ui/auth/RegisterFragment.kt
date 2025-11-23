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

class RegisterFragment : Fragment() {

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var txtLogin: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var authViewModel: AuthViewModel

    companion object {
        private const val TAG = "RegisterFragment"
    }

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
        progressBar = view.findViewById(R.id.progressBar)

        // Deshabilitar autofill para evitar Samsung Pass
        etNombre.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        etEmail.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        etPassword.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        etConfirmPassword.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
    }

    private fun setupViewModel() {
        val repository = AuthRepository(requireContext())
        val factory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
    }

    private fun setupValidation() {
        etNombre.addTextChangedListener {
            val result = ValidationUtils.validateName(it.toString())
            etNombre.error = if (result.isValid) null else result.message
        }

        etEmail.addTextChangedListener {
            val result = ValidationUtils.validateEmail(it.toString())
            etEmail.error = if (result.isValid) null else result.message
        }

        etPassword.addTextChangedListener {
            val result = ValidationUtils.validatePassword(it.toString())
            etPassword.error = if (result.isValid) null else result.message
        }

        etConfirmPassword.addTextChangedListener {
            val result = ValidationUtils.validatePasswordMatch(
                etPassword.text.toString(),
                it.toString()
            )
            etConfirmPassword.error = if (result.isValid) null else result.message
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
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnRegistrar.isEnabled = !isLoading
            btnRegistrar.text = if (isLoading) "Registrando..." else "Registrarse"
        }

        authViewModel.signupSuccess.observe(viewLifecycleOwner) { authResponse ->
            authResponse?.let {
                val userName = it.user?.name ?: "Usuario"
                val userRole = it.user?.role ?: "member"

                Log.d(TAG, "✅ Registro exitoso - Usuario: $userName, Rol: $userRole")

                Toast.makeText(
                    requireContext(),
                    "✓ Registro exitoso\n¡Bienvenido $userName!",
                    Toast.LENGTH_LONG
                ).show()

                authViewModel.clearSuccessEvents()
                findNavController().navigate(R.id.homeFragment)
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Log.e(TAG, "❌ Error en registro: $it")

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
        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        Log.d(TAG, "🔍 Intentando registrar usuario:")
        Log.d(TAG, "  - Nombre: $nombre")
        Log.d(TAG, "  - Email: $email")
        Log.d(TAG, "  - Password length: ${password.length}")

        val nombreValidation = ValidationUtils.validateName(nombre)
        val emailValidation = ValidationUtils.validateEmail(email)
        val passwordValidation = ValidationUtils.validatePassword(password)
        val matchValidation = ValidationUtils.validatePasswordMatch(password, confirmPassword)

        when {
            !nombreValidation.isValid -> {
                etNombre.error = nombreValidation.message
                etNombre.requestFocus()
                Log.w(TAG, "⚠️ Validación nombre falló: ${nombreValidation.message}")
            }
            !emailValidation.isValid -> {
                etEmail.error = emailValidation.message
                etEmail.requestFocus()
                Log.w(TAG, "⚠️ Validación email falló: ${emailValidation.message}")
            }
            !passwordValidation.isValid -> {
                etPassword.error = passwordValidation.message
                etPassword.requestFocus()
                Log.w(TAG, "⚠️ Validación password falló: ${passwordValidation.message}")
            }
            !matchValidation.isValid -> {
                etConfirmPassword.error = matchValidation.message
                etConfirmPassword.requestFocus()
                Log.w(TAG, "⚠️ Validación confirmación falló: ${matchValidation.message}")
            }
            else -> {
                Log.d(TAG, "✅ Todas las validaciones pasaron, iniciando signup...")
                authViewModel.signup(nombre, email, password)
            }
        }
    }
}