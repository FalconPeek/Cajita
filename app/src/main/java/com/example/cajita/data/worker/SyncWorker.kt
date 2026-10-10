package com.example.cajita.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.cajita.data.local.db.CajitaDatabase
import com.example.cajita.data.preferences.UserPreferencesRepository
import com.example.cajita.data.remote.SupabaseService

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = CajitaDatabase.getDatabase(applicationContext)
        val dao = database.cajitaDao()
        val prefs = UserPreferencesRepository(applicationContext)
        val supabaseService = SupabaseService()
        val dispositivoId = prefs.getOrCreateDispositivoId()

        val success = Sincronizador.ejecutarSincronizacion(
            context = applicationContext,
            dao = dao,
            supabaseService = supabaseService,
            dispositivoId = dispositivoId
        )

        return if (success) Result.success() else Result.retry()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "CajitaSyncWork",
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest
            )
        }
    }
}
