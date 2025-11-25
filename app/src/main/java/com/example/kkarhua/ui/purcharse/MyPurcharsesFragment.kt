package com.example.kkarhua.ui.purchase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kkarhua.R
import com.example.kkarhua.data.remote.PurchaseWithDetails
import com.example.kkarhua.data.repository.AuthRepository
import com.example.kkarhua.data.repository.ProductRepository
import com.example.kkarhua.data.repository.PurchaseRepository
import com.example.kkarhua.data.local.AppDatabase
import kotlinx.coroutines.launch

class MyPurchasesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtEmpty: TextView
    private lateinit var authRepository: AuthRepository
    private lateinit var purchaseRepository: PurchaseRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var purchasesAdapter: PurchasesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_purchases, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRepositories()
        setupRecyclerView()
        loadPurchases()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerPurchases)
        progressBar = view.findViewById(R.id.progressBar)
        txtEmpty = view.findViewById(R.id.txtEmpty)
    }

    private fun setupRepositories() {
        authRepository = AuthRepository(requireContext())
        purchaseRepository = PurchaseRepository(authRepository)

        val database = AppDatabase.getInstance(requireContext())
        productRepository = ProductRepository(database.productDao())
    }

    private fun setupRecyclerView() {
        purchasesAdapter = PurchasesAdapter(isAdmin = false)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = purchasesAdapter
        }
    }

    private fun loadPurchases() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val userId = getUserId()
                if (userId == 0) {
                    showError("Error al obtener usuario")
                    return@launch
                }

                val purchasesResult = purchaseRepository.getUserPurchases(userId)

                if (purchasesResult.isFailure) {
                    showError("Error al cargar compras: ${purchasesResult.exceptionOrNull()?.message}")
                    return@launch
                }

                val purchases = purchasesResult.getOrNull() ?: emptyList()

                if (purchases.isEmpty()) {
                    txtEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    txtEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    // Cargar detalles de cada compra
                    val purchasesWithDetails = purchases.map { purchase ->
                        loadPurchaseDetails(purchase.id, purchase)
                    }

                    purchasesAdapter.submitList(purchasesWithDetails)
                }

                progressBar.visibility = View.GONE

            } catch (e: Exception) {
                showError("Error: ${e.message}")
            }
        }
    }

    private suspend fun loadPurchaseDetails(
        purchaseId: Int,
        purchase: com.example.kkarhua.data.remote.PurchaseResponse
    ): PurchaseWithDetails {
        // Cargar dirección
        val addressResult = purchaseRepository.getAddressById(purchase.address_id)
        val address = addressResult.getOrNull()

        // Cargar items
        val itemsResult = purchaseRepository.getPurchaseItems(purchaseId)
        val items = itemsResult.getOrNull() ?: emptyList()

        // Cargar nombres de productos
        val itemsWithProducts = items.map { item ->
            val product = productRepository.getProductById(item.product_id.toString())
            com.example.kkarhua.data.remote.PurchaseItemWithProduct(
                item = item,
                productName = product?.name ?: "Producto desconocido",
                productImage = product?.image ?: ""
            )
        }

        return PurchaseWithDetails(
            purchase = purchase,
            address = address,
            items = itemsWithProducts
        )
    }

    private fun getUserId(): Int {
        val prefs = requireContext().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getInt("user_id", 0)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        progressBar.visibility = View.GONE
        txtEmpty.visibility = View.VISIBLE
        txtEmpty.text = "Error al cargar compras"
    }
}