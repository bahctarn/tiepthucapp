package com.tiepthuc.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiepthuc.app.data.ItemStatus
import com.tiepthuc.app.data.OrderItemEntity
import com.tiepthuc.app.ui.theme.AmberPending
import com.tiepthuc.app.viewmodel.PendingRow
import com.tiepthuc.app.viewmodel.PendingViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingScreen(viewModel: PendingViewModel) {
    val rows by viewModel.pendingRows.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val tablesWithPending by viewModel.tablesWithPendingCount.collectAsState()
    val history by viewModel.servedTodayCount.collectAsState()

    val todayServedCount = remember(history) {
        val cal = Calendar.getInstance()
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)
        history.count {
            val c = Calendar.getInstance().apply { timeInMillis = it.servedAt ?: 0L }
            c.get(Calendar.DAY_OF_YEAR) == todayDay && c.get(Calendar.YEAR) == todayYear
        }
    }

    var undoTarget by remember { mutableStateOf<OrderItemEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Món đang chờ", fontWeight = FontWeight.Bold) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            DashboardRow(
                pending = pendingCount,
                tablesPending = tablesWithPending,
                servedToday = todayServedCount
            )

            if (rows.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Không có món nào đang chờ", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rows, key = { it.item.id }) { row ->
                        PendingItemCard(
                            row = row,
                            onServed = {
                                viewModel.markServed(row.item)
                                undoTarget = row.item
                            }
                        )
                    }
                }
            }
        }
    }

    undoTarget?.let { item ->
        LaunchedEffect(item.id) {
            val result = snackbarHostState.showSnackbar(
                message = "Đã xác nhận món đã mang ra.",
                actionLabel = "HOÀN TÁC",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoServed(item)
            }
            undoTarget = null
        }
    }
}

@Composable
private fun DashboardRow(pending: Int, tablesPending: Int, servedToday: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DashboardCard("MÓN ĐANG CHỜ", pending.toString(), Modifier.weight(1f))
        DashboardCard("BÀN CÓ MÓN CHỜ", tablesPending.toString(), Modifier.weight(1f))
        DashboardCard("ĐÃ MANG RA HÔM NAY", servedToday.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun DashboardCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun PendingItemCard(row: PendingRow, onServed: () -> Unit) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Card {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.RestaurantMenu, contentDescription = null, tint = AmberPending)
                Spacer(Modifier.width(8.dp))
                Text(
                    "BÀN ${row.tableName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(timeFormat.format(Date(row.item.createdAt)), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
            Text("${row.item.itemName} × ${row.item.quantity}", style = MaterialTheme.typography.bodyLarge)
            if (!row.item.note.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Ghi chú: ${row.item.note}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onServed,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("ĐÃ MANG RA", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
