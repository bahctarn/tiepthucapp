package com.tiepthuc.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Danh sách "gợi ý nhanh" khi thêm món.
 * KHÔNG phải menu cố định - đây chỉ là các tên món người dùng đã từng gõ,
 * được tự động lưu lại để lần sau chọn nhanh thay vì gõ lại. Người dùng vẫn
 * luôn có thể gõ tên món hoàn toàn mới bất cứ lúc nào.
 */
@Entity(
    tableName = "menu_items",
    indices = [Index(value = ["name"], unique = true)]
)
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val usageCount: Int = 1,
    val lastUsedAt: Long = System.currentTimeMillis()
)
