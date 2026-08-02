package com.tiepthuc.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room Database duy nhất của ứng dụng.
 * File SQLite được lưu trong bộ nhớ riêng của app (/data/data/com.tiepthuc.app/databases/),
 * hoàn toàn local trên thiết bị, không đồng bộ ra ngoài dưới bất kỳ hình thức nào.
 */
@Database(
    entities = [TableEntity::class, OrderItemEntity::class, MenuItemEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tableDao(): TableDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun menuItemDao(): MenuItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Thêm bảng menu_items (danh sách gợi ý nhanh) mà không đụng tới dữ liệu
         * bàn/món hiện có của người dùng.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS menu_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        usageCount INTEGER NOT NULL,
                        lastUsedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_menu_items_name ON menu_items(name)"
                )
            }
        }

        /**
         * Thêm cột archived cho order_items, phục vụ tính năng "Kết thúc bàn".
         * Mặc định archived = 0 cho toàn bộ dữ liệu cũ, không ảnh hưởng gì tới
         * bàn/món/lịch sử người dùng đã có sẵn.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE order_items ADD COLUMN archived INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tiepthuc.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
