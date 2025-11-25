package com.example.kkarhua.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kkarhua.R
import com.example.kkarhua.data.local.AppDatabase
import com.example.kkarhua.data.remote.PurchaseWithDetails
import com.example.kkarhua.data.repository.AuthRepository
import com.example.kkarhua.data.repository.ProductRepository
import com.example.kkarhua.data.repository.PurchaseRepository
import com.example.kkarhua.ui.purchase.PurchasesAdapter
import kotlinx.coroutines.launch

class AdminManagePurchasesFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_admin_manage_purchases, container, false)
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
        purchaseRepository = PurchaseRepository(authRepository)
        val database = AppDatabase.getInstance(requireContext())
        productRepository = ProductRepository(database.productDao())
    }

    private fun setupRecyclerView() {
        purchasesAdapter = PurchasesAdapter(
            isAdmin = true,
            onApprove = { purchaseId ->
                confirmStatusChange(purchaseId, "aprobado", "Aprobar")
            },
            onReject = { purchaseId ->
                confirmStatusChange(purchaseId, "rechazado", "Rechazar")
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = purchasesAdapter
        }
    }

    private fun confirmStatusChange(purchaseId: Int, newStatus: String, actionName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("$actionName compra")
            .setMessage("¿Estás seguro de que deseas $actionName la orden #$purchaseId?")
            .setPositiveButton(actionName) { _, _ ->
                updatePurchaseStatus(purchaseId, newStatus)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updatePurchaseStatus(purchaseId: Int, newStatus: String) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = purchaseRepository.updatePurchaseStatus(purchaseId, newStatus)

                if (result.isSuccess) {
                    val statusText = when (newStatus) {
                        "aprobado" -> "aprobada"
                        "rechazado" -> "rechazada"
                        else -> "actualizada"
                    }
                    Toast.makeText(
                        requireContext(),
                        "✓ Orden #$purchaseId $statusText exitosamente",
                        Toast.LENGTH_LONG
                    ).show()

                    // Recargar lista
                    loadPurchases()
                } else {
                    showError("Error al actualizar estado: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            }
        }
    }

    private fun loadPurchases() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val purchasesResult = purchaseRepository.getAllPurchases()

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
        val addressResult = purchaseRepository.getAddressById(purchase.address_id)
        val address = addressResult.getOrNull()

        val itemsResult = purchaseRepository.getPurchaseItems(purchaseId)
        val items = itemsResult.getOrNull() ?: emptyList()

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

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        progressBar.visibility = View.GONE
        txtEmpty.visibility = View.VISIBLE
        txtEmpty.text = "Error al cargar compras"
    }
}