package com.tiepthuc.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiepthuc.app.BuildConfig
import com.tiepthuc.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var pendingBackupJson by remember { mutableStateOf<String?>(null) }

    val backupFileName = remember {
        val fmt = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        "tiepthuc_backup_${fmt.format(Date())}.json"
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val json = pendingBackupJson
        if (uri != null && json != null) {
            writeTextToUri(context, uri, json)
            scope.launch { snackbarHostState.showSnackbar("Đã lưu file sao lưu.") }
        }
        pendingBackupJson = null
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val json = readTextFromUri(context, uri)
            if (json != null) {
                viewModel.restoreFromJson(json) { success, message ->
                    scope.launch { snackbarHostState.showSnackbar(message) }
                }
            } else {
                scope.launch { snackbarHostState.showSnackbar("Không đọc được file.") }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cài đặt", fontWeight = FontWeight.Bold) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SettingsSection(title = "Thông tin ứng dụng") {
                InfoRow("Tên ứng dụng", "Tiếp Thực")
                InfoRow("Phiên bản", BuildConfig.VERSION_NAME)
            }

            Spacer(Modifier.height(20.dp))

            SettingsSection(title = "Sao lưu & khôi phục") {
                Text(
                    "Toàn bộ dữ liệu được lưu trực tiếp trên máy. Bạn có thể xuất file JSON để lưu dự phòng và khôi phục lại khi cần.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.buildBackupJson { json ->
                            pendingBackupJson = json
                            createDocumentLauncher.launch(backupFileName)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("SAO LƯU DỮ LIỆU") }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("KHÔI PHỤC DỮ LIỆU") }
            }

            Spacer(Modifier.height(20.dp))

            SettingsSection(title = "Vùng nguy hiểm") {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("XOÁ TOÀN BỘ DỮ LIỆU") }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xoá toàn bộ dữ liệu?") },
            text = { Text("Thao tác này sẽ xoá toàn bộ bàn, món và lịch sử. Không thể hoàn tác. Bạn nên sao lưu trước khi xoá.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllData {
                        scope.launch { snackbarHostState.showSnackbar("Đã xoá toàn bộ dữ liệu.") }
                    }
                    showDeleteConfirm = false
                }) { Text("Xoá tất cả", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Huỷ") }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Card {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Medium)
    }
}

private fun writeTextToUri(context: Context, uri: Uri, text: String) {
    context.contentResolver.openOutputStream(uri)?.use { out ->
        out.write(text.toByteArray(Charsets.UTF_8))
    }
}

private fun readTextFromUri(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        }
    } catch (e: Exception) {
        null
    }
}
