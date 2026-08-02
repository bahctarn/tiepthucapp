package com.tiepthuc.app.ui.screens

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
    val menuItems by viewModel.menuItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<OrderItemEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<OrderItemEntity?>(null) }
    var showEndTableConfirm by remember { mutableStateOf(false) }

    val pendingCount = items.count { it.status == ItemStatus.PENDING }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tableName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        IconButton(onClick = { showEndTableConfirm = true }) {
                            Icon(Icons.Filled.EventAvailable, contentDescription = "Kết thúc bàn")
                        }
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
            menuItems = menuItems,
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
            menuItems = menuItems,
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

    if (showEndTableConfirm) {
        AlertDialog(
            onDismissRequest = { showEndTableConfirm = false },
            title = { Text("Kết thúc bàn?") },
            text = {
                Text(
                    if (pendingCount > 0) {
                        "Bàn này còn $pendingCount món chưa mang ra. Kết thúc bàn sẽ xoá các món chưa phục vụ này (không lưu vào lịch sử). Các món đã mang ra vẫn được giữ nguyên trong Lịch sử. Bàn sẽ trở về trạng thái Trống."
                    } else {
                        "Toàn bộ món đã mang ra của bàn này vẫn được giữ trong Lịch sử. Bàn sẽ trở về trạng thái Trống, sẵn sàng cho khách mới."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showEndTableConfirm = false
                    viewModel.endTable { onBack() }
                }) { Text("Kết thúc bàn") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTableConfirm = false }) { Text("Huỷ") }
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
    menuItems: List<MenuItemEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var qty by remember { mutableStateOf(initialQty) }
    var note by remember { mutableStateOf(initialNote) }

    val filteredMenu = remember(name, menuItems) {
        if (name.isBlank()) {
            menuItems
        } else {
            menuItems.filter { it.name.contains(name, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món") },
                    placeholder = { Text("Gõ để tìm trong menu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (menuItems.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (name.isBlank()) "Chọn từ menu:" else "Khớp với \"$name\":",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    if (filteredMenu.isEmpty()) {
                        Text(
                            "Không có món nào trong menu khớp với từ khoá này.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredMenu, key = { it.id }) { menuItem ->
                                AssistChip(
                                    onClick = { name = menuItem.name },
                                    label = { Text(menuItem.name) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                } else {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Menu đang trống. Bạn có thể vào tab \"Menu\" để thêm món, hoặc cứ gõ tên món trực tiếp ở đây.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(10.dp))
                }

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
