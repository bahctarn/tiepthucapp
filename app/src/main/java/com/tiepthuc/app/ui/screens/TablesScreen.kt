package com.tiepthuc.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiepthuc.app.viewmodel.TableRow
import com.tiepthuc.app.viewmodel.TableStatus
import com.tiepthuc.app.viewmodel.TablesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(viewModel: TablesViewModel, onOpenTable: (Long, String) -> Unit) {
    val rows by viewModel.tableRows.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<TableRow?>(null) }
    var deleteTarget by remember { mutableStateOf<TableRow?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sơ đồ bàn", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Thêm bàn") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (rows.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Chưa có bàn nào. Bấm \"Thêm bàn\" để bắt đầu.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rows, key = { it.table.id }) { row ->
                    TableCard(
                        row = row,
                        onClick = { onOpenTable(row.table.id, row.table.name) },
                        onEdit = { editTarget = row },
                        onDelete = {
                            scope.launch {
                                if (viewModel.hasPendingItems(row.table.id)) {
                                    deleteTarget = row
                                } else {
                                    viewModel.deleteTable(row.table)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        TableNameDialog(
            title = "Thêm bàn",
            initialValue = "",
            onDismiss = { showAddDialog = false },
            onConfirm = {
                viewModel.addTable(it)
                showAddDialog = false
            }
        )
    }

    editTarget?.let { row ->
        TableNameDialog(
            title = "Sửa tên bàn",
            initialValue = row.table.name,
            onDismiss = { editTarget = null },
            onConfirm = {
                viewModel.renameTable(row.table, it)
                editTarget = null
            }
        )
    }

    deleteTarget?.let { row ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Bàn còn món chưa phục vụ") },
            text = { Text("Bàn \"${row.table.name}\" vẫn còn món chưa được mang ra. Bạn có chắc muốn xoá bàn này? Toàn bộ món cũng sẽ bị xoá.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTable(row.table)
                    deleteTarget = null
                }) { Text("Xoá bàn", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Huỷ") }
            }
        )
    }
}

@Composable
private fun TableCard(
    row: TableRow,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (statusColor, statusLabel) = when (row.status) {
        TableStatus.EMPTY -> Color(0xFF9E9E9E) to "TRỐNG"
        TableStatus.HAS_PENDING -> Color(0xFFF57C00) to "CÓ MÓN ĐANG CHỜ"
        TableStatus.ALL_SERVED -> Color(0xFF2E7D32) to "ĐÃ MANG RA ĐỦ"
    }

    Card(onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(statusColor, shape = CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(row.table.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(statusLabel, style = MaterialTheme.typography.bodyMedium, color = statusColor)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Sửa tên bàn")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Xoá bàn")
            }
        }
    }
}

@Composable
private fun TableNameDialog(
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
                label = { Text("Tên bàn (VD: Bàn 05, Bàn VIP 01)") },
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
