package com.tiepthuc.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiepthuc.app.data.OrderItemEntity
import com.tiepthuc.app.data.TableEntity
import com.tiepthuc.app.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PendingRow(val item: OrderItemEntity, val tableName: String)

class PendingViewModel(private val repository: AppRepository) : ViewModel() {

    val pendingRows = combine(
        repository.observePendingItems(),
        repository.observeTables()
    ) { items, tables ->
        val tableMap = tables.associateBy { it.id }
        items.map { item ->
            PendingRow(item, tableMap[item.tableId]?.name ?: "?")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount = repository.observePendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val tablesWithPendingCount = repository.observeTablesWithPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val servedTodayCount = repository.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markServed(item: OrderItemEntity) {
        viewModelScope.launch { repository.markServed(item) }
    }

    fun undoServed(item: OrderItemEntity) {
        viewModelScope.launch { repository.undoServed(item) }
    }
}
