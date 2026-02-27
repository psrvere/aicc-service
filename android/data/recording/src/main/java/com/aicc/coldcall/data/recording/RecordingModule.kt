package com.aicc.coldcall.data.recording

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecordingModule {

    @Provides
    @Singleton
    fun provideRecordingFileManager(
        @ApplicationContext context: Context,
    ): RecordingFileManager = RecordingFileManager(context.filesDir)

    @Provides
    @SystemClock
    fun provideSystemClock(): () -> Long = { System.currentTimeMillis() }
}
