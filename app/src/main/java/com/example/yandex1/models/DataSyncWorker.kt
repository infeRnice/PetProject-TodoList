package com.example.yandex1.models

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context

class DataSyncWorker(
    appContext: android.content.Context,
    workerParams: WorkerParameters,
    private val repository: TodoItemsRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            repository.pushLocalChangesToFirestore()
            repository.syncDataWithFirestore()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
