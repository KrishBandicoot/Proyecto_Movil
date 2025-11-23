package com.example.kkarhua.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kkarhua.R
import com.example.kkarhua.data.local.AppDatabase
import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.repository.AuthRepository
import com.example.kkarhua.data.repository.ProductRepository
import com.example.kkarhua.viewmodel.ProductListViewModel
import com.example.kkarhua.viewmodel.ProductListViewModelFactory

class ManageProductsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var productViewModel: ProductListViewModel
    private lateinit var adminProductAdapter: AdminProductAdapter
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage_products, container, false)
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
        setupRecyclerView()
        setupViewModel()
        observeProducts()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerProducts)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        adminProductAdapter = AdminProductAdapter(
            onEditClick = { product ->
                navigateToEditProduct(product)
            },
            onDeleteClick = { product ->
                confirmDeleteProduct(product)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adminProductAdapter
        }
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = ProductRepository(database.productDao())
        val factory = ProductListViewModelFactory(repository)
        productViewModel = ViewModelProvider(this, factory)
            .get(ProductListViewModel::class.java)
    }

    private fun observeProducts() {
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            adminProductAdapter.submitList(products)
        }

        productViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun navigateToEditProduct(product: Product) {
        // TODO: Navegación cuando esté el EditProductFragment
        Toast.makeText(
            requireContext(),
            "Editar: ${product.name}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun confirmDeleteProduct(product: Product) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Estás seguro de que quieres eliminar '${product.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteProduct(product: Product) {
        // TODO: Implementar eliminación
        Toast.makeText(
            requireContext(),
            "Función de eliminar en desarrollo",
            Toast.LENGTH_SHORT
        ).show()
    }
}