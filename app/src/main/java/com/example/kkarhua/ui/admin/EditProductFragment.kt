package com.example.kkarhua.ui.admin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.kkarhua.R
import com.example.kkarhua.data.local.AppDatabase
import com.example.kkarhua.data.local.Product
import com.example.kkarhua.data.repository.AuthRepository
import com.example.kkarhua.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EditProductFragment : Fragment() {

    private val args: EditProductFragmentArgs by navArgs()

    private lateinit var etProductName: EditText
    private lateinit var etProductDescription: EditText
    private lateinit var etProductPrice: EditText
    private lateinit var etProductStock: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var imgProductPhoto: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnUpdateProduct: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var productRepository: ProductRepository
    private lateinit var authRepository: AuthRepository

    private var selectedImageUri: Uri? = null
    private var currentProduct: Product? = null
    private var hasImageChanged = false

    private val categories = listOf(
        "Accesorios",
        "Joyería",
        "Bisutería",
        "Textil",
        "Cerámica",
        "Madera",
        "Cuero",
        "Otro"
    )

    private val getImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            hasImageChanged = true
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
        return inflater.inflate(R.layout.fragment_edit_product, container, false)
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
        setupRepository()
        setupCategorySpinner()
        setupListeners()
        loadProductData()
    }

    private fun setupViews(view: View) {
        etProductName = view.findViewById(R.id.etProductName)
        etProductDescription = view.findViewById(R.id.etProductDescription)
        etProductPrice = view.findViewById(R.id.etProductPrice)
        etProductStock = view.findViewById(R.id.etProductStock)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        imgProductPhoto = view.findViewById(R.id.imgProductPhoto)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        btnUpdateProduct = view.findViewById(R.id.btnUpdateProduct)
        btnCancel = view.findViewById(R.id.btnCancel)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupRepository() {
        val database = AppDatabase.getInstance(requireContext())
        productRepository = ProductRepository(database.productDao())
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        spinnerCategory.adapter = adapter
    }

    private fun setupListeners() {
        btnSelectImage.setOnClickListener {
            getImage.launch("image/*")
        }

        btnUpdateProduct.setOnClickListener {
            attemptUpdateProduct()
        }

        btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadProductData() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val product = productRepository.getProductById(args.productId)

                if (product != null) {
                    currentProduct = product
                    displayProductData(product)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error: Producto no encontrado",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    }
                }

                progressBar.visibility = View.GONE

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar producto: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun displayProductData(product: Product) {
        etProductName.setText(product.name)
        etProductDescription.setText(product.description)
        etProductPrice.setText(product.price.toString())
        etProductStock.setText(product.stock.toString())

        // Seleccionar categoría en el spinner
        val categoryPosition = categories.indexOf(product.category)
        if (categoryPosition >= 0) {
            spinnerCategory.setSelection(categoryPosition)
        }

        // Cargar imagen actual
        Glide.with(this)
            .load(product.image)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(imgProductPhoto)
    }

    private fun attemptUpdateProduct() {
        val name = etProductName.text.toString().trim()
        val description = etProductDescription.text.toString().trim()
        val priceStr = etProductPrice.text.toString().trim()
        val stockStr = etProductStock.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()

        when {
            name.isEmpty() -> {
                etProductName.error = "El nombre es requerido"
                etProductName.requestFocus()
                return
            }
            name.length < 3 -> {
                etProductName.error = "El nombre debe tener al menos 3 caracteres"
                etProductName.requestFocus()
                return
            }
            description.isEmpty() -> {
                etProductDescription.error = "La descripción es requerida"
                etProductDescription.requestFocus()
                return
            }
            description.length < 10 -> {
                etProductDescription.error = "La descripción debe tener al menos 10 caracteres"
                etProductDescription.requestFocus()
                return
            }
            priceStr.isEmpty() -> {
                etProductPrice.error = "El precio es requerido"
                etProductPrice.requestFocus()
                return
            }
            stockStr.isEmpty() -> {
                etProductStock.error = "El stock es requerido"
                etProductStock.requestFocus()
                return
            }
        }

        try {
            val price = priceStr.toDouble()
            val stock = stockStr.toInt()

            if (price <= 0) {
                etProductPrice.error = "El precio debe ser mayor a 0"
                etProductPrice.requestFocus()
                return
            }

            if (stock < 0) {
                etProductStock.error = "El stock no puede ser negativo"
                etProductStock.requestFocus()
                return
            }

            updateProductInXano(name, description, price, stock, category)

        } catch (e: NumberFormatException) {
            Toast.makeText(
                requireContext(),
                "Verifica que el precio y stock sean números válidos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateProductInXano(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        category: String
    ) {
        progressBar.visibility = View.VISIBLE
        btnUpdateProduct.isEnabled = false
        btnUpdateProduct.text = "Actualizando..."

        lifecycleScope.launch {
            try {
                val productId = currentProduct?.id?.toIntOrNull()

                if (productId == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error: ID de producto inválido",
                            Toast.LENGTH_SHORT
                        ).show()
                        resetButton()
                    }
                    return@launch
                }

                // Si cambió la imagen, procesarla
                val imageFile = if (hasImageChanged && selectedImageUri != null) {
                    withContext(Dispatchers.IO) {
                        createImageFile(requireContext(), selectedImageUri!!)
                    }
                } else {
                    null
                }

                val result = productRepository.updateProductInApi(
                    productId = productId,
                    name = name,
                    description = description,
                    price = price,
                    stock = stock,
                    category = category,
                    imageFile = imageFile
                )

                // Limpiar archivo temporal si existe
                imageFile?.let {
                    withContext(Dispatchers.IO) {
                        it.delete()
                    }
                }

                withContext(Dispatchers.Main) {
                    result.onSuccess {
                        Toast.makeText(
                            requireContext(),
                            "✓ Producto actualizado exitosamente",
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().navigateUp()
                    }.onFailure { exception ->
                        Toast.makeText(
                            requireContext(),
                            "✗ Error: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        resetButton()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "✗ Error inesperado: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    resetButton()
                }
            }
        }
    }

    private fun resetButton() {
        progressBar.visibility = View.GONE
        btnUpdateProduct.isEnabled = true
        btnUpdateProduct.text = "Actualizar Producto"
    }

    private fun createImageFile(context: Context, imageUri: Uri): File? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

            val resizedBitmap = resizeBitmap(bitmap, 1024, 1024)

            val tempFile = File(context.cacheDir, "temp_product_edit_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(tempFile)

            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            fos.flush()
            fos.close()

            tempFile

        } catch (e: Exception) {
            null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
}