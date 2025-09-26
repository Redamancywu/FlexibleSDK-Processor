package com.example.android.services

import android.content.Context
import androidx.room.*
import com.example.android.models.User
import com.flexiblesdk.processor.annotation.ServiceProvider
import java.util.Date

/**
 * Room database interface
 */
@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

/**
 * User DAO interface
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsers(): List<User>
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

/**
 * Type converters for Room
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

/**
 * Database service interface
 */
interface DatabaseService {
    fun getDatabase(): AppDatabase
    suspend fun clearAllData()
    suspend fun getDatabaseSize(): Long
}

/**
 * Database service implementation
 */
@ServiceProvider(
    interfaces = [DatabaseService::class],
    priority = 100,
    singleton = true
)
class DatabaseServiceImpl(
    private val context: Context
) : DatabaseService {
    
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    override fun getDatabase(): AppDatabase {
        return database
    }
    
    override suspend fun clearAllData() {
        database.clearAllTables()
    }
    
    override suspend fun getDatabaseSize(): Long {
        // Simplified database size calculation
        return database.userDao().getUserCount().toLong()
    }
}