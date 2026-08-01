package com.tiepthuc.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiepthuc.app.data.ItemStatus
import com.tiepthuc.app.data.MenuItemEntity
import com.tiepthuc.app.data.OrderItemEntity
import com.tiepthuc.app.ui.theme.GreenServed
import com.tiepthuc.app.ui.theme.AmberPending
import com.tiepthuc.app.viewmodel.TableDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDetailScreen(
    tableName: String,
    viewModel: TableDetailViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.items.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<OrderItemEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<OrderItemEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tableName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Thêm món") }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Chưa có món nào cho bàn này.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ItemCard(
                        item = item,
                        onToggleServed = {
                            if (item.status == ItemStatus.PENDING) viewModel.markServed(item)
                            else viewModel.undoServed(item)
                        },
                        onEdit = { editTarget = item },
                        onDelete = { deleteTarget = item }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ItemDialog(
            title = "Thêm món",
            initialName = "",
            initialQty = 1,
            initialNote = "",
            suggestions = suggestions,
            onDeleteSuggestion = { viewModel.deleteSuggestion(it) },
            onDismiss = { showAddDialog = false },
            onConfirm = { name, qty, note ->
                viewModel.addItem(name, qty, note)
                showAddDialog = false
            }
        )
    }

    editTarget?.let { item ->
        ItemDialog(
            title = "Sửa món",
            initialName = item.itemName,
            initialQty = item.quantity,
            initialNote = item.note ?: "",
            suggestions = emptyList(),
            onDeleteSuggestion = {},
            onDismiss = { editTarget = null },
            onConfirm = { name, qty, note ->
                viewModel.updateItem(item, name, qty, note)
                editTarget = null
            }
        )
    }

    deleteTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Xoá món") },
            text = { Text("Xoá \"${item.itemName}\" khỏi bàn này?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteItem(item)
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
private fun ItemCard(
    item: OrderItemEntity,
    onToggleServed: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isServed = item.status == ItemStatus.SERVED
    Card {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${item.itemName} × ${item.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Sửa") }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Xoá") }
            }
            if (!item.note.isNullOrBlank()) {
                Text("Ghi chú: ${item.note}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isServed) "Đã mang ra" else "Đang chờ",
                    color = if (isServed) GreenServed else AmberPending,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = onToggleServed) {
                    Text(if (isServed) "HOÀN TÁC" else "ĐÃ MANG RA")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDialog(
    title: String,
    initialName: String,
    initialQty: Int,
    initialNote: String,
    suggestions: List<MenuItemEntity>,
    onDeleteSuggestion: (MenuItemEntity) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var qty by remember { mutableStateOf(initialQty) }
    var note by remember { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (suggestions.isNotEmpty()) {
                    Text(
                        "Chọn nhanh (giữ để xoá):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(suggestions, key = { it.id }) { suggestion ->
                            QuickPickChip(
                                text = suggestion.name,
                                onClick = { name = suggestion.name },
                                onLongClick = { onDeleteSuggestion(suggestion) }
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Số lượng:", modifier = Modifier.weight(1f))
                    IconButton(onClick = { if (qty > 1) qty-- }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Giảm")
                    }
                    Text(qty.toString(), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { qty++ }) {
                        Icon(Icons.Filled.Add, contentDescription = "Tăng")
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú (không bắt buộc)") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onConfirm(name, qty, note)
            }) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huỷ") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickPickChip(
    text: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
