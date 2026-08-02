package com.tiepthuc.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiepthuc.app.data.MenuItemEntity
import com.tiepthuc.app.data.OrderItemEntity
import com.tiepthuc.app.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TableDetailViewModel(
    private val repository: AppRepository,
    private val tableId: Long
) : ViewModel() {

    val items = repository.observeItemsForTable(tableId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Danh sách món trong Menu (do người dùng tự quản lý ở màn hình "Menu"),
    // dùng để lọc/gợi ý khi thêm món cho bàn. Không tự thêm/học món mới ở đây nữa.
    val menuItems: StateFlow<List<MenuItemEntity>> = repository.observeMenuItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addItem(name: String, quantity: Int, note: String?) {
        if (name.isBlank() || quantity <= 0) return
        viewModelScope.launch {
            repository.addItem(tableId, name.trim(), quantity, note)
        }
    }

    fun markServed(item: OrderItemEntity) {
        viewModelScope.launch { repository.markServed(item) }
    }

    fun undoServed(item: OrderItemEntity) {
        viewModelScope.launch { repository.undoServed(item) }
    }

    fun updateItem(item: OrderItemEntity, name: String, quantity: Int, note: String?) {
        if (name.isBlank() || quantity <= 0) return
        viewModelScope.launch {
            repository.updateItem(item.copy(itemName = name.trim(), quantity = quantity, note = note?.ifBlank { null }))
        }
    }

    fun deleteItem(item: OrderItemEntity) {
        viewModelScope.launch { repository.deleteItem(item) }
    }

    fun endTable(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.endTable(tableId)
            onDone()
        }
    }
}
