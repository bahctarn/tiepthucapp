package com.tiepthuc.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiepthuc.app.viewmodel.HistoryRow
import com.tiepthuc.app.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

private enum class DateFilter { ALL, TODAY, YESTERDAY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val rows by viewModel.historyRows.collectAsState()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(DateFilter.ALL) }

    val filtered = remember(rows, query, filter) {
        val cal = Calendar.getInstance()
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayDay = cal.get(Calendar.DAY_OF_YEAR)
        val yesterdayYear = cal.get(Calendar.YEAR)

        rows.filter { row ->
            val matchesQuery = query.isBlank() ||
                row.tableName.contains(query, ignoreCase = true) ||
                row.item.itemName.contains(query, ignoreCase = true)

            val matchesDate = when (filter) {
                DateFilter.ALL -> true
                DateFilter.TODAY -> {
                    val c = Calendar.getInstance().apply { timeInMillis = row.item.servedAt ?: 0L }
                    c.get(Calendar.DAY_OF_YEAR) == todayDay && c.get(Calendar.YEAR) == todayYear
                }
                DateFilter.YESTERDAY -> {
                    val c = Calendar.getInstance().apply { timeInMillis = row.item.servedAt ?: 0L }
                    c.get(Calendar.DAY_OF_YEAR) == yesterdayDay && c.get(Calendar.YEAR) == yesterdayYear
                }
            }
            matchesQuery && matchesDate
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Lịch sử", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                label = { Text("Tìm theo tên bàn hoặc món") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            Row(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = filter == DateFilter.ALL, onClick = { filter = DateFilter.ALL }, label = { Text("Tất cả") })
                FilterChip(selected = filter == DateFilter.TODAY, onClick = { filter = DateFilter.TODAY }, label = { Text("Hôm nay") })
                FilterChip(selected = filter == DateFilter.YESTERDAY, onClick = { filter = DateFilter.YESTERDAY }, label = { Text("Hôm qua") })
            }

            Spacer(Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có dữ liệu lịch sử.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.item.id }) { row ->
                        HistoryItemRow(row)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemRow(row: HistoryRow) {
    val timeFormat = remember { SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()) }
    Card {
        Column(Modifier.padding(14.dp)) {
            Text(
                timeFormat.format(Date(row.item.servedAt ?: row.item.createdAt)),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Text("Bàn ${row.tableName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text("${row.item.itemName} × ${row.item.quantity}", style = MaterialTheme.typography.bodyLarge)
            Text("Đã mang ra", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
