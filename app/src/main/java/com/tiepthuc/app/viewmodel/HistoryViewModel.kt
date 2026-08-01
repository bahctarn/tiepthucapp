package com.tiepthuc.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiepthuc.app.data.OrderItemEntity
import com.tiepthuc.app.data.TableEntity
import com.tiepthuc.app.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HistoryRow(val item: OrderItemEntity, val tableName: String)

class HistoryViewModel(private val repository: AppRepository) : ViewModel() {

    val historyRows = combine(
        repository.observeHistory(),
        repository.observeTables()
    ) { items, tables ->
        val tableMap = tables.associateBy { it.id }
        items.map { item -> HistoryRow(item, tableMap[item.tableId]?.name ?: "?") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
