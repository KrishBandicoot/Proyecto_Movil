package com.example.kkarhua.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kkarhua.R
import com.example.kkarhua.data.local.AppDatabase
import com.example.kkarhua.data.local.CartItem
import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.local.ProductCategory
import com.example.kkarhua.data.repository.CartRepository
import com.example.kkarhua.data.repository.ProductRepository
import com.example.kkarhua.viewmodel.CartViewModel
import com.example.kkarhua.viewmodel.CartViewModelFactory
import com.example.kkarhua.viewmodel.ProductListViewModel
import com.example.kkarhua.viewmodel.ProductListViewModelFactory

class ProductListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var spinnerCategoryFilter: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var txtError: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var productViewModel: ProductListViewModel
    private lateinit var cartViewModel: CartViewModel
    private lateinit var productAdapter: ProductAdapter

    private var allProducts = listOf<Product>()
    private var currentSearchQuery = ""
    private var currentCategoryFilter = "Todas"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        setupViewModels()
        setupCategoryFilter()
        setupSearch()
        setupSwipeRefresh()
        observeProducts()
        observeLoadingState()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerProducts)
        etSearch = view.findViewById(R.id.etSearch)
        spinnerCategoryFilter = view.findViewById(R.id.spinnerCategoryFilter)
        progressBar = view.findViewById(R.id.progressBar)
        txtError = view.findViewById(R.id.txtError)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        productAdapter = ProductAdapter(
            onProductClick = { product ->
                navigateToProductDetail(product)
            },
            onAddToCart = { product ->
                addProductToCart(product)
            }
        )

        recyclerView.adapter = productAdapter
    }

    private fun setupViewModels() {
        val database = AppDatabase.getInstance(requireContext())

        // ViewModel de productos
        val productRepository = ProductRepository(database.productDao())
        val productFactory = ProductListViewModelFactory(productRepository)
        productViewModel = ViewModelProvider(this, productFactory)
            .get(ProductListViewModel::class.java)

        // ViewModel del carrito
        val cartRepository = CartRepository(database.cartDao())
        val cartFactory = CartViewModelFactory(cartRepository)
        cartViewModel = ViewModelProvider(this, cartFactory)
            .get(CartViewModel::class.java)
    }

    private fun setupCategoryFilter() {
        // Agregar "Todas" al inicio de las categorías
        val categories = listOf("Todas") + ProductCategory.getAllCategories()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        spinnerCategoryFilter.adapter = adapter

        spinnerCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentCategoryFilter = categories[position]
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener { text ->
            currentSearchQuery = text.toString().trim()
            applyFilters()
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            productViewModel.syncProducts()
        }
    }

    private fun observeProducts() {
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            applyFilters()

            if (products.isEmpty() && productViewModel.isLoading.value != true) {
                txtError.visibility = View.VISIBLE
                txtError.text = "No hay productos disponibles"
            } else {
                txtError.visibility = View.GONE
            }
        }
    }

    private fun observeLoadingState() {
        // Observar estado de carga
        productViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            swipeRefresh.isRefreshing = isLoading
        }

        // Observar errores
        productViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                txtError.visibility = View.VISIBLE
                txtError.text = it
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun applyFilters() {
        var filteredList = allProducts

        // Filtrar por categoría
        if (currentCategoryFilter != "Todas") {
            filteredList = filteredList.filter { product ->
                product.category.equals(currentCategoryFilter, ignoreCase = true)
            }
        }

        // Filtrar por búsqueda
        if (currentSearchQuery.isNotEmpty()) {
            filteredList = filteredList.filter { product ->
                product.name.contains(currentSearchQuery, ignoreCase = true) ||
                        product.description.contains(currentSearchQuery, ignoreCase = true) ||
                        product.category.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        productAdapter.submitList(filteredList)

        // Mostrar mensaje si no hay resultados
        if (filteredList.isEmpty() && allProducts.isNotEmpty()) {
            txtError.visibility = View.VISIBLE
            txtError.text = if (currentSearchQuery.isNotEmpty()) {
                "No se encontraron productos con \"$currentSearchQuery\""
            } else {
                "No hay productos en esta categoría"
            }
        } else {
            txtError.visibility = View.GONE
        }
    }

    private fun navigateToProductDetail(product: Product) {
        val action = ProductListFragmentDirections
            .actionProductListFragmentToProductDetailFragment(product.id)
        findNavController().navigate(action)
    }

    private fun addProductToCart(product: Product) {
        if (product.stock <= 0) {
            Toast.makeText(
                requireContext(),
                "Producto sin stock disponible",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val cartItem = CartItem(
            productId = product.id,
            productName = product.name,
            quantity = 1,
            price = product.price,
            image = product.image
        )

        cartViewModel.addToCart(cartItem)

        Toast.makeText(
            requireContext(),
            "✓ ${product.name} agregado al carrito",
            Toast.LENGTH_SHORT
        ).show()
    }
}