package com.example.kkarhua.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.launch

class ManageProductsFragment : Fragment() {

    private lateinit var etSearch: EditText // ✅ NUEVO
    private lateinit var spinnerCategoryFilter: Spinner // ✅ NUEVO
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var productViewModel: ProductListViewModel
    private lateinit var adminProductAdapter: AdminProductAdapter
    private lateinit var authRepository: AuthRepository
    private lateinit var productRepository: ProductRepository

    private var allProducts = listOf<Product>() // ✅ NUEVO: Lista completa de productos

    // ✅ NUEVO: Lista de categorías
    private val categories = listOf(
        "Todas",
        "Accesorios",
        "Joyería",
        "Bisutería",
        "Textil",
        "Cerámica",
        "Madera",
        "Cuero",
        "Otro"
    )

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
        setupViewModel()
        setupCategorySpinner() // ✅ NUEVO
        setupSearch() // ✅ NUEVO
        observeProducts()
    }

    private fun setupViews(view: View) {
        etSearch = view.findViewById(R.id.etSearch) // ✅ NUEVO
        spinnerCategoryFilter = view.findViewById(R.id.spinnerCategoryFilter) // ✅ NUEVO
        recyclerView = view.findViewById(R.id.recyclerProducts)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupRepository() {
        val database = AppDatabase.getInstance(requireContext())
        productRepository = ProductRepository(database.productDao())
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

    // ✅ NUEVO: Configurar Spinner de categorías
    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        spinnerCategoryFilter.adapter = adapter

        spinnerCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterProducts()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ✅ NUEVO: Configurar búsqueda
    private fun setupSearch() {
        etSearch.addTextChangedListener { text ->
            filterProducts()
        }
    }

    // ✅ NUEVO: Filtrar productos por búsqueda y categoría
    private fun filterProducts() {
        val searchQuery = etSearch.text.toString().trim().lowercase()
        val selectedCategory = spinnerCategoryFilter.selectedItem.toString()

        val filteredList = allProducts.filter { product ->
            val matchesSearch = searchQuery.isEmpty() ||
                    product.name.lowercase().contains(searchQuery) ||
                    product.description.lowercase().contains(searchQuery)

            val matchesCategory = selectedCategory == "Todas" ||
                    product.category == selectedCategory

            matchesSearch && matchesCategory
        }

        adminProductAdapter.submitList(filteredList)
    }

    private fun observeProducts() {
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products // ✅ Guardar lista completa
            filterProducts() // ✅ Aplicar filtros
        }

        productViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun navigateToEditProduct(product: Product) {
        val action = ManageProductsFragmentDirections
            .actionManageProductsFragmentToEditProductFragment(product.id)
        findNavController().navigate(action)
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

    // ✅ ACTUALIZADO: Ahora realmente elimina del API
    private fun deleteProduct(product: Product) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val productId = product.id.toIntOrNull()

                if (productId == null) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ID de producto inválido",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE
                    return@launch
                }

                val result = productRepository.deleteProductFromApi(productId)

                result.onSuccess {
                    Toast.makeText(
                        requireContext(),
                        "✓ Producto eliminado exitosamente",
                        Toast.LENGTH_LONG
                    ).show()

                    // Sincronizar productos para reflejar cambios
                    productViewModel.syncProducts()
                }.onFailure { exception ->
                    Toast.makeText(
                        requireContext(),
                        "✗ Error al eliminar: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                progressBar.visibility = View.GONE

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