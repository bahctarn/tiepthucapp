package com.tiepthuc.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tiepthuc.app.repository.AppRepository

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            PendingViewModel::class.java -> PendingViewModel(repository) as T
            TablesViewModel::class.java -> TablesViewModel(repository) as T
            HistoryViewModel::class.java -> HistoryViewModel(repository) as T
            SettingsViewModel::class.java -> SettingsViewModel(repository) as T
            MenuViewModel::class.java -> MenuViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }

    fun tableDetailFactory(tableId: Long): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TableDetailViewModel(repository, tableId) as T
            }
        }
    }
}
