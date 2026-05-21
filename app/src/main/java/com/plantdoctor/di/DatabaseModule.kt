package com.plantdoctor.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.plantdoctor.data.local.PlantDoctorDatabase
import com.plantdoctor.data.local.dao.DiagnosisDao
import com.plantdoctor.data.local.dao.PlantDao
import com.plantdoctor.data.local.dao.SeasonalTipDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PlantDoctorDatabase {
        return Room.databaseBuilder(
            context,
            PlantDoctorDatabase::class.java,
            "plant_doctor_db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        prepopulateSeasonalTips(db)
                    }
                }
            })
            .build()
    }

    private fun prepopulateSeasonalTips(db: SupportSQLiteDatabase) {
        val tips = listOf(
            Triple("January", "Winter Pruning", "Prune dormant trees and shrubs while they are leafless. Remove dead, damaged, or crossing branches to promote healthy spring growth."),
            Triple("February", "Seed Starting", "Start seeds indoors for warm-season crops like tomatoes, peppers, and herbs. Use seed-starting mix and provide adequate light and warmth."),
            Triple("March", "Spring Preparation", "Prepare garden beds by turning soil, adding compost, and testing soil pH. Clean up winter debris and plan your spring planting layout."),
            Triple("April", "Planting Season", "Begin transplanting seedlings outdoors after the last frost. Plant cool-season vegetables, spring bulbs, and start hardening off indoor seedlings."),
            Triple("May", "Pest Watch", "Monitor plants closely for early signs of aphids, slugs, and caterpillars. Use companion planting and introduce beneficial insects like ladybugs for natural pest control."),
            Triple("June", "Watering Wisely", "Water deeply and less frequently to encourage deep root growth. Water in the early morning to reduce evaporation and prevent fungal diseases."),
            Triple("July", "Heat Protection", "Protect plants from extreme heat with shade cloth and mulch. Keep soil consistently moist and avoid fertilizing during heat waves to reduce plant stress."),
            Triple("August", "Fertilizing", "Apply a balanced fertilizer to support late-summer growth. Side-dress vegetables with compost and feed flowering plants to encourage continued blooming."),
            Triple("September", "Fall Preparation", "Begin planting fall crops like kale, lettuce, and garlic. Divide perennials, collect seeds from mature plants, and start reducing watering for dormancy."),
            Triple("October", "Harvest Time", "Harvest remaining summer crops before the first frost. Store root vegetables properly, make preserves, and clean up spent annual plants from beds."),
            Triple("November", "Winterizing", "Protect tender plants with mulch or burlap wraps. Drain and store hoses, clean and oil garden tools, and apply winter mulch to perennial beds."),
            Triple("December", "Indoor Plant Care", "Focus on houseplant care during the darkest month. Reduce watering, increase humidity, move plants closer to windows, and avoid cold drafts.")
        )

        tips.forEachIndexed { index, (month, title, tip) ->
            db.execSQL(
                """INSERT INTO seasonal_tips (id, month, monthIndex, title, tip) 
                   VALUES ('${UUID.randomUUID()}', '$month', ${index + 1}, '$title', '$tip')"""
            )
        }
    }

    @Provides
    fun providePlantDao(database: PlantDoctorDatabase): PlantDao {
        return database.plantDao()
    }

    @Provides
    fun provideDiagnosisDao(database: PlantDoctorDatabase): DiagnosisDao {
        return database.diagnosisDao()
    }

    @Provides
    fun provideSeasonalTipDao(database: PlantDoctorDatabase): SeasonalTipDao {
        return database.seasonalTipDao()
    }
}
