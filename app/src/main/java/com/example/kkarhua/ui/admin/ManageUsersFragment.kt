package com.example.kkarhua.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kkarhua.R
import com.example.kkarhua.data.remote.UserResponse
import com.example.kkarhua.data.repository.AuthRepository
import com.example.kkarhua.data.repository.UserRepository
import kotlinx.coroutines.launch

class ManageUsersFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var userRepository: UserRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var usersAdapter: UsersAdapter

    private var allUsers = listOf<UserResponse>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage_users, container, false)
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

        setupViews(view)
        setupRepository()
        setupRecyclerView()
        setupSearch()
        loadUsers()
    }

    private fun setupViews(view: View) {
        etSearch = view.findViewById(R.id.etSearch)
        recyclerView = view.findViewById(R.id.recyclerUsers)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupRepository() {
        userRepository = UserRepository(authRepository)
    }

    private fun setupRecyclerView() {
        usersAdapter = UsersAdapter(
            onEditClick = { user ->
                navigateToEditUser(user)
            },
            onDeleteClick = { user ->
                confirmDeleteUser(user)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = usersAdapter
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener { text ->
            filterUsers(text.toString())
        }
    }

    private fun loadUsers() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = userRepository.getAllUsers()

                result.onSuccess { users ->
                    allUsers = users
                    filterUsers("")
                    progressBar.visibility = View.GONE
                }.onFailure { exception ->
                    Toast.makeText(
                        requireContext(),
                        "✗ Error al cargar usuarios: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "✗ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun filterUsers(query: String) {
        val searchQuery = query.trim().lowercase()

        val filteredList = if (searchQuery.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.name.lowercase().contains(searchQuery) ||
                        user.email.lowercase().contains(searchQuery)
            }
        }

        usersAdapter.submitList(filteredList)
    }

    private fun navigateToEditUser(user: UserResponse) {
        val action = ManageUsersFragmentDirections
            .actionManageUsersFragmentToEditUserFragment(user.id)
        findNavController().navigate(action)
    }

    private fun confirmDeleteUser(user: UserResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar usuario")
            .setMessage("¿Estás seguro de que quieres eliminar a '${user.name}'?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteUser(user)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUser(user: UserResponse) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = userRepository.deleteUser(user.id)

                result.onSuccess {
                    Toast.makeText(
                        requireContext(),
                        "✓ Usuario ${user.name} eliminado exitosamente",
                        Toast.LENGTH_LONG
                    ).show()

                    // Recargar lista de usuarios
                    loadUsers()
                }.onFailure { exception ->
                    Toast.makeText(
                        requireContext(),
                        "✗ Error al eliminar: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "✗ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE
            }
        }
    }
}