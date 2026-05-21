package com.plantdoctor.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.plantdoctor.data.remote.GeminiAnalyzer
import com.plantdoctor.data.remote.OpenAiAnalyzer
import com.plantdoctor.data.remote.PlantAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyzerModule {

    private const val PREFS_FILE = "encrypted_prefs"
    private const val KEY_PROVIDER = "ai_provider"
    private const val KEY_API_KEY = "api_key"
    private const val PROVIDER_GEMINI = "gemini"
    private const val PROVIDER_OPENAI = "openai"

    @Provides
    @Singleton
    fun providePlantAnalyzer(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        gson: Gson
    ): PlantAnalyzer {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val provider = prefs.getString(KEY_PROVIDER, PROVIDER_GEMINI) ?: PROVIDER_GEMINI
        val apiKey = prefs.getString(KEY_API_KEY, null)

        if (apiKey.isNullOrBlank()) {
            return object : PlantAnalyzer {
                override suspend fun analyze(
                    imageBytes: ByteArray,
                    context: Context
                ): com.plantdoctor.domain.model.DiagnosisResult {
                    throw IllegalStateException(
                        "No API key configured. Please go to Settings and enter your API key to analyze plants."
                    )
                }
            }
        }

        return when (provider) {
            PROVIDER_OPENAI -> OpenAiAnalyzer(okHttpClient, gson, apiKey)
            else -> GeminiAnalyzer(okHttpClient, gson, apiKey)
        }
    }
}
