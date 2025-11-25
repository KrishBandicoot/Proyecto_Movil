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
    private lateinit var spinnerCommune: Spinner
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
        spinnerCommune = view.findViewById(R.id.spinnerCommune)
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

        // ✅ Listener para actualizar comunas cuando cambia la región
        spinnerRegion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRegion = regions[position]
                updateCommuneSpinner(selectedRegion)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Cargar comunas de la primera región por defecto
        if (regions.isNotEmpty()) {
            updateCommuneSpinner(regions[0])
        }
    }

    private fun updateCommuneSpinner(regionName: String) {
        val communes = ChileanRegion.getCommunesByRegion(regionName)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            communes
        )
        spinnerCommune.adapter = adapter
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
        val commune = spinnerCommune.selectedItem.toString()
        val instructions = etInstructions.text.toString().trim()

        when {
            addressLine.isEmpty() -> {
                etAddressLine.error = "La dirección es requerida"
                etAddressLine.requestFocus()
                return
            }
            commune.isEmpty() -> {
                Toast.makeText(requireContext(), "Selecciona una comuna", Toast.LENGTH_SHORT).show()
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
                // ✅ FIX: Obtener userId correctamente desde SharedPreferences
                val prefs = requireContext().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                val userId = prefs.getInt("user_id", 0)

                android.util.Log.d("ShippingAddress", "========================================")
                android.util.Log.d("ShippingAddress", "DATOS DE COMPRA")
                android.util.Log.d("ShippingAddress", "========================================")
                android.util.Log.d("ShippingAddress", "User ID: $userId")
                android.util.Log.d("ShippingAddress", "Address: $addressLine")
                android.util.Log.d("ShippingAddress", "Region: $region")
                android.util.Log.d("ShippingAddress", "Commune: $commune")

                if (userId == 0) {
                    showError("Error: Usuario no identificado. Por favor, inicia sesión nuevamente.")
                    return@launch
                }

                val cartItems = cartViewModel.cartItems.value
                if (cartItems.isNullOrEmpty()) {
                    showError("El carrito está vacío")
                    return@launch
                }

                // Calcular total con IVA
                val subtotal = cartItems.sumOf { it.price * it.quantity }
                val iva = subtotal * IVA_RATE
                val total = subtotal + iva

                android.util.Log.d("ShippingAddress", "Subtotal: $subtotal")
                android.util.Log.d("ShippingAddress", "IVA: $iva")
                android.util.Log.d("ShippingAddress", "Total: $total")

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
                android.util.Log.d("ShippingAddress", "✓ Dirección creada: ${address.id}")

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
                android.util.Log.d("ShippingAddress", "✓ Compra creada: ${purchase.id}")

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
                        android.util.Log.e("ShippingAddress", "Error creando item: ${itemResult.exceptionOrNull()?.message}")
                    } else {
                        android.util.Log.d("ShippingAddress", "✓ Item creado para producto $productId")
                    }
                }

                // 4. Limpiar carrito
                cartViewModel.clearCart()
                android.util.Log.d("ShippingAddress", "✓ Carrito limpiado")

                // 5. Mostrar mensaje y regresar
                Toast.makeText(
                    requireContext(),
                    "✓ Compra realizada exitosamente\n\n📋 Orden #${purchase.id}\n⏳ Estado: Pendiente de aprobación",
                    Toast.LENGTH_LONG
                ).show()

                android.util.Log.d("ShippingAddress", "========================================")
                android.util.Log.d("ShippingAddress", "COMPRA COMPLETADA")
                android.util.Log.d("ShippingAddress", "========================================")

                findNavController().navigate(R.id.action_shippingAddressFragment_to_cartFragment)

            } catch (e: Exception) {
                android.util.Log.e("ShippingAddress", "✗ Exception: ${e.message}", e)
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