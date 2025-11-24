package com.example.kkarhua.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AdminRegisterUserFragment : Fragment() {

    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var radioGroupRole: RadioGroup
    private lateinit var radioMember: RadioButton
    private lateinit var radioAdmin: RadioButton
    private lateinit var btnRegister: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var authViewModel: AuthViewModel
    private lateinit var authRepository: AuthRepository

    companion object {
        private const val TAG = "AdminRegisterUser"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_register_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository(requireContext())

        // ✅ Verificar que el usuario actual es admin
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
        setupViewModel()
        setupValidation()
        setupListeners()
        setupAnimations()
        observeViewModel()
    }

    private fun setupViews(view: View) {
        tilName = view.findViewById(R.id.tilName)
        tilEmail = view.findViewById(R.id.tilEmail)
        tilPassword = view.findViewById(R.id.tilPassword)
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword)
        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        radioGroupRole = view.findViewById(R.id.radioGroupRole)
        radioMember = view.findViewById(R.id.radioMember)
        radioAdmin = view.findViewById(R.id.radioAdmin)
        btnRegister = view.findViewById(R.id.btnRegister)
        btnCancel = view.findViewById(R.id.btnCancel)
        progressBar = view.findViewById(R.id.progressBar)

        // Deshabilitar autofill
        etName.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        etEmail.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        etPassword.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        etConfirmPassword.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
    }

    private fun setupViewModel() {
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
    }

    private fun setupValidation() {
        etName.addTextChangedListener {
            val result = ValidationUtils.validateName(it.toString())
            tilName.error = if (result.isValid) null else result.message
        }

        etEmail.addTextChangedListener {
            val result = ValidationUtils.validateEmail(it.toString())
            tilEmail.error = if (result.isValid) null else result.message
        }

        etPassword.addTextChangedListener {
            val result = ValidationUtils.validatePassword(it.toString())
            tilPassword.error = if (result.isValid) null else result.message
        }

        etConfirmPassword.addTextChangedListener {
            val result = ValidationUtils.validatePasswordMatch(
                etPassword.text.toString(),
                it.toString()
            )
            tilConfirmPassword.error = if (result.isValid) null else result.message
        }
    }

    private fun setupListeners() {
        btnRegister.setOnClickListener {
            animateButton(it)
            attemptRegister()
        }

        btnCancel.setOnClickListener {
            animateButton(it)
            findNavController().navigateUp()
        }
    }

    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        tilName.startAnimation(slideUp)
        tilEmail.startAnimation(slideUp)
        tilPassword.startAnimation(slideUp)
        tilConfirmPassword.startAnimation(slideUp)
        radioGroupRole.startAnimation(fadeIn)
    }

    private fun observeViewModel() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnRegister.isEnabled = !isLoading
            btnRegister.text = if (isLoading) "Registrando..." else "Registrar Usuario"
        }

        authViewModel.signupSuccess.observe(viewLifecycleOwner) { authResponse ->
            authResponse?.let {
                val userName = it.user?.name ?: "Usuario"
                val userRole = it.user?.role ?: "member"
                val roleText = if (userRole == "admin") "Administrador" else "Usuario"

                Log.d(TAG, "✅ Usuario creado - Nombre: $userName, Rol: $userRole")

                Toast.makeText(
                    requireContext(),
                    "✓ Usuario $userName creado exitosamente\nRol: $roleText",
                    Toast.LENGTH_LONG
                ).show()

                authViewModel.clearSuccessEvents()
                findNavController().navigateUp()
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Log.e(TAG, "❌ Error: $it")

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
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        val role = when (radioGroupRole.checkedRadioButtonId) {
            R.id.radioAdmin -> "admin"
            else -> "member"
        }

        Log.d(TAG, "🔍 Intentando registrar usuario:")
        Log.d(TAG, "  - Nombre: $name")
        Log.d(TAG, "  - Email: $email")
        Log.d(TAG, "  - Rol: $role")

        // Validaciones
        val nameValidation = ValidationUtils.validateName(name)
        val emailValidation = ValidationUtils.validateEmail(email)
        val passwordValidation = ValidationUtils.validatePassword(password)
        val matchValidation = ValidationUtils.validatePasswordMatch(password, confirmPassword)

        when {
            !nameValidation.isValid -> {
                tilName.error = nameValidation.message
                etName.requestFocus()
                Log.w(TAG, "⚠️ Validación nombre falló")
            }
            !emailValidation.isValid -> {
                tilEmail.error = emailValidation.message
                etEmail.requestFocus()
                Log.w(TAG, "⚠️ Validación email falló")
            }
            !passwordValidation.isValid -> {
                tilPassword.error = passwordValidation.message
                etPassword.requestFocus()
                Log.w(TAG, "⚠️ Validación password falló")
            }
            !matchValidation.isValid -> {
                tilConfirmPassword.error = matchValidation.message
                etConfirmPassword.requestFocus()
                Log.w(TAG, "⚠️ Validación confirmación falló")
            }
            else -> {
                Log.d(TAG, "✅ Todas las validaciones pasaron, creando usuario...")
                authViewModel.adminSignup(name, email, password, role)
            }
        }
    }
}