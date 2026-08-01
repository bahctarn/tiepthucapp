package com.tiepthuc.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {

    @Query("SELECT * FROM menu_items ORDER BY name ASC")
    fun observeAll(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): MenuItemEntity?

    @Insert
    suspend fun insert(item: MenuItemEntity): Long

    @Update
    suspend fun update(item: MenuItemEntity)

    @Delete
    suspend fun delete(item: MenuItemEntity)

    @Query("SELECT * FROM menu_items")
    suspend fun getAllOnce(): List<MenuItemEntity>
}
