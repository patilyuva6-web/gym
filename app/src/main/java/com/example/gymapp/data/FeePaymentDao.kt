package com.example.gymapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeePaymentDao {

    @Query("SELECT * FROM fee_payments WHERE paidAt >= :sinceMillis ORDER BY paidAt ASC")
    fun getPaymentsSince(sinceMillis: Long): Flow<List<FeePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: FeePayment): Long
}
