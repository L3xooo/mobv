package eu.mcomputing.mobv.zadanie.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import eu.mcomputing.mobv.zadanie.data.db.entities.Location
import eu.mcomputing.mobv.zadanie.data.db.entities.User

@Database(entities = [User::class, Location::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dbDao(): DbDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mobv_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
