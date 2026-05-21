package com.plantdoctor.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.plantdoctor.data.remote.GeminiAnalyzer
import com.plantdoctor.data.remote.OpenAiAnalyzer
import com.plantdoctor.data.remote.PlantAnalyzer
import com.plantdoctor.data.remote.model.PlantDiagnosis
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

    private const val PREFS_FILE = "plant_doctor_secure_prefs"
    private const val KEY_PROVIDER = "ai_provider"
    private const val PROVIDER_GEMINI = "GEMINI"
    private const val PROVIDER_OPENAI = "OPENAI"

    @Provides
    @Singleton
    fun provideEncryptedPrefs(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun providePlantAnalyzer(
        prefs: SharedPreferences,
        okHttpClient: OkHttpClient
    ): PlantAnalyzer {
        // Dynamic proxy that reads the API key fresh each time,
        // so saving a key in Settings works without restarting the app
        return object : PlantAnalyzer {
            override suspend fun analyze(imageBytes: ByteArray): PlantDiagnosis {
                val provider = prefs.getString(KEY_PROVIDER, PROVIDER_GEMINI) ?: PROVIDER_GEMINI
                val apiKey = prefs.getString("api_key_$provider", null)

                if (apiKey.isNullOrBlank()) {
                    throw IllegalStateException(
                        "No API key configured. Please go to Settings and enter your API key."
                    )
                }

                val analyzer = when (provider) {
                    PROVIDER_OPENAI -> OpenAiAnalyzer(apiKey, okHttpClient)
                    else -> GeminiAnalyzer(apiKey, okHttpClient)
                }
                return analyzer.analyze(imageBytes)
            }
        }
    }
}
