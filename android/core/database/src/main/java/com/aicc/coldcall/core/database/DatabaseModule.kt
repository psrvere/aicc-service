package com.aicc.coldcall.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
        ).build()
    }

    @Provides
    fun provideContactDao(database: AiccDatabase): ContactDao = database.contactDao()

    @Provides
    fun provideCallLogDao(database: AiccDatabase): CallLogDao = database.callLogDao()
}
