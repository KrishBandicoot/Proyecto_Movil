package com.example.kkarhua.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kkarhua.R
import com.example.kkarhua.data.local.CartItem

class CartAdapter(
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view, onQuantityChanged, onRemoveItem)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CartViewHolder(
        itemView: View,
        private val onQuantityChanged: (CartItem, Int) -> Unit,
        private val onRemoveItem: (CartItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val imgProduct: ImageView = itemView.findViewById(R.id.imgCartProduct)
        private val txtProductName: TextView = itemView.findViewById(R.id.txtCartProductName)
        private val txtProductPrice: TextView = itemView.findViewById(R.id.txtCartProductPrice)
        private val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        private val btnDecrease: ImageButton = itemView.findViewById(R.id.btnDecrease)
        private val btnIncrease: ImageButton = itemView.findViewById(R.id.btnIncrease)
        private val btnRemove: Button = itemView.findViewById(R.id.btnRemove)

        fun bind(cartItem: CartItem) {
            txtProductName.text = cartItem.productName
            txtProductPrice.text = "$${(cartItem.price * cartItem.quantity).toInt()}"
            txtQuantity.text = cartItem.quantity.toString()

            Glide.with(itemView.context)
                .load(cartItem.image)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imgProduct)

            btnDecrease.setOnClickListener {
                val newQuantity = cartItem.quantity - 1
                if (newQuantity >= 0) {
                    animateButton(it)
                    onQuantityChanged(cartItem, newQuantity)
                }
            }

            btnIncrease.setOnClickListener {
                val newQuantity = cartItem.quantity + 1
                animateButton(it)
                onQuantityChanged(cartItem, newQuantity)
            }

            btnRemove.setOnClickListener {
                animateButton(it)
                onRemoveItem(cartItem)
            }
        }

        private fun animateButton(view: View) {
            val animation = AnimationUtils.loadAnimation(
                view.context,
                R.anim.bounce
            )
            view.startAnimation(animation)
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}