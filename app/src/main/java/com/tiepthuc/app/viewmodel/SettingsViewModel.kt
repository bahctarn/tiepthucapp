package com.tiepthuc.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiepthuc.app.data.BackupData
import com.tiepthuc.app.repository.AppRepository
import kotlinx.coroutines.launch

sealed class SettingsEvent {
    data class Success(val message: String) : SettingsEvent()
    data class Error(val message: String) : SettingsEvent()
}

class SettingsViewModel(private val repository: AppRepository) : ViewModel() {

    fun buildBackupJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val tables = repository.exportAllTables()
            val items = repository.exportAllItems()
            val menuItems = repository.exportAllMenuItems()
            onReady(BackupData.toJson(tables, items, menuItems))
        }
    }

    fun restoreFromJson(json: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val parsed = BackupData.fromJson(json)
                repository.restoreAll(parsed.tables, parsed.items, parsed.menuItems)
                onDone(true, "Khôi phục dữ liệu thành công.")
            } catch (e: Exception) {
                onDone(false, "File backup không hợp lệ: ${e.message}")
            }
        }
    }

    fun deleteAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAllData()
            onDone()
        }
    }
}
