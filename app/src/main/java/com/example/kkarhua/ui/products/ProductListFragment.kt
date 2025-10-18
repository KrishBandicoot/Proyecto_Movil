package com.example.kkarhua.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
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
import com.example.kkarhua.data.repository.CartRepository
import com.example.kkarhua.data.repository.ProductRepository
import com.example.kkarhua.viewmodel.CartViewModel
import com.example.kkarhua.viewmodel.CartViewModelFactory
import com.example.kkarhua.viewmodel.ProductListViewModel
import com.example.kkarhua.viewmodel.ProductListViewModelFactory

class ProductListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var txtError: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var productViewModel: ProductListViewModel
    private lateinit var cartViewModel: CartViewModel
    private lateinit var productAdapter: ProductAdapter

    private var allProducts = listOf<Product>()

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
        setupSearch()
        setupSwipeRefresh()
        observeProducts()
        observeLoadingState()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerProducts)
        etSearch = view.findViewById(R.id.etSearch)

        // Agregar estos elementos al layout si no existen
        progressBar = view.findViewById<ProgressBar?>(R.id.progressBar)
            ?: ProgressBar(requireContext()).apply {
                visibility = View.GONE
            }

        txtError = view.findViewById<TextView?>(R.id.txtError)
            ?: TextView(requireContext()).apply {
                visibility = View.GONE
            }

        swipeRefresh = view.findViewById<SwipeRefreshLayout?>(R.id.swipeRefresh)
            ?: SwipeRefreshLayout(requireContext())
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

    private fun setupSearch() {
        etSearch.addTextChangedListener { text ->
            filterProducts(text.toString())
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
            productAdapter.submitList(products)

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

    private fun filterProducts(query: String) {
        val filteredList = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true)
            }
        }
        productAdapter.submitList(filteredList)
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