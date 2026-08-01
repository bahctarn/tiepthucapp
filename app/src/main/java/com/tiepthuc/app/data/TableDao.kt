package com.tiepthuc.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {
    @Query("SELECT * FROM tables ORDER BY name ASC")
    fun observeAll(): Flow<List<TableEntity>>

    @Query("SELECT * FROM tables WHERE id = :id")
    suspend fun getById(id: Long): TableEntity?

    @Insert
    suspend fun insert(table: TableEntity): Long

    @Update
    suspend fun update(table: TableEntity)

    @Delete
    suspend fun delete(table: TableEntity)

    @Query("SELECT COUNT(*) FROM order_items WHERE tableId = :tableId AND status = 'PENDING'")
    suspend fun countPendingItemsForTable(tableId: Long): Int

    @Query("SELECT * FROM tables")
    suspend fun getAllOnce(): List<TableEntity>
}
