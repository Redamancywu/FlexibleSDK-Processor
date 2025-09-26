package com.example.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.models.CreateUserData
import com.example.android.models.UpdateUserData
import com.example.android.models.User
import com.example.android.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for user management
 * Demonstrates MVVM pattern with FlexibleSDK services
 */
class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()
    
    private val _userCount = MutableStateFlow(0)
    val userCount: StateFlow<Int> = _userCount.asStateFlow()
    
    init {
        loadUsers()
        loadUserCount()
    }
    
    /**
     * Load all users
     */
    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            userRepository.getAllUsersFlow().collect { users ->
                _users.value = users
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Create a new user
     */
    fun createUser(name: String, email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val userData = CreateUserData(name = name, email = email)
            userRepository.createUser(userData)
                .onSuccess {
                    loadUsers()
                    loadUserCount()
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Get user by ID
     */
    fun getUserById(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            userRepository.getUserById(id)
                .onSuccess { user ->
                    _selectedUser.value = user
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Update user
     */
    fun updateUser(id: String, name: String? = null, email: String? = null, isActive: Boolean? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val updateData = UpdateUserData(name = name, email = email, isActive = isActive)
            userRepository.updateUser(id, updateData)
                .onSuccess {
                    loadUsers()
                    if (_selectedUser.value?.id == id) {
                        getUserById(id)
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Delete user
     */
    fun deleteUser(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            userRepository.deleteUser(id)
                .onSuccess { success ->
                    if (success) {
                        loadUsers()
                        loadUserCount()
                        if (_selectedUser.value?.id == id) {
                            _selectedUser.value = null
                        }
                    } else {
                        _error.value = "Failed to delete user"
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Search users
     */
    fun searchUsers(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            if (query.isBlank()) {
                loadUsers()
            } else {
                userRepository.searchUsers(query)
                    .onSuccess { users ->
                        _users.value = users
                    }
                    .onFailure { exception ->
                        _error.value = exception.message
                    }
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Load user count
     */
    private fun loadUserCount() {
        viewModelScope.launch {
            userRepository.getUserCount()
                .onSuccess { count ->
                    _userCount.value = count
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Clear selected user
     */
    fun clearSelectedUser() {
        _selectedUser.value = null
    }
    
    /**
     * Check if service is available
     */
    fun isServiceAvailable(): Boolean {
        return userRepository.isServiceAvailable()
    }
}

/**
 * Factory for creating UserViewModel
 */
class UserViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}