package com.example.kkarhua.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.kkarhua.R
import com.example.kkarhua.data.repository.AuthRepository
import com.example.kkarhua.data.repository.UserRepository
import com.example.kkarhua.utils.ValidationUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {

    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnUpdate: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var userRepository: UserRepository
    private lateinit var authRepository: AuthRepository

    companion object {
        private const val TAG = "EditProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository(requireContext())

        if (!authRepository.isAuthenticated()) {
            Toast.makeText(
                requireContext(),
                "⚠️ Debes iniciar sesión primero",
                Toast.LENGTH_LONG
            ).show()
            findNavController().navigateUp()
            return
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "EDIT PROFILE FRAGMENT CREATED")
        Log.d(TAG, "========================================")

        setupViews(view)
        setupRepository()
        setupValidation()
        setupListeners()
        loadUserData()
    }

    private fun setupViews(view: View) {
        tilName = view.findViewById(R.id.tilName)
        tilEmail = view.findViewById(R.id.tilEmail)
        tilPassword = view.findViewById(R.id.tilPassword)
        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnUpdate = view.findViewById(R.id.btnUpdate)
        btnCancel = view.findViewById(R.id.btnCancel)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupRepository() {
        userRepository = UserRepository(authRepository)
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
            val password = it.toString()
            val result = ValidationUtils.validatePasswordOptional(password)
            tilPassword.error = if (result.isValid) null else result.message
        }
    }

    private fun setupListeners() {
        btnUpdate.setOnClickListener {
            attemptUpdateProfile()
        }

        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadUserData() {
        progressBar.visibility = View.VISIBLE

        // Obtener datos del usuario actual desde SharedPreferences
        val prefs = requireContext().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)

        Log.d(TAG, "→ Cargando datos del usuario $userId")

        lifecycleScope.launch {
            try {
                val result = userRepository.getUserById(userId)

                result.onSuccess { user ->
                    Log.d(TAG, "✓ Datos cargados:")
                    Log.d(TAG, "  Name: ${user.name}")
                    Log.d(TAG, "  Email: ${user.email}")

                    displayUserData(user)
                    progressBar.visibility = View.GONE
                }.onFailure { exception ->
                    Log.e(TAG, "✗ Error al cargar: ${exception.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar perfil: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE
                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Exception: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayUserData(user: com.example.kkarhua.data.remote.UserResponse) {
        etName.setText(user.name)
        etEmail.setText(user.email)
    }

    private fun attemptUpdateProfile() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        Log.d(TAG, "========================================")
        Log.d(TAG, "ATTEMPT UPDATE PROFILE")
        Log.d(TAG, "========================================")
        Log.d(TAG, "Name: $name")
        Log.d(TAG, "Email: $email")
        Log.d(TAG, "Password: ${if (password.isEmpty()) "No cambiar" else "Cambiar"}")
        Log.d(TAG, "========================================")

        val nameValidation = ValidationUtils.validateName(name)
        val emailValidation = ValidationUtils.validateEmail(email)
        val passwordValidation = ValidationUtils.validatePasswordOptional(password)

        when {
            !nameValidation.isValid -> {
                tilName.error = nameValidation.message
                etName.requestFocus()
                return
            }
            !emailValidation.isValid -> {
                tilEmail.error = emailValidation.message
                etEmail.requestFocus()
                return
            }
            !passwordValidation.isValid -> {
                tilPassword.error = passwordValidation.message
                etPassword.requestFocus()
                return
            }
        }

        updateProfile(name, email, password.ifEmpty { null })
    }

    private fun updateProfile(name: String, email: String, password: String?) {
        progressBar.visibility = View.VISIBLE
        btnUpdate.isEnabled = false
        btnUpdate.text = "Actualizando..."

        val prefs = requireContext().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)
        val currentRole = authRepository.getUserRole()
        val currentState = authRepository.getUserState()

        lifecycleScope.launch {
            try {
                val result = userRepository.updateUser(
                    userId = userId,
                    name = name,
                    email = email,
                    role = currentRole, // Mantener rol actual
                    state = currentState, // Mantener estado actual
                    password = password
                )

                result.onSuccess {
                    Log.d(TAG, "✓ Perfil actualizado exitosamente")

                    // Actualizar datos en SharedPreferences
                    prefs.edit().apply {
                        putString("user_name", name)
                        putString("user_email", email)
                        apply()
                    }

                    Toast.makeText(
                        requireContext(),
                        "✓ Perfil actualizado exitosamente",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigateUp()
                }.onFailure { exception ->
                    Log.e(TAG, "✗ Error: ${exception.message}")
                    Toast.makeText(
                        requireContext(),
                        "✗ Error: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    resetButton()
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Exception: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "✗ Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                resetButton()
            }
        }
    }

    private fun resetButton() {
        progressBar.visibility = View.GONE
        btnUpdate.isEnabled = true
        btnUpdate.text = "Actualizar Perfil"
    }
}