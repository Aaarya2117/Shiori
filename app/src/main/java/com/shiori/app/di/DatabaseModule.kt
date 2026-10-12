package com.shiori.app.di

import android.content.Context
import androidx.room.Room
import com.shiori.app.data.local.ShioriDatabase
import com.shiori.app.data.local.dao.GameSessionDao
import com.shiori.app.data.local.dao.MedicationScheduleDao
import com.shiori.app.data.local.dao.PatientProfileDao
import com.shiori.app.data.local.dao.ReminderRecordDao
import com.shiori.app.security.KeystoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideShioriDatabase(
        @ApplicationContext context: Context,
        keystoreManager: KeystoreManager
    ): ShioriDatabase {
        val passphrase = keystoreManager.getOrCreateDatabasePassphrase()
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context,
            ShioriDatabase::class.java,
            "shiori_encrypted.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMedicationScheduleDao(db: ShioriDatabase): MedicationScheduleDao = db.medicationScheduleDao()

    @Provides
    fun provideReminderRecordDao(db: ShioriDatabase): ReminderRecordDao = db.reminderRecordDao()

    @Provides
    fun providePatientProfileDao(db: ShioriDatabase): PatientProfileDao = db.patientProfileDao()

    @Provides
    fun provideGameSessionDao(db: ShioriDatabase): GameSessionDao = db.gameSessionDao()
}
