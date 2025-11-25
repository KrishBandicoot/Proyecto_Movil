package com.example.kkarhua.ui.products

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
import com.bumptech.glide.Glide
import com.example.kkarhua.R
import com.example.kkarhua.data.local.AppDatabase
import com.example.kkarhua.data.repository.AuthRepository
import com.example.kkarhua.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AddProductFragment : Fragment() {

    private lateinit var etProductName: EditText
    private lateinit var etProductDescription: EditText
    private lateinit var etProductPrice: EditText
    private lateinit var etProductStock: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var imgProductPhoto: ImageView
    private lateinit var imgProductPhoto2: ImageView // ✅ NUEVO
    private lateinit var imgProductPhoto3: ImageView // ✅ NUEVO
    private lateinit var btnSelectImage: Button
    private lateinit var btnSelectImage2: Button // ✅ NUEVO
    private lateinit var btnSelectImage3: Button // ✅ NUEVO
    private lateinit var btnAddProduct: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var productRepository: ProductRepository
    private lateinit var authRepository: AuthRepository

    private var selectedImageUri: Uri? = null
    private var selectedImageUri2: Uri? = null // ✅ NUEVO
    private var selectedImageUri3: Uri? = null // ✅ NUEVO

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

    // ✅ ACTUALIZADO: Ahora manejamos 3 imágenes
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

    private val getImage2 = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri2 = uri
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgProductPhoto2)
        }
    }

    private val getImage3 = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri3 = uri
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgProductPhoto3)
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

        authRepository = AuthRepository(requireContext())

        if (!authRepository.isAdmin()) {
            Toast.makeText(
                requireContext(),
                "⚠️ Acceso denegado: Solo administradores pueden agregar productos",
                Toast.LENGTH_LONG
            ).show()
            findNavController().navigateUp()
            return
        }

        setupViews(view)
        setupRepository()
        setupCategorySpinner()
        setupListeners()
    }

    private fun setupViews(view: View) {
        etProductName = view.findViewById(R.id.etProductName)
        etProductDescription = view.findViewById(R.id.etProductDescription)
        etProductPrice = view.findViewById(R.id.etProductPrice)
        etProductStock = view.findViewById(R.id.etProductStock)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        imgProductPhoto = view.findViewById(R.id.imgProductPhoto)
        imgProductPhoto2 = view.findViewById(R.id.imgProductPhoto2) // ✅ NUEVO
        imgProductPhoto3 = view.findViewById(R.id.imgProductPhoto3) // ✅ NUEVO
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        btnSelectImage2 = view.findViewById(R.id.btnSelectImage2) // ✅ NUEVO
        btnSelectImage3 = view.findViewById(R.id.btnSelectImage3) // ✅ NUEVO
        btnAddProduct = view.findViewById(R.id.btnAddProduct)
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

        // ✅ NUEVO: Botones para imagen 2 y 3
        btnSelectImage2.setOnClickListener {
            getImage2.launch("image/*")
        }

        btnSelectImage3.setOnClickListener {
            getImage3.launch("image/*")
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
            selectedImageUri == null -> {
                Toast.makeText(
                    requireContext(),
                    "Debes seleccionar al menos la imagen principal",
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
                etProductPrice.requestFocus()
                return
            }

            if (stock < 0) {
                etProductStock.error = "El stock no puede ser negativo"
                etProductStock.requestFocus()
                return
            }

            uploadProductToXano(name, description, price, stock, category)

        } catch (e: NumberFormatException) {
            Toast.makeText(
                requireContext(),
                "Verifica que el precio y stock sean números válidos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun uploadProductToXano(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        category: String
    ) {
        progressBar.visibility = View.VISIBLE
        btnAddProduct.isEnabled = false
        btnAddProduct.text = "Subiendo..."

        lifecycleScope.launch {
            try {
                // ✅ Crear archivo para imagen principal
                val imageFile = withContext(Dispatchers.IO) {
                    createImageFile(requireContext(), selectedImageUri!!)
                }

                if (imageFile == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error al procesar la imagen principal",
                            Toast.LENGTH_SHORT
                        ).show()
                        resetButton()
                    }
                    return@launch
                }

                // ✅ NUEVO: Crear archivos para imagen2 e imagen3 si existen
                val imageFile2 = selectedImageUri2?.let { uri ->
                    withContext(Dispatchers.IO) {
                        createImageFile(requireContext(), uri)
                    }
                }

                val imageFile3 = selectedImageUri3?.let { uri ->
                    withContext(Dispatchers.IO) {
                        createImageFile(requireContext(), uri)
                    }
                }

                val result = productRepository.createProductInApi(
                    name = name,
                    description = description,
                    price = price,
                    stock = stock,
                    category = category,
                    imageFile = imageFile,
                    imageFile2 = imageFile2, // ✅ NUEVO
                    imageFile3 = imageFile3  // ✅ NUEVO
                )

                // ✅ Limpiar archivos temporales
                withContext(Dispatchers.IO) {
                    imageFile.delete()
                    imageFile2?.delete()
                    imageFile3?.delete()
                }

                withContext(Dispatchers.Main) {
                    result.onSuccess {
                        Toast.makeText(
                            requireContext(),
                            "✓ Producto creado exitosamente",
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
        btnAddProduct.isEnabled = true
        btnAddProduct.text = "Agregar Producto"
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

            val tempFile = File(context.cacheDir, "temp_product_${System.currentTimeMillis()}.jpg")
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