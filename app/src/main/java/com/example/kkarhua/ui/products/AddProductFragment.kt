package com.example.kkarhua.ui.products

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.kkarhua.R
import com.example.kkarhua.data.local.AppDatabase
import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.repository.ProductRepository
import com.example.kkarhua.viewmodel.ProductListViewModel
import com.example.kkarhua.viewmodel.ProductListViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {

    private lateinit var etProductName: EditText
    private lateinit var etProductDescription: EditText
    private lateinit var etProductPrice: EditText
    private lateinit var etProductCategory: EditText
    private lateinit var etProductStock: EditText
    private lateinit var imgProductPhoto: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnAddProduct: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var productViewModel: ProductListViewModel

    private var selectedImageUri: Uri? = null

    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgProductPhoto)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupViewModel()
        setupListeners()
    }

    private fun setupViews(view: View) {
        etProductName = view.findViewById(R.id.etProductName)
        etProductDescription = view.findViewById(R.id.etProductDescription)
        etProductPrice = view.findViewById(R.id.etProductPrice)
        etProductStock = view.findViewById(R.id.etProductStock)
        imgProductPhoto = view.findViewById(R.id.imgProductPhoto)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        btnAddProduct = view.findViewById(R.id.btnAddProduct)
        btnCancel = view.findViewById(R.id.btnCancel)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val repository = ProductRepository(database.productDao())
        val factory = ProductListViewModelFactory(repository)
        productViewModel = ViewModelProvider(this, factory).get(ProductListViewModel::class.java)
    }

    private fun setupListeners() {
        btnSelectImage.setOnClickListener {
            getImage.launch("image/*")
        }

        btnAddProduct.setOnClickListener {
            attemptAddProduct()
        }

        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun attemptAddProduct() {
        val name = etProductName.text.toString().trim()
        val description = etProductDescription.text.toString().trim()
        val priceStr = etProductPrice.text.toString().trim()
        val category = etProductCategory.text.toString().trim()
        val stockStr = etProductStock.text.toString().trim()

        // Validaciones
        when {
            name.isEmpty() -> {
                etProductName.error = "El nombre es requerido"
                return
            }
            name.length < 3 -> {
                etProductName.error = "El nombre debe tener al menos 3 caracteres"
                return
            }
            description.isEmpty() -> {
                etProductDescription.error = "La descripción es requerida"
                return
            }
            description.length < 10 -> {
                etProductDescription.error = "La descripción debe tener al menos 10 caracteres"
                return
            }
            priceStr.isEmpty() -> {
                etProductPrice.error = "El precio es requerido"
                return
            }
            category.isEmpty() -> {
                etProductCategory.error = "La categoría es requerida"
                return
            }
            stockStr.isEmpty() -> {
                etProductStock.error = "El stock es requerido"
                return
            }
            selectedImageUri == null -> {
                Toast.makeText(
                    requireContext(),
                    "Debes seleccionar una imagen",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        try {
            val price = priceStr.toDouble()
            val stock = stockStr.toInt()

            if (price <= 0) {
                etProductPrice.error = "El precio debe ser mayor a 0"
                return
            }

            if (stock < 0) {
                etProductStock.error = "El stock no puede ser negativo"
                return
            }

            // Convertir URI a String para almacenar
            val imageUri = selectedImageUri.toString()

            // Crear el producto
            val product = Product(
                id = System.currentTimeMillis().toString(),
                name = name,
                description = description,
                price = price,
                image = imageUri,
                stock = stock
            )

            saveProduct(product)

        } catch (e: NumberFormatException) {
            Toast.makeText(
                requireContext(),
                "Verifica que el precio y stock sean números válidos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveProduct(product: Product) {
        progressBar.visibility = View.VISIBLE
        btnAddProduct.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(requireContext())
                database.productDao().insertProduct(product)

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnAddProduct.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        "✓ Producto agregado exitosamente",
                        Toast.LENGTH_LONG
                    ).show()

                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnAddProduct.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        "✗ Error al guardar el producto: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}