package com.tiepthuc.app.repository

import com.tiepthuc.app.data.AppDatabase
import com.tiepthuc.app.data.ItemStatus
import com.tiepthuc.app.data.MenuItemEntity
import com.tiepthuc.app.data.OrderItemEntity
import com.tiepthuc.app.data.TableEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository là lớp trung gian duy nhất giữa ViewModel và Room Database.
 * Toàn bộ thao tác đều là local, không có bất kỳ lời gọi mạng nào.
 */
class AppRepository(private val db: AppDatabase) {

    private val tableDao = db.tableDao()
    private val itemDao = db.orderItemDao()
    private val menuItemDao = db.menuItemDao()

    // ---- Tables ----
    fun observeTables(): Flow<List<TableEntity>> = tableDao.observeAll()

    suspend fun addTable(name: String): Long = tableDao.insert(TableEntity(name = name))

    suspend fun renameTable(table: TableEntity, newName: String) {
        tableDao.update(table.copy(name = newName))
    }

    suspend fun deleteTable(table: TableEntity) {
        tableDao.delete(table)
    }

    suspend fun pendingCountForTable(tableId: Long): Int =
        tableDao.countPendingItemsForTable(tableId)

    // ---- Order items ----
    fun observeAllItems(): Flow<List<OrderItemEntity>> = itemDao.observeAll()

    fun observePendingItems(): Flow<List<OrderItemEntity>> = itemDao.observePending()

    fun observeItemsForTable(tableId: Long): Flow<List<OrderItemEntity>> =
        itemDao.observeForTable(tableId)

    fun observeHistory(): Flow<List<OrderItemEntity>> = itemDao.observeHistory()

    fun observePendingCount(): Flow<Int> = itemDao.countPending()

    fun observeTablesWithPendingCount(): Flow<Int> = itemDao.countTablesWithPending()

    suspend fun addItem(tableId: Long, name: String, quantity: Int, note: String?): Long {
        return itemDao.insert(
            OrderItemEntity(
                tableId = tableId,
                itemName = name,
                quantity = quantity,
                note = note?.ifBlank { null },
                status = ItemStatus.PENDING
            )
        )
    }

    suspend fun updateItem(item: OrderItemEntity) {
        itemDao.update(item)
    }

    suspend fun deleteItem(item: OrderItemEntity) {
        itemDao.delete(item)
    }

    suspend fun markServed(item: OrderItemEntity) {
        itemDao.update(item.copy(status = ItemStatus.SERVED, servedAt = System.currentTimeMillis()))
    }

    suspend fun undoServed(item: OrderItemEntity) {
        itemDao.update(item.copy(status = ItemStatus.PENDING, servedAt = null))
    }

    suspend fun getItemById(id: Long): OrderItemEntity? = itemDao.getById(id)

    // ---- Gợi ý nhanh (menu items tự học) ----
    fun observeSuggestions(): Flow<List<MenuItemEntity>> = menuItemDao.observeSuggestions()

    /**
     * Gọi mỗi khi người dùng thêm một món (dù gõ tự do hay chọn từ gợi ý).
     * Nếu tên món đã tồn tại trong danh sách gợi ý -> tăng usageCount để nó nổi lên trên.
     * Nếu chưa có -> tự động lưu làm gợi ý mới cho lần sau.
     */
    suspend fun recordItemUsed(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val existing = menuItemDao.findByName(trimmed)
        if (existing != null) {
            menuItemDao.update(
                existing.copy(
                    usageCount = existing.usageCount + 1,
                    lastUsedAt = System.currentTimeMillis()
                )
            )
        } else {
            menuItemDao.insert(MenuItemEntity(name = trimmed))
        }
    }

    suspend fun deleteSuggestion(item: MenuItemEntity) {
        menuItemDao.delete(item)
    }

    // ---- Backup / Restore ----
    suspend fun exportAllTables(): List<TableEntity> = tableDao.getAllOnce()

    suspend fun exportAllItems(): List<OrderItemEntity> = itemDao.getAllOnce()

    suspend fun exportAllMenuItems(): List<MenuItemEntity> = menuItemDao.getAllOnce()

    suspend fun restoreAll(
        tables: List<TableEntity>,
        items: List<OrderItemEntity>,
        menuItems: List<MenuItemEntity> = emptyList()
    ) {
        db.clearAllTables()
        for (t in tables) {
            db.tableDao().insert(t.copy(id = 0)).let { newId ->
                // Re-insert items referencing the old table id, remapped to newId
                items.filter { it.tableId == t.id }.forEach { item ->
                    db.orderItemDao().insert(item.copy(id = 0, tableId = newId))
                }
            }
        }
        for (m in menuItems) {
            db.menuItemDao().insert(m.copy(id = 0))
        }
    }

    suspend fun deleteAllData() {
        db.clearAllTables()
    }
}
