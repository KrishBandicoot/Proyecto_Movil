package com.example.kkarhua.ui.productdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.kkarhua.R
import com.example.kkarhua.data.local.AppDatabase
import com.example.kkarhua.data.local.CartItem
import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.repository.CartRepository
import com.example.kkarhua.data.repository.ProductRepository
import com.example.kkarhua.viewmodel.CartViewModel
import com.example.kkarhua.viewmodel.CartViewModelFactory
import kotlinx.coroutines.launch

class ProductDetailFragment : Fragment() {

    private val args: ProductDetailFragmentArgs by navArgs()

    private lateinit var imgProduct: ImageView
    private lateinit var txtProductName: TextView
    private lateinit var txtProductPrice: TextView
    private lateinit var txtProductDescription: TextView
    private lateinit var txtProductCategory: TextView
    private lateinit var btnAgregarCarrito: Button
    private lateinit var cartViewModel: CartViewModel

    private var currentProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupViewModel()
        loadProductDetails()
        setupListeners()
        animateEntrance()
    }

    private fun setupViews(view: View) {
        imgProduct = view.findViewById(R.id.imgProduct)
        txtProductName = view.findViewById(R.id.txtProductName)
        txtProductPrice = view.findViewById(R.id.txtProductPrice)
        txtProductDescription = view.findViewById(R.id.txtProductDescription)
        txtProductCategory = view.findViewById(R.id.txtProductCategory)
        btnAgregarCarrito = view.findViewById(R.id.btnAgregarCarrito)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = CartRepository(database.cartDao())
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, factory).get(CartViewModel::class.java)
    }

    private fun loadProductDetails() {
        val database = AppDatabase.getInstance(requireContext())
        val productRepository = ProductRepository(database.productDao())

        lifecycleScope.launch {
            val product = productRepository.getProductById(args.productId)
            product?.let {
                currentProduct = it
                displayProduct(it)
            }
        }
    }

    private fun displayProduct(product: Product) {
        txtProductName.text = product.name
        txtProductPrice.text = "$${String.format("%.0f", product.price)}"
        txtProductDescription.text = product.description

        Glide.with(requireContext())
            .load(product.image)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(imgProduct)
    }

    private fun setupListeners() {
        btnAgregarCarrito.setOnClickListener {
            animateButton(it)
            addToCart()
        }
    }

    private fun animateEntrance() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        imgProduct.startAnimation(fadeIn)
        txtProductName.startAnimation(slideUp)
        txtProductPrice.startAnimation(slideUp)
    }

    private fun animateButton(view: View) {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        view.startAnimation(animation)
    }

    private fun addToCart() {
        currentProduct?.let { product ->
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
}