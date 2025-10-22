package com.example.kkarhua.ui.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.kkarhua.R
import com.example.kkarhua.data.local.Product

class ProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onAddToCart: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view, onProductClick, onAddToCart)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))

        val animation = AnimationUtils.loadAnimation(
            holder.itemView.context,
            R.anim.item_animation_fall_down
        )
        holder.itemView.startAnimation(animation)
    }

    class ProductViewHolder(
        itemView: View,
        private val onProductClick: (Product) -> Unit,
        private val onAddToCart: (Product) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        private val txtProductName: TextView = itemView.findViewById(R.id.txtProductName)
        private val txtProductPrice: TextView = itemView.findViewById(R.id.txtProductPrice)
        private val txtProductCategory: TextView = itemView.findViewById(R.id.txtProductCategory)
        private val btnAddToCart: Button = itemView.findViewById(R.id.btnAddToCart)

        fun bind(product: Product) {
            txtProductName.text = product.name
            txtProductPrice.text = "$${String.format("%.0f", product.price)}"

            loadProductImage(product.image)

            itemView.setOnClickListener {
                onProductClick(product)
            }

            btnAddToCart.setOnClickListener {
                val animation = AnimationUtils.loadAnimation(
                    itemView.context,
                    R.anim.bounce
                )
                it.startAnimation(animation)
                onAddToCart(product)
            }
        }

        private fun loadProductImage(image: String) {
            when {
                image.isBlank() -> {
                    imgProduct.setImageResource(R.drawable.ic_launcher_background)
                }
                image.startsWith("http://") || image.startsWith("https://") -> {
                    Glide.with(itemView.context)
                        .load(image)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imgProduct)
                }
                image.startsWith("content://") -> {
                    Glide.with(itemView.context)
                        .load(image)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imgProduct)
                }
                else -> {
                    imgProduct.setImageResource(R.drawable.ic_launcher_background)
                }
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}