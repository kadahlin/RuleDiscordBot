package com.kyledahlin.rulebot.config

import android.app.Application
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kyledahlin.models.Response
import com.kyledahlin.models.basicResponseModule
import com.kyledahlin.wellness.models.wellnessSerializerModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import okhttp3.MediaType
import retrofit2.Retrofit
import javax.inject.Singleton

@HiltAndroidApp
class ConfigApplication : Application() {
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

//    @Binds
//    abstract fun bindAnalyticsService(
//    ): ConfigService

    companion object {

        @Provides
        @Singleton
        fun providesConfigService(values: BuildValues): ConfigService = Retrofit.Builder()
            .baseUrl("https://${values.serverAddress}")
            .addConverterFactory(Json {
                ignoreUnknownKeys = true
                serializersModule = basicResponseModule + wellnessSerializerModule
            }.asConverterFactory(MediaType.parse("application/json")!!))
            .client(createOkHttpClient())
            .build()
            .create(ConfigService::class.java)
    }
}