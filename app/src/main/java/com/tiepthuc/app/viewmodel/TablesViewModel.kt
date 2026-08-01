package com.tiepthuc.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiepthuc.app.data.ItemStatus
import com.tiepthuc.app.data.OrderItemEntity
import com.tiepthuc.app.data.TableEntity
import com.tiepthuc.app.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TableStatus { EMPTY, HAS_PENDING, ALL_SERVED }

data class TableRow(
    val table: TableEntity,
    val status: TableStatus,
    val itemCount: Int
)

class TablesViewModel(private val repository: AppRepository) : ViewModel() {

    val tableRows = combine(
        repository.observeTables(),
        repository.observeAllItems()
    ) { tables, items ->
        tables.map { table ->
            val tableItems = items.filter { it.tableId == table.id }
            val status = when {
                tableItems.isEmpty() -> TableStatus.EMPTY
                tableItems.any { it.status == ItemStatus.PENDING } -> TableStatus.HAS_PENDING
                else -> TableStatus.ALL_SERVED
            }
            TableRow(table, status, tableItems.size)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTable(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addTable(name.trim()) }
    }

    fun renameTable(table: TableEntity, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { repository.renameTable(table, newName.trim()) }
    }

    suspend fun hasPendingItems(tableId: Long): Boolean {
        return repository.pendingCountForTable(tableId) > 0
    }

    fun deleteTable(table: TableEntity) {
        viewModelScope.launch { repository.deleteTable(table) }
    }
}
