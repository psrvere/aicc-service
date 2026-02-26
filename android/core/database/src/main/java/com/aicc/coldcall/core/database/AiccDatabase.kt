package com.aicc.coldcall.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ContactEntity::class, CallLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AiccDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun callLogDao(): CallLogDao
}
