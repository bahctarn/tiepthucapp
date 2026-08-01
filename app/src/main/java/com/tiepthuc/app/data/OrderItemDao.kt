package com.tiepthuc.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {

    @Query("SELECT * FROM order_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun observePending(): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE tableId = :tableId ORDER BY createdAt DESC")
    fun observeForTable(tableId: Long): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE status = 'SERVED' ORDER BY servedAt DESC")
    fun observeHistory(): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE id = :id")
    suspend fun getById(id: Long): OrderItemEntity?

    @Insert
    suspend fun insert(item: OrderItemEntity): Long

    @Update
    suspend fun update(item: OrderItemEntity)

    @Delete
    suspend fun delete(item: OrderItemEntity)

    @Query("SELECT COUNT(*) FROM order_items WHERE status = 'PENDING'")
    fun countPending(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT tableId) FROM order_items WHERE status = 'PENDING'")
    fun countTablesWithPending(): Flow<Int>

    @Query("SELECT * FROM order_items")
    suspend fun getAllOnce(): List<OrderItemEntity>
}
