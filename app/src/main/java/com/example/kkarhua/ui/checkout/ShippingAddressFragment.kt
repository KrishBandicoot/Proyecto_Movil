package com.example.kkarhua.ui.checkout

import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.flow.first

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
    private val TAG = "ShippingAddress"

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

        spinnerRegion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRegion = regions[position]
                updateCommuneSpinner(selectedRegion)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

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
                val prefs = requireContext().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                val userId = prefs.getInt("user_id", 0)

                Log.d(TAG, "========================================")
                Log.d(TAG, "INICIANDO PROCESO DE COMPRA")
                Log.d(TAG, "========================================")
                Log.d(TAG, "User ID: $userId")
                Log.d(TAG, "Address: $addressLine")
                Log.d(TAG, "Apartment: $apartment")
                Log.d(TAG, "Region: $region")
                Log.d(TAG, "Commune: $commune")
                Log.d(TAG, "Instructions: $instructions")

                if (userId == 0) {
                    showError("Error: Usuario no identificado. Por favor, inicia sesión nuevamente.")
                    return@launch
                }

                // Obtener items del carrito
                val database = AppDatabase.getInstance(requireContext())
                val cartDao = database.cartDao()
                val cartItems = cartDao.getAllCartItems().first()

                Log.d(TAG, "Items en carrito: ${cartItems.size}")
                for (item in cartItems) {
                    Log.d(TAG, "  - ${item.productName} x${item.quantity} = $${item.price}")
                }

                if (cartItems.isEmpty()) {
                    showError("El carrito está vacío")
                    return@launch
                }

                // Calcular total con IVA
                val subtotal = cartItems.sumOf { it.price * it.quantity }
                val iva = subtotal * IVA_RATE
                val total = subtotal + iva

                Log.d(TAG, "Subtotal: $subtotal")
                Log.d(TAG, "IVA: $iva")
                Log.d(TAG, "Total: $total")

                // 1. Crear dirección
                Log.d(TAG, "========================================")
                Log.d(TAG, "PASO 1: CREAR DIRECCIÓN")
                Log.d(TAG, "========================================")

                val addressResult = purchaseRepository.createAddress(
                    addressLine1 = addressLine,
                    apartmentNumber = apartment,
                    region = region,
                    commune = commune,
                    shippingInstructions = instructions,
                    userId = userId
                )

                if (addressResult.isFailure) {
                    val error = addressResult.exceptionOrNull()
                    Log.e(TAG, "✗ Error creando dirección", error)
                    Log.e(TAG, "Mensaje: ${error?.message}")
                    showError("Error al crear dirección: ${error?.message}")
                    return@launch
                }

                val address = addressResult.getOrNull()!!
                Log.d(TAG, "✓ Dirección creada exitosamente")
                Log.d(TAG, "  ID: ${address.id}")
                Log.d(TAG, "  address_line_1: ${address.address_line_1}")
                Log.d(TAG, "  region: ${address.region}")
                Log.d(TAG, "  commune: ${address.commune}")

                // 2. Crear compra
                Log.d(TAG, "========================================")
                Log.d(TAG, "PASO 2: CREAR COMPRA")
                Log.d(TAG, "========================================")
                Log.d(TAG, "Parámetros:")
                Log.d(TAG, "  userId: $userId")
                Log.d(TAG, "  addressId: ${address.id}")
                Log.d(TAG, "  totalAmount: $total")

                val purchaseResult = purchaseRepository.createPurchase(
                    userId = userId,
                    addressId = address.id,
                    totalAmount = total
                )

                Log.d(TAG, "Purchase result isSuccess: ${purchaseResult.isSuccess}")
                Log.d(TAG, "Purchase result isFailure: ${purchaseResult.isFailure}")

                if (purchaseResult.isFailure) {
                    val error = purchaseResult.exceptionOrNull()
                    Log.e(TAG, "✗ Error creando compra", error)
                    Log.e(TAG, "Mensaje completo: ${error?.message}")
                    Log.e(TAG, "Tipo de error: ${error?.javaClass?.simpleName}")
                    error?.printStackTrace()
                    showError("Error al crear compra: ${error?.message}")
                    return@launch
                }

                val purchase = purchaseResult.getOrNull()

                if (purchase == null) {
                    Log.e(TAG, "✗ Purchase es null después de éxito")
                    showError("Error: La compra se creó pero no se recibió respuesta")
                    return@launch
                }

                Log.d(TAG, "✓ Compra creada exitosamente")
                Log.d(TAG, "  ID: ${purchase.id}")
                Log.d(TAG, "  status: ${purchase.status}")
                Log.d(TAG, "  total_amount: ${purchase.total_amount}")

                // 3. Crear items de compra
                Log.d(TAG, "========================================")
                Log.d(TAG, "PASO 3: CREAR ITEMS DE COMPRA")
                Log.d(TAG, "========================================")

                for ((index, cartItem) in cartItems.withIndex()) {
                    val productId = cartItem.productId.toIntOrNull()

                    if (productId == null) {
                        Log.e(TAG, "✗ ProductId inválido en item $index: ${cartItem.productId}")
                        continue
                    }

                    Log.d(TAG, "Item ${index + 1}/${cartItems.size}:")
                    Log.d(TAG, "  purchaseId: ${purchase.id}")
                    Log.d(TAG, "  productId: $productId")
                    Log.d(TAG, "  quantity: ${cartItem.quantity}")
                    Log.d(TAG, "  price: ${cartItem.price}")

                    val itemResult = purchaseRepository.createPurchaseItem(
                        purchaseId = purchase.id,
                        productId = productId,
                        quantity = cartItem.quantity,
                        priceAtPurchase = cartItem.price
                    )

                    if (itemResult.isFailure) {
                        val error = itemResult.exceptionOrNull()
                        Log.e(TAG, "✗ Error creando item ${index + 1}", error)
                        Log.e(TAG, "Mensaje: ${error?.message}")
                    } else {
                        Log.d(TAG, "✓ Item ${index + 1} creado exitosamente")
                    }
                }

                // 4. Limpiar carrito
                Log.d(TAG, "========================================")
                Log.d(TAG, "PASO 4: LIMPIAR CARRITO")
                Log.d(TAG, "========================================")
                cartViewModel.clearCart()
                Log.d(TAG, "✓ Carrito limpiado")

                // 5. Mostrar mensaje y regresar
                Toast.makeText(
                    requireContext(),
                    "✓ Compra realizada exitosamente\n\n📋 Orden #${purchase.id}\n⏳ Estado: Pendiente de aprobación",
                    Toast.LENGTH_LONG
                ).show()

                Log.d(TAG, "========================================")
                Log.d(TAG, "COMPRA COMPLETADA EXITOSAMENTE")
                Log.d(TAG, "Orden #${purchase.id}")
                Log.d(TAG, "========================================")

                findNavController().navigate(R.id.action_shippingAddressFragment_to_cartFragment)

            } catch (e: Exception) {
                Log.e(TAG, "========================================")
                Log.e(TAG, "EXCEPCIÓN NO CONTROLADA")
                Log.e(TAG, "========================================")
                Log.e(TAG, "Tipo: ${e.javaClass.simpleName}")
                Log.e(TAG, "Mensaje: ${e.message}")
                Log.e(TAG, "StackTrace completo:")
                e.printStackTrace()
                Log.e(TAG, "========================================")
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