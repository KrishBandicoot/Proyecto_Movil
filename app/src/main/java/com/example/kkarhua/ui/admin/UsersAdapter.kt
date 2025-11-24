package com.example.kkarhua.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kkarhua.R
import com.example.kkarhua.data.remote.UserResponse
import java.text.SimpleDateFormat
import java.util.*

class UsersAdapter(
    private val onEditClick: (UserResponse) -> Unit,
    private val onDeleteClick: (UserResponse) -> Unit
) : ListAdapter<UserResponse, UsersAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        itemView: View,
        private val onEditClick: (UserResponse) -> Unit,
        private val onDeleteClick: (UserResponse) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val txtUserName: TextView = itemView.findViewById(R.id.txtUserName)
        private val txtUserEmail: TextView = itemView.findViewById(R.id.txtUserEmail)
        private val txtUserRole: TextView = itemView.findViewById(R.id.txtUserRole)
        private val txtUserDate: TextView = itemView.findViewById(R.id.txtUserDate)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(user: UserResponse) {
            txtUserName.text = user.name
            txtUserEmail.text = user.email

            // Mostrar rol con estilo
            txtUserRole.text = when (user.role) {
                "admin" -> "⚙️ Administrador"
                else -> "👤 Usuario"
            }

            // Formatear fecha
            user.created_at?.let { timestamp ->
                val date = Date(timestamp)
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                txtUserDate.text = "Registrado: ${format.format(date)}"
            } ?: run {
                txtUserDate.text = "Fecha no disponible"
            }

            btnEdit.setOnClickListener {
                onEditClick(user)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(user)
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserResponse>() {
        override fun areItemsTheSame(oldItem: UserResponse, newItem: UserResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserResponse, newItem: UserResponse): Boolean {
            return oldItem == newItem
        }
    }
}