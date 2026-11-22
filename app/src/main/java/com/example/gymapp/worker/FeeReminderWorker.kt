package com.example.gymapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.gymapp.data.GymDatabase
import com.example.gymapp.util.DateUtils
import java.util.concurrent.TimeUnit

class FeeReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dao = GymDatabase.getInstance(applicationContext).memberDao()
        val allMembers = dao.getAllMembersOnce()

        val dueOrExpiredCount = allMembers.count { member ->
            DateUtils.daysUntilDue(member.feeDueDate) <= 3L
        }

        if (dueOrExpiredCount > 0) {
            NotificationHelper.showFeeReminderNotification(
                context = applicationContext,
                dueOrExpiredCount = dueOrExpiredCount
            )
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "daily_fee_reminder_work"

        fun scheduleDailyCheck(context: Context) {
            val request = PeriodicWorkRequestBuilder<FeeReminderWorker>(
                1, TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
