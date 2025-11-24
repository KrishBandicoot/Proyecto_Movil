package com.example.kkarhua.ui.admin

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
import androidx.navigation.fragment.navArgs
import com.example.kkarhua.R
import com.example.kkarhua.data.remote.UserResponse
import com.example.kkarhua.data.repository.AuthRepository
import com.example.kkarhua.data.repository.UserRepository
import com.example.kkarhua.utils.ValidationUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class EditUserFragment : Fragment() {

    private val args: EditUserFragmentArgs by navArgs()

    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout  // ✅ NUEVO
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText  // ✅ NUEVO
    private lateinit var radioGroupRole: RadioGroup
    private lateinit var radioMember: RadioButton
    private lateinit var radioAdmin: RadioButton
    private lateinit var btnUpdate: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var userRepository: UserRepository
    private lateinit var authRepository: AuthRepository

    private var currentUser: UserResponse? = null

    companion object {
        private const val TAG = "EditUserFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository(requireContext())

        if (!authRepository.isAdmin()) {
            Toast.makeText(
                requireContext(),
                "⚠️ Acceso denegado: Solo administradores",
                Toast.LENGTH_LONG
            ).show()
            findNavController().navigateUp()
            return
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "EDIT USER FRAGMENT CREATED")
        Log.d(TAG, "User ID: ${args.userId}")
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
        tilPassword = view.findViewById(R.id.tilPassword)  // ✅ NUEVO
        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)  // ✅ NUEVO
        radioGroupRole = view.findViewById(R.id.radioGroupRole)
        radioMember = view.findViewById(R.id.radioMember)
        radioAdmin = view.findViewById(R.id.radioAdmin)
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

        // ✅ NUEVO: Validación opcional de contraseña
        etPassword.addTextChangedListener {
            val password = it.toString()
            if (password.isNotEmpty()) {
                val result = ValidationUtils.validatePassword(password)
                tilPassword.error = if (result.isValid) null else result.message
            } else {
                tilPassword.error = null
            }
        }
    }

    private fun setupListeners() {
        btnUpdate.setOnClickListener {
            attemptUpdateUser()
        }

        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadUserData() {
        progressBar.visibility = View.VISIBLE

        Log.d(TAG, "→ Cargando datos del usuario ${args.userId}")

        lifecycleScope.launch {
            try {
                val result = userRepository.getUserById(args.userId)

                result.onSuccess { user ->
                    Log.d(TAG, "✓ Datos cargados:")
                    Log.d(TAG, "  Name: ${user.name}")
                    Log.d(TAG, "  Email: ${user.email}")
                    Log.d(TAG, "  Role: ${user.role}")

                    currentUser = user
                    displayUserData(user)
                    progressBar.visibility = View.GONE
                }.onFailure { exception ->
                    Log.e(TAG, "✗ Error al cargar: ${exception.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar usuario: ${exception.message}",
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

    private fun displayUserData(user: UserResponse) {
        etName.setText(user.name)
        etEmail.setText(user.email)
        // ✅ NO establecer contraseña (campo vacío = no cambiar)

        // Seleccionar rol
        when (user.role) {
            "admin" -> radioAdmin.isChecked = true
            else -> radioMember.isChecked = true
        }
    }

    private fun attemptUpdateUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()  // ✅ NUEVO
        val role = when (radioGroupRole.checkedRadioButtonId) {
            R.id.radioAdmin -> "admin"
            else -> "member"
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "ATTEMPT UPDATE USER")
        Log.d(TAG, "========================================")
        Log.d(TAG, "Name: $name")
        Log.d(TAG, "Email: $email")
        Log.d(TAG, "Role: $role")
        Log.d(TAG, "Password: ${if (password.isEmpty()) "No cambiar" else "Cambiar"}")
        Log.d(TAG, "========================================")

        // Validaciones
        val nameValidation = ValidationUtils.validateName(name)
        val emailValidation = ValidationUtils.validateEmail(email)

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
            password.isNotEmpty() -> {
                // ✅ Si se ingresó contraseña, validarla
                val passwordValidation = ValidationUtils.validatePassword(password)
                if (!passwordValidation.isValid) {
                    tilPassword.error = passwordValidation.message
                    etPassword.requestFocus()
                    return
                }
            }
        }

        updateUser(name, email, role, password.ifEmpty { null })
    }

    private fun updateUser(name: String, email: String, role: String, password: String?) {
        progressBar.visibility = View.VISIBLE
        btnUpdate.isEnabled = false
        btnUpdate.text = "Actualizando..."

        lifecycleScope.launch {
            try {
                // ✅ NUEVO: Pasar password (null si está vacío)
                val result = userRepository.updateUser(
                    userId = args.userId,
                    name = name,
                    email = email,
                    role = role,
                    password = password
                )

                result.onSuccess {
                    Log.d(TAG, "✓ Usuario actualizado exitosamente")
                    Toast.makeText(
                        requireContext(),
                        "✓ Usuario actualizado exitosamente",
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
        btnUpdate.text = "Actualizar Usuario"
    }
}