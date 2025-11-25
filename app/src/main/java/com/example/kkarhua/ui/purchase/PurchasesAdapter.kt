package com.example.kkarhua.ui.purchase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kkarhua.R
import com.example.kkarhua.data.remote.PurchaseWithDetails
import java.text.SimpleDateFormat
import java.util.*

class PurchasesAdapter(
    private val isAdmin: Boolean = false,
    private val onApprove: ((Int) -> Unit)? = null,
    private val onReject: ((Int) -> Unit)? = null
) : ListAdapter<PurchaseWithDetails, PurchasesAdapter.PurchaseViewHolder>(PurchaseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val layoutId = if (isAdmin) {
            R.layout.item_admin_purchase
        } else {
            R.layout.item_user_purchase
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return PurchaseViewHolder(view, isAdmin, onApprove, onReject)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PurchaseViewHolder(
        itemView: View,
        private val isAdmin: Boolean,
        private val onApprove: ((Int) -> Unit)?,
        private val onReject: ((Int) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val txtPurchaseId: TextView = itemView.findViewById(R.id.txtPurchaseId)
        private val txtPurchaseDate: TextView = itemView.findViewById(R.id.txtPurchaseDate)
        private val txtPurchaseStatus: TextView = itemView.findViewById(R.id.txtPurchaseStatus)
        private val txtPurchaseTotal: TextView = itemView.findViewById(R.id.txtPurchaseTotal)
        private val txtPurchaseAddress: TextView = itemView.findViewById(R.id.txtPurchaseAddress)
        private val txtPurchaseItems: TextView = itemView.findViewById(R.id.txtPurchaseItems)

        private val btnApprove: Button? = itemView.findViewById(R.id.btnApprove)
        private val btnReject: Button? = itemView.findViewById(R.id.btnReject)
        private val txtUserName: TextView? = itemView.findViewById(R.id.txtUserName)

        fun bind(purchaseDetails: PurchaseWithDetails) {
            val purchase = purchaseDetails.purchase

            txtPurchaseId.text = "Orden #${purchase.id}"

            // Formatear fecha
            val date = Date(purchase.createdAt)
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            txtPurchaseDate.text = format.format(date)

            // ✅ Estado con los valores correctos de Xano (minúsculas)
            val statusText = when (purchase.status.lowercase()) {
                "aprobado" -> "✅ Aprobada"
                "rechazado" -> "❌ Rechazada"
                else -> "⏳ Pendiente"
            }
            txtPurchaseStatus.text = statusText

            txtPurchaseStatus.setTextColor(
                when (purchase.status.lowercase()) {
                    "aprobado" -> android.graphics.Color.parseColor("#4CAF50")
                    "rechazado" -> android.graphics.Color.parseColor("#F44336")
                    else -> android.graphics.Color.parseColor("#FF9800")
                }
            )

            txtPurchaseTotal.text = "Total: $${purchase.total_amount.toInt()}"

            // Dirección
            val address = purchaseDetails.address
            if (address != null) {
                txtPurchaseAddress.text = "${address.address_line_1}, ${address.commune}, ${address.region}"
            } else {
                txtPurchaseAddress.text = "Dirección no disponible"
            }

            // Items
            val itemsText = purchaseDetails.items.joinToString("\n") { itemWithProduct ->
                "• ${itemWithProduct.productName} x${itemWithProduct.item.quantity} - $${itemWithProduct.item.price_at_purchase.toInt()}"
            }
            txtPurchaseItems.text = itemsText

            // ✅ Botones de admin solo si está pendiente
            if (isAdmin && purchase.status.lowercase() == "pendiente") {
                btnApprove?.visibility = View.VISIBLE
                btnReject?.visibility = View.VISIBLE

                btnApprove?.setOnClickListener {
                    onApprove?.invoke(purchase.id)
                }

                btnReject?.setOnClickListener {
                    onReject?.invoke(purchase.id)
                }
            } else {
                btnApprove?.visibility = View.GONE
                btnReject?.visibility = View.GONE
            }
        }
    }

    class PurchaseDiffCallback : DiffUtil.ItemCallback<PurchaseWithDetails>() {
        override fun areItemsTheSame(
            oldItem: PurchaseWithDetails,
            newItem: PurchaseWithDetails
        ): Boolean {
            return oldItem.purchase.id == newItem.purchase.id
        }

        override fun areContentsTheSame(
            oldItem: PurchaseWithDetails,
            newItem: PurchaseWithDetails
        ): Boolean {
            return oldItem == newItem
        }
    }
}