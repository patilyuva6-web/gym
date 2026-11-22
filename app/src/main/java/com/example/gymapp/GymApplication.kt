package com.example.gymapp

import android.app.Application
import com.example.gymapp.data.GymDatabase
import com.example.gymapp.data.MemberRepository
import com.example.gymapp.worker.FeeReminderWorker
import com.example.gymapp.worker.NotificationHelper

class GymApplication : Application() {

    val database: GymDatabase by lazy { GymDatabase.getInstance(this) }
    val repository: MemberRepository by lazy {
        MemberRepository(database.memberDao(), database.feePaymentDao())
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        FeeReminderWorker.scheduleDailyCheck(this)
    }
}
