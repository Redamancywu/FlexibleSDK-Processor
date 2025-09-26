package com.example.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.databinding.ItemUserBinding
import com.example.android.models.User
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * RecyclerView adapter for displaying users
 */
class UserAdapter(
    private val onUserClick: (User) -> Unit,
    private val onUserDelete: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        
        fun bind(user: User) {
            binding.apply {
                textViewName.text = user.name
                textViewEmail.text = user.email
                textViewCreatedAt.text = "Created: ${dateFormat.format(user.createdAt)}"
                textViewStatus.text = if (user.isActive) "Active" else "Inactive"
                textViewStatus.setTextColor(
                    if (user.isActive) {
                        android.graphics.Color.GREEN
                    } else {
                        android.graphics.Color.RED
                    }
                )
                
                root.setOnClickListener {
                    onUserClick(user)
                }
                
                buttonDelete.setOnClickListener {
                    onUserDelete(user)
                }
            }
        }
    }
    
    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}