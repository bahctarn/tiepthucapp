package com.tiepthuc.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiepthuc.app.data.MenuItemEntity
import com.tiepthuc.app.repository.AppRepository
import com.tiepthuc.app.viewmodel.MenuViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(viewModel: MenuViewModel) {
    val menuItems by viewModel.menuItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<MenuItemEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<MenuItemEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showError(result: AppRepository.AddMenuItemResult) {
        val message = when (result) {
            is AppRepository.AddMenuItemResult.DuplicateName -> "Món này đã có trong menu rồi."
            is AppRepository.AddMenuItemResult.BlankName -> "Tên món không được để trống."
            else -> null
        }
        if (message != null) {
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Menu", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Thêm món") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (menuItems.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Menu đang trống.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Bấm \"Thêm món\" để bắt đầu tạo danh sách món cho quán.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(menuItems, key = { it.id }) { menuItem ->
                    MenuItemRow(
                        item = menuItem,
                        onEdit = { editTarget = menuItem },
                        onDelete = { deleteTarget = menuItem }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        MenuNameDialog(
            title = "Thêm món vào menu",
            initialValue = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addMenuItem(name) { result ->
                    if (result is AppRepository.AddMenuItemResult.Success) {
                        showAddDialog = false
                    } else {
                        showError(result)
                    }
                }
            }
        )
    }

    editTarget?.let { item ->
        MenuNameDialog(
            title = "Sửa tên món",
            initialValue = item.name,
            onDismiss = { editTarget = null },
            onConfirm = { newName ->
                viewModel.renameMenuItem(item, newName) { result ->
                    if (result is AppRepository.AddMenuItemResult.Success) {
                        editTarget = null
                    } else {
                        showError(result)
                    }
                }
            }
        )
    }

    deleteTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Xoá món khỏi menu") },
            text = { Text("Xoá \"${item.name}\" khỏi menu? Các món đã thêm cho bàn trước đây sẽ không bị ảnh hưởng.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMenuItem(item)
                    deleteTarget = null
                }) { Text("Xoá", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Huỷ") }
            }
        )
    }
}

@Composable
private fun MenuItemRow(
    item: MenuItemEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Sửa") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Xoá") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuNameDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Tên món (VD: Bò lúc lắc, Cơm chiên, Trà đá)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text) }) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huỷ") }
        }
    )
}
