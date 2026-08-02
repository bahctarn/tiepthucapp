package com.tiepthuc.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {

    // Dùng cho tính trạng thái bàn (Sơ đồ bàn) - chỉ tính món thuộc phiên hiện tại.
    @Query("SELECT * FROM order_items WHERE archived = 0 ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE status = 'PENDING' AND archived = 0 ORDER BY createdAt ASC")
    fun observePending(): Flow<List<OrderItemEntity>>

    // Chi tiết bàn - chỉ hiện món của phiên hiện tại (chưa "Kết thúc bàn").
    @Query("SELECT * FROM order_items WHERE tableId = :tableId AND archived = 0 ORDER BY createdAt DESC")
    fun observeForTable(tableId: Long): Flow<List<OrderItemEntity>>

    // Lịch sử: luôn hiển thị đầy đủ mọi món đã từng mang ra, kể cả sau khi bàn đã
    // "Kết thúc" và món bị archived - lịch sử không bao giờ bị xoá.
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

    @Query("SELECT COUNT(*) FROM order_items WHERE status = 'PENDING' AND archived = 0")
    fun countPending(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT tableId) FROM order_items WHERE status = 'PENDING' AND archived = 0")
    fun countTablesWithPending(): Flow<Int>

    @Query("SELECT COUNT(*) FROM order_items WHERE tableId = :tableId AND status = 'PENDING' AND archived = 0")
    suspend fun countPendingForTable(tableId: Long): Int

    // Xuất/khôi phục backup: lấy toàn bộ, kể cả món đã archived, để không mất lịch sử.
    @Query("SELECT * FROM order_items")
    suspend fun getAllOnce(): List<OrderItemEntity>

    // ---- Kết thúc bàn ----
    // Món chưa từng được mang ra thì xoá hẳn (chưa có giá trị lịch sử).
    @Query("DELETE FROM order_items WHERE tableId = :tableId AND status = 'PENDING' AND archived = 0")
    suspend fun deleteUnservedPendingForTable(tableId: Long)

    // Món đã mang ra thì giữ lại nguyên vẹn cho Lịch sử, chỉ đánh dấu archived
    // để nó không còn tính vào bàn hiện tại nữa.
    @Query("UPDATE order_items SET archived = 1 WHERE tableId = :tableId AND status = 'SERVED' AND archived = 0")
    suspend fun archiveServedForTable(tableId: Long)

    @Transaction
    suspend fun endTableSession(tableId: Long) {
        deleteUnservedPendingForTable(tableId)
        archiveServedForTable(tableId)
    }
}
