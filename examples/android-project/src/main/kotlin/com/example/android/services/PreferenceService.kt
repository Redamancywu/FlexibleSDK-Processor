package com.example.android.services

import android.content.Context
import android.content.SharedPreferences
import com.flexiblesdk.processor.annotation.ServiceProvider

/**
 * Preference service interface
 */
interface PreferenceService {
    fun getString(key: String, defaultValue: String = ""): String
    fun setString(key: String, value: String)
    fun getInt(key: String, defaultValue: Int = 0): Int
    fun setInt(key: String, value: Int)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun setBoolean(key: String, value: Boolean)
    fun getLong(key: String, defaultValue: Long = 0L): Long
    fun setLong(key: String, value: Long)
    fun remove(key: String)
    fun clear()
    fun contains(key: String): Boolean
}

/**
 * Preference service implementation using SharedPreferences
 */
@ServiceProvider(
    interfaces = [PreferenceService::class],
    priority = 100,
    singleton = true
)
class PreferenceServiceImpl(
    private val context: Context
) : PreferenceService {
    
    companion object {
        private const val PREF_NAME = "app_preferences"
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    override fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    
    override fun setString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    override fun setInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    override fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    override fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }
    
    override fun setLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    
    override fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
    
    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }
    
    override fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}