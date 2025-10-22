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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.kkarhua.R
import com.example.kkarhua.data.local.AppDatabase
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
    private lateinit var imgProductPhoto: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnAddProduct: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var productRepository: ProductRepository

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
        setupRepository()
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

    private fun setupRepository() {
        val database = AppDatabase.getInstance(requireContext())
        productRepository = ProductRepository(database.productDao())
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
        val stockStr = etProductStock.text.toString().trim()

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
                etProductPrice.requestFocus()
                return
            }

            if (stock < 0) {
                etProductStock.error = "El stock no puede ser negativo"
                etProductStock.requestFocus()
                return
            }

            uploadProductToXano(name, description, price, stock)

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
        stock: Int
    ) {
        progressBar.visibility = View.VISIBLE
        btnAddProduct.isEnabled = false
        btnAddProduct.text = "Subiendo..."

        lifecycleScope.launch {
            try {
                val imageFile = withContext(Dispatchers.IO) {
                    createImageFile(requireContext(), selectedImageUri!!)
                }

                if (imageFile == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error al procesar la imagen",
                            Toast.LENGTH_SHORT
                        ).show()
                        resetButton()
                    }
                    return@launch
                }

                val result = productRepository.createProductInApi(
                    name = name,
                    description = description,
                    price = price,
                    stock = stock,
                    imageFile = imageFile
                )

                withContext(Dispatchers.IO) {
                    imageFile.delete()
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