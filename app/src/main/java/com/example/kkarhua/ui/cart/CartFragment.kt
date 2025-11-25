package com.example.kkarhua.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kkarhua.R
import com.example.kkarhua.data.local.AppDatabase
import com.example.kkarhua.data.local.CartItem
import com.example.kkarhua.data.repository.AuthRepository
import com.example.kkarhua.data.repository.CartRepository
import com.example.kkarhua.viewmodel.CartViewModel
import com.example.kkarhua.viewmodel.CartViewModelFactory

class CartFragment : Fragment() {

    private lateinit var recyclerCartItems: RecyclerView
    private lateinit var txtEmptyCart: TextView
    private lateinit var txtSubtotal: TextView
    private lateinit var txtIva: TextView
    private lateinit var txtTotalPrice: TextView
    private lateinit var btnCheckout: Button
    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter
    private lateinit var authRepository: AuthRepository

    private val IVA_RATE = 0.19

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository(requireContext())

        setupViews(view)
        setupViewModel()
        setupRecyclerView()
        observeCart()
        setupListeners()
    }

    private fun setupViews(view: View) {
        recyclerCartItems = view.findViewById(R.id.recyclerCartItems)
        txtEmptyCart = view.findViewById(R.id.txtEmptyCart)
        txtSubtotal = view.findViewById(R.id.txtSubtotal)
        txtIva = view.findViewById(R.id.txtIva)
        txtTotalPrice = view.findViewById(R.id.txtTotalPrice)
        btnCheckout = view.findViewById(R.id.btnCheckout)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = CartRepository(database.cartDao())
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, factory).get(CartViewModel::class.java)
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChanged = { cartItem, newQuantity ->
                cartViewModel.updateQuantity(cartItem.productId, newQuantity)
            },
            onRemoveItem = { cartItem ->
                cartViewModel.removeFromCart(cartItem.productId)
                Toast.makeText(
                    requireContext(),
                    "${cartItem.productName} eliminado del carrito",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        recyclerCartItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun observeCart() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                showEmptyCart()
            } else {
                showCartItems(items)
            }
        }

        cartViewModel.totalPrice.observe(viewLifecycleOwner) { total ->
            updatePriceBreakdown(total)
        }
    }

    private fun updatePriceBreakdown(subtotal: Double) {
        val iva = subtotal * IVA_RATE
        val total = subtotal + iva

        txtSubtotal.text = "$${subtotal.toInt()}"
        txtIva.text = "$${iva.toInt()}"
        txtTotalPrice.text = "$${total.toInt()}"
    }

    private fun showEmptyCart() {
        recyclerCartItems.visibility = View.GONE
        txtEmptyCart.visibility = View.VISIBLE
        btnCheckout.isEnabled = false
        btnCheckout.alpha = 0.5f
    }

    private fun showCartItems(items: List<CartItem>) {
        recyclerCartItems.visibility = View.VISIBLE
        txtEmptyCart.visibility = View.GONE
        btnCheckout.isEnabled = true
        btnCheckout.alpha = 1.0f
        cartAdapter.submitList(items)
    }

    private fun setupListeners() {
        btnCheckout.setOnClickListener {
            if (!authRepository.isAuthenticated()) {
                Toast.makeText(
                    requireContext(),
                    "⚠️ Debes iniciar sesión para realizar una compra",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val items = cartViewModel.cartItems.value
            if (items.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "El carrito está vacío",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            findNavController().navigate(R.id.action_cartFragment_to_shippingAddressFragment)
        }
    }
}