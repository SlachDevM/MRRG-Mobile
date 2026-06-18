package com.slachdevm.mrrgmobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.slachdevm.mrrgmobile.data.local.converter.JobConverters
import com.slachdevm.mrrgmobile.data.local.dao.JobDao
import com.slachdevm.mrrgmobile.data.local.entity.JobEntity
import com.slachdevm.mrrgmobile.data.local.dao.UserProfileDao
import com.slachdevm.mrrgmobile.data.local.entity.UserProfileEntity

@Database(
    entities = [
        JobEntity::class,
        UserProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(JobConverters::class)
abstract class MRRGDatabase : RoomDatabase() {

    abstract fun jobDao(): JobDao

    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: MRRGDatabase? = null

        fun getInstance(context: Context): MRRGDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MRRGDatabase::class.java,
                    "mrrg_database"
                ).fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}