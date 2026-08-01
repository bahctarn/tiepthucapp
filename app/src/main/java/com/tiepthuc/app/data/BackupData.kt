package com.tiepthuc.app.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Chuyển toàn bộ dữ liệu Room sang JSON để sao lưu, và ngược lại để khôi phục.
 * Không dùng bất kỳ thư viện mạng hay cloud nào - chỉ đọc/ghi file JSON trên bộ nhớ máy.
 */
object BackupData {

    const val BACKUP_VERSION = 2

    fun toJson(
        tables: List<TableEntity>,
        items: List<OrderItemEntity>,
        menuItems: List<MenuItemEntity> = emptyList()
    ): String {
        val root = JSONObject()
        root.put("backupVersion", BACKUP_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        val tablesArray = JSONArray()
        for (t in tables) {
            val o = JSONObject()
            o.put("id", t.id)
            o.put("name", t.name)
            o.put("createdAt", t.createdAt)
            tablesArray.put(o)
        }
        root.put("tables", tablesArray)

        val itemsArray = JSONArray()
        for (i in items) {
            val o = JSONObject()
            o.put("id", i.id)
            o.put("tableId", i.tableId)
            o.put("itemName", i.itemName)
            o.put("quantity", i.quantity)
            o.put("note", i.note ?: JSONObject.NULL)
            o.put("status", i.status)
            o.put("createdAt", i.createdAt)
            o.put("servedAt", i.servedAt ?: JSONObject.NULL)
            itemsArray.put(o)
        }
        root.put("items", itemsArray)

        val menuArray = JSONArray()
        for (m in menuItems) {
            val o = JSONObject()
            o.put("id", m.id)
            o.put("name", m.name)
            o.put("usageCount", m.usageCount)
            o.put("lastUsedAt", m.lastUsedAt)
            menuArray.put(o)
        }
        root.put("menuItems", menuArray)

        return root.toString(2)
    }

    data class ParsedBackup(
        val tables: List<TableEntity>,
        val items: List<OrderItemEntity>,
        val menuItems: List<MenuItemEntity>
    )

    fun fromJson(json: String): ParsedBackup {
        val root = JSONObject(json)

        val tablesArray = root.optJSONArray("tables") ?: JSONArray()
        val tables = mutableListOf<TableEntity>()
        for (idx in 0 until tablesArray.length()) {
            val o = tablesArray.getJSONObject(idx)
            tables.add(
                TableEntity(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    createdAt = o.getLong("createdAt")
                )
            )
        }

        val itemsArray = root.optJSONArray("items") ?: JSONArray()
        val items = mutableListOf<OrderItemEntity>()
        for (idx in 0 until itemsArray.length()) {
            val o = itemsArray.getJSONObject(idx)
            items.add(
                OrderItemEntity(
                    id = o.getLong("id"),
                    tableId = o.getLong("tableId"),
                    itemName = o.getString("itemName"),
                    quantity = o.getInt("quantity"),
                    note = if (o.isNull("note")) null else o.getString("note"),
                    status = o.getString("status"),
                    createdAt = o.getLong("createdAt"),
                    servedAt = if (o.isNull("servedAt")) null else o.getLong("servedAt")
                )
            )
        }

        // menuItems không tồn tại trong file backup cũ (version 1) - đọc an toàn, mặc định rỗng.
        val menuArray = root.optJSONArray("menuItems") ?: JSONArray()
        val menuItems = mutableListOf<MenuItemEntity>()
        for (idx in 0 until menuArray.length()) {
            val o = menuArray.getJSONObject(idx)
            menuItems.add(
                MenuItemEntity(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    usageCount = o.optInt("usageCount", 1),
                    lastUsedAt = o.optLong("lastUsedAt", System.currentTimeMillis())
                )
            )
        }

        return ParsedBackup(tables, items, menuItems)
    }
}
