package com.tiepthuc.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tiepthuc.app.data.AppDatabase
import com.tiepthuc.app.repository.AppRepository
import com.tiepthuc.app.ui.TiepThucNavHost
import com.tiepthuc.app.ui.theme.TiepThucTheme

/**
 * Điểm khởi đầu duy nhất của ứng dụng.
 * Không có màn hình đăng nhập, không có splash gọi mạng - mở app là dùng được ngay,
 * kể cả ở chế độ máy bay.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = AppRepository(database)

        setContent {
            TiepThucTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TiepThucNavHost(repository)
                }
            }
        }
    }
}
