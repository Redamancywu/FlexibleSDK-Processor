package com.example.spring.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * User data model
 */
data class User(
    @JsonProperty("id")
    val id: String,
    
    @JsonProperty("name")
    val name: String,
    
    @JsonProperty("email")
    val email: String,
    
    @JsonProperty("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @JsonProperty("active")
    val active: Boolean = true
)

/**
 * User creation request
 */
data class CreateUserRequest(
    @JsonProperty("name")
    val name: String,
    
    @JsonProperty("email")
    val email: String
)

/**
 * User update request
 */
data class UpdateUserRequest(
    @JsonProperty("name")
    val name: String? = null,
    
    @JsonProperty("email")
    val email: String? = null,
    
    @JsonProperty("active")
    val active: Boolean? = null
)