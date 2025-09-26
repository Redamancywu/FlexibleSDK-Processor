package com.example.android.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.R
import com.example.android.adapters.UserAdapter
import com.example.android.databinding.ActivityMainBinding
import com.example.android.models.User
import com.example.android.repositories.UserRepository
import com.example.android.viewmodels.UserViewModel
import com.example.android.viewmodels.UserViewModelFactory
import kotlinx.coroutines.launch

/**
 * Main activity demonstrating FlexibleSDK integration in Android
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var userAdapter: UserAdapter
    
    private val userRepository = UserRepository()
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(userRepository)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        // Check if services are available
        if (!userViewModel.isServiceAvailable()) {
            Toast.makeText(this, "UserService not available", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            onUserClick = { user ->
                userViewModel.getUserById(user.id)
                showUserDetails(user)
            },
            onUserDelete = { user ->
                userViewModel.deleteUser(user.id)
            }
        )
        
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = userAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonAddUser.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            
            if (name.isNotEmpty() && email.isNotEmpty()) {
                userViewModel.createUser(name, email)
                binding.editTextName.text.clear()
                binding.editTextEmail.text.clear()
            } else {
                Toast.makeText(this, "Please enter name and email", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.buttonSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString().trim()
            userViewModel.searchUsers(query)
        }
        
        binding.buttonClearSearch.setOnClickListener {
            binding.editTextSearch.text.clear()
            userViewModel.loadUsers()
        }
        
        binding.buttonRefresh.setOnClickListener {
            userViewModel.loadUsers()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            userViewModel.users.collect { users ->
                userAdapter.submitList(users)
                binding.textViewUserCount.text = "Total Users: ${users.size}"
            }
        }
        
        lifecycleScope.launch {
            userViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
            }
        }
        
        lifecycleScope.launch {
            userViewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                    userViewModel.clearError()
                }
            }
        }
        
        lifecycleScope.launch {
            userViewModel.selectedUser.collect { user ->
                user?.let {
                    // Update UI with selected user details if needed
                }
            }
        }
        
        lifecycleScope.launch {
            userViewModel.userCount.collect { count ->
                binding.textViewTotalCount.text = "Database Count: $count"
            }
        }
    }
    
    private fun showUserDetails(user: User) {
        val details = """
            ID: ${user.id}
            Name: ${user.name}
            Email: ${user.email}
            Created: ${user.createdAt}
            Active: ${user.isActive}
        """.trimIndent()
        
        Toast.makeText(this, details, Toast.LENGTH_LONG).show()
    }
}