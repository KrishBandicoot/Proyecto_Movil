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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kkarhua.R
import com.example.kkarhua.data.local.AppDatabase
import com.example.kkarhua.data.local.CartItem
import com.example.kkarhua.data.repository.CartRepository
import com.example.kkarhua.viewmodel.CartViewModel
import com.example.kkarhua.viewmodel.CartViewModelFactory

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtEmptyCart: TextView
    private lateinit var txtTotalPrice: TextView
    private lateinit var btnCheckout: Button
    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupViewModel()
        setupRecyclerView()
        observeCart()
        setupListeners()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerCartItems)
        txtEmptyCart = view.findViewById(R.id.txtEmptyCart)
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

        recyclerView.apply {
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
            txtTotalPrice.text = "$${total.toInt()}"
        }
    }

    private fun showEmptyCart() {
        txtEmptyCart.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        btnCheckout.isEnabled = false
    }

    private fun showCartItems(items: List<CartItem>) {
        txtEmptyCart.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        btnCheckout.isEnabled = true
        cartAdapter.submitList(items)
    }

    private fun setupListeners() {
        btnCheckout.setOnClickListener {
            val itemCount = cartViewModel.itemCount.value ?: 0
            if (itemCount > 0) {
                Toast.makeText(
                    requireContext(),
                    "Procesando compra de $itemCount productos...",
                    Toast.LENGTH_LONG
                ).show()

                // Aquí puedes agregar la lógica de checkout
                // Por ahora, simplemente limpiamos el carrito
                cartViewModel.clearCart()
            }
        }
    }
}