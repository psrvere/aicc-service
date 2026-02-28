package com.aicc.coldcall

import com.aicc.coldcall.core.network.BaseUrlProvider
import com.aicc.coldcall.core.network.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindTokenProvider(provider: AppConfigProvider): TokenProvider

    @Binds
    @Singleton
    abstract fun bindBaseUrlProvider(provider: AppConfigProvider): BaseUrlProvider
}
