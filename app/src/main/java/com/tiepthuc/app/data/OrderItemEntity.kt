package com.tiepthuc.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object ItemStatus {
    const val PENDING = "PENDING"
    const val SERVED = "SERVED"
}

/**
 * Mỗi món ăn/thức uống được gọi cho một bàn.
 * (Gộp khái niệm "Order" vào trực tiếp OrderItem để đơn giản hoá thao tác thêm món nhanh
 * trong nhà hàng - mỗi dòng gắn với tableId và có mốc thời gian tạo riêng.)
 */
@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = TableEntity::class,
            parentColumns = ["id"],
            childColumns = ["tableId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tableId")]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tableId: Long,
    val itemName: String,
    val quantity: Int,
    val note: String? = null,
    val status: String = ItemStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val servedAt: Long? = null
)
