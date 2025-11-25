package com.example.kkarhua.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.kkarhua.R
import com.example.kkarhua.data.local.AppDatabase
import com.example.kkarhua.data.remote.ChileanRegion
import com.example.kkarhua.data.repository.AuthRepository
import com.example.kkarhua.data.repository.CartRepository
import com.example.kkarhua.data.repository.ProductRepository
import com.example.kkarhua.data.repository.PurchaseRepository
import com.example.kkarhua.viewmodel.CartViewModel
import com.example.kkarhua.viewmodel.CartViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ShippingAddressFragment : Fragment() {

    private lateinit var etAddressLine: TextInputEditText
    private lateinit var etApartment: TextInputEditText
    private lateinit var spinnerRegion: Spinner
    private lateinit var etCommune: TextInputEditText
    private lateinit var etInstructions: TextInputEditText
    private lateinit var btnConfirmPurchase: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var authRepository: AuthRepository
    private lateinit var purchaseRepository: PurchaseRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var cartViewModel: CartViewModel

    private val IVA_RATE = 0.19

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shipping_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRepositories()
        setupViewModel()
        setupRegionSpinner()
        setupListeners()
    }

    private fun setupViews(view: View) {
        etAddressLine = view.findViewById(R.id.etAddressLine)
        etApartment = view.findViewById(R.id.etApartment)
        spinnerRegion = view.findViewById(R.id.spinnerRegion)
        etCommune = view.findViewById(R.id.etCommune)
        etInstructions = view.findViewById(R.id.etInstructions)
        btnConfirmPurchase = view.findViewById(R.id.btnConfirmPurchase)
        btnCancel = view.findViewById(R.id.btnCancel)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupRepositories() {
        authRepository = AuthRepository(requireContext())
        purchaseRepository = PurchaseRepository(authRepository)

        val database = AppDatabase.getInstance(requireContext())
        productRepository = ProductRepository(database.productDao())
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = CartRepository(database.cartDao())
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, factory).get(CartViewModel::class.java)
    }

    private fun setupRegionSpinner() {
        val regions = ChileanRegion.getAllRegions()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            regions
        )
        spinnerRegion.adapter = adapter
    }

    private fun setupListeners() {
        btnConfirmPurchase.setOnClickListener {
            attemptPurchase()
        }

        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun attemptPurchase() {
        val addressLine = etAddressLine.text.toString().trim()
        val apartment = etApartment.text.toString().trim()
        val region = spinnerRegion.selectedItem.toString()
        val commune = etCommune.text.toString().trim()
        val instructions = etInstructions.text.toString().trim()

        // Validaciones
        when {
            addressLine.isEmpty() -> {
                etAddressLine.error = "La dirección es requerida"
                etAddressLine.requestFocus()
                return
            }
            commune.isEmpty() -> {
                etCommune.error = "La comuna es requerida"
                etCommune.requestFocus()
                return
            }
        }

        processPurchase(addressLine, apartment, region, commune, instructions)
    }

    private fun processPurchase(
        addressLine: String,
        apartment: String,
        region: String,
        commune: String,
        instructions: String
    ) {
        progressBar.visibility = View.VISIBLE
        btnConfirmPurchase.isEnabled = false
        btnConfirmPurchase.text = "Procesando..."

        lifecycleScope.launch {
            try {
                // Obtener datos del usuario
                val userId = authRepository.getUserId()
                if (userId == 0) {
                    showError("Error: Usuario no identificado")
                    return@launch
                }

                // Obtener items del carrito
                val cartItems = cartViewModel.cartItems.value
                if (cartItems.isNullOrEmpty()) {
                    showError("El carrito está vacío")
                    return@launch
                }

                // Calcular total con IVA
                val subtotal = cartItems.sumOf { it.price * it.quantity }
                val iva = subtotal * IVA_RATE
                val total = subtotal + iva

                // 1. Crear dirección
                val addressResult = purchaseRepository.createAddress(
                    addressLine1 = addressLine,
                    apartmentNumber = apartment,
                    region = region,
                    commune = commune,
                    shippingInstructions = instructions,
                    userId = userId
                )

                if (addressResult.isFailure) {
                    showError("Error al crear dirección: ${addressResult.exceptionOrNull()?.message}")
                    return@launch
                }

                val address = addressResult.getOrNull()!!

                // 2. Crear compra
                val purchaseResult = purchaseRepository.createPurchase(
                    userId = userId,
                    addressId = address.id,
                    totalAmount = total
                )

                if (purchaseResult.isFailure) {
                    showError("Error al crear compra: ${purchaseResult.exceptionOrNull()?.message}")
                    return@launch
                }

                val purchase = purchaseResult.getOrNull()!!

                // 3. Crear items de compra
                for (cartItem in cartItems) {
                    val productId = cartItem.productId.toIntOrNull() ?: continue

                    val itemResult = purchaseRepository.createPurchaseItem(
                        purchaseId = purchase.id,
                        productId = productId,
                        quantity = cartItem.quantity,
                        priceAtPurchase = cartItem.price
                    )

                    if (itemResult.isFailure) {
                        android.util.Log.e("Purchase", "Error creating item: ${itemResult.exceptionOrNull()?.message}")
                    }
                }

                // 4. Limpiar carrito
                cartViewModel.clearCart()

                // 5. Mostrar mensaje y regresar
                Toast.makeText(
                    requireContext(),
                    "✓ Compra realizada exitosamente\n\nEstado: Pendiente de aprobación",
                    Toast.LENGTH_LONG
                ).show()

                findNavController().navigate(R.id.action_shippingAddressFragment_to_cartFragment)

            } catch (e: Exception) {
                showError("Error inesperado: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        progressBar.visibility = View.GONE
        btnConfirmPurchase.isEnabled = true
        btnConfirmPurchase.text = "Confirmar Compra"
    }
}

// ✅ Extensión para obtener user ID
private fun AuthRepository.getUserId(): Int {
    return try {
        // Asumiendo que guardas el user ID en SharedPreferences
        val prefs = android.content.Context.MODE_PRIVATE
        val sharedPrefs = android.app.Application().getSharedPreferences("auth_prefs", prefs)
        sharedPrefs.getInt("user_id", 0)
    } catch (e: Exception) {
        0
    }
}