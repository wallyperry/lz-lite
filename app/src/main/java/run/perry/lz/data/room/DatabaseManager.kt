package run.perry.lz.data.room

import android.content.Context
import androidx.room.Room

object DatabaseManager {
    private var database: LzDatabase? = null

    fun init(context: Context) {
        database = Room.databaseBuilder(
            context,
            LzDatabase::class.java,
            "lz.db"
        ).build()
    }

    fun getDatabase(): LzDatabase {
        return database ?: throw IllegalStateException("Database not initialized!")
    }
}