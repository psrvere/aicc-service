package com.aicc.coldcall.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `recordings` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `contact_id` TEXT NOT NULL,
                `file_path` TEXT NOT NULL,
                `remote_url` TEXT,
                `is_uploaded` INTEGER NOT NULL DEFAULT 0,
                `created_at` INTEGER NOT NULL
            )"""
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AiccDatabase {
        return Room.databaseBuilder(
            context,
            AiccDatabase::class.java,
            "aicc_database"
        ).addMigrations(MIGRATION_1_2).build()
    }

    @Provides
    fun provideContactDao(database: AiccDatabase): ContactDao = database.contactDao()

    @Provides
    fun provideCallLogDao(database: AiccDatabase): CallLogDao = database.callLogDao()

    @Provides
    fun provideRecordingDao(database: AiccDatabase): RecordingDao = database.recordingDao()
}
