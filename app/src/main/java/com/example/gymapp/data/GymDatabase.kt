package com.example.gymapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Member::class],
    version = 1,
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getInstance(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
