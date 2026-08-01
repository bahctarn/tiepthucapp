package com.tiepthuc.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiepthuc.app.data.MenuItemEntity
import com.tiepthuc.app.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MenuViewModel(private val repository: AppRepository) : ViewModel() {

    val menuItems = repository.observeMenuItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMenuItem(name: String, onResult: (AppRepository.AddMenuItemResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.addMenuItem(name)
            onResult(result)
        }
    }

    fun renameMenuItem(item: MenuItemEntity, newName: String, onResult: (AppRepository.AddMenuItemResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.renameMenuItem(item, newName)
            onResult(result)
        }
    }

    fun deleteMenuItem(item: MenuItemEntity) {
        viewModelScope.launch { repository.deleteMenuItem(item) }
    }
}
