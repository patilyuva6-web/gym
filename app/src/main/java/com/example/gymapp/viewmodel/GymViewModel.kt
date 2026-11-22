package com.example.gymapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.Member
import com.example.gymapp.data.MemberRepository
import com.example.gymapp.util.DateUtils
import com.example.gymapp.util.PhotoStorageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class MemberDueItem(
    val member: Member,
    val daysDifference: Long // 0..3 = due soon, < 0 = expired
)

sealed interface MembersUiState {
    data object Loading : MembersUiState
    data class Success(
        val allCount: Int,
        val filteredMembers: List<Member>,
        val dueOrExpiredCount: Int
    ) : MembersUiState
}

data class DueExpiredUiState(
    val dueSoon: List<MemberDueItem> = emptyList(),
    val expired: List<MemberDueItem> = emptyList()
) {
    val totalCount: Int get() = dueSoon.size + expired.size
}

data class MonthlyRevenue(
    val yearMonth: YearMonth,
    val monthLabel: String,
    val totalAmount: Double
)

data class RevenueUiState(
    val months: List<MonthlyRevenue> = emptyList(),
    val sixMonthTotal: Double = 0.0,
    val currentMonthTotal: Double = 0.0,
    val previousMonthTotal: Double = 0.0
)

class GymViewModel(private val repository: MemberRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val membersUiState: StateFlow<MembersUiState> = combine(
        repository.allMembers,
        _searchQuery
    ) { members, query ->
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            members
        } else {
            members.filter { it.name.contains(trimmed, ignoreCase = true) }
        }
        val dueOrExpiredCount = members.count { DateUtils.daysUntilDue(it.feeDueDate) <= 3L }
        MembersUiState.Success(
            allCount = members.size,
            filteredMembers = filtered,
            dueOrExpiredCount = dueOrExpiredCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MembersUiState.Loading
    )

    val dueExpiredUiState: StateFlow<DueExpiredUiState> = repository.allMembers
        .map { members ->
            val dueSoon = mutableListOf<MemberDueItem>()
            val expired = mutableListOf<MemberDueItem>()

            members.forEach { member ->
                val diff = DateUtils.daysUntilDue(member.feeDueDate)
                when {
                    diff < 0L -> expired.add(MemberDueItem(member, diff))
                    diff in 0L..3L -> dueSoon.add(MemberDueItem(member, diff))
                }
            }

            DueExpiredUiState(
                dueSoon = dueSoon.sortedBy { it.daysDifference },
                expired = expired.sortedBy { it.daysDifference }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DueExpiredUiState()
        )

    private val sixMonthsStartMillis: Long = YearMonth.now()
        .minusMonths(5)
        .atDay(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val revenueUiState: StateFlow<RevenueUiState> = repository
        .getPaymentsSince(sixMonthsStartMillis)
        .map { payments ->
            val zone = ZoneId.systemDefault()
            val currentYm = YearMonth.now(zone)
            val totalsByMonth = payments.groupBy { payment ->
                YearMonth.from(Instant.ofEpochMilli(payment.paidAt).atZone(zone))
            }.mapValues { (_, list) ->
                list.sumOf { it.amount }
            }

            val monthlyList = (5L downTo 0L).map { offset ->
                val ym = currentYm.minusMonths(offset)
                val label = ym.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                MonthlyRevenue(
                    yearMonth = ym,
                    monthLabel = label,
                    totalAmount = totalsByMonth[ym] ?: 0.0
                )
            }

            val sixMonthTotal = monthlyList.sumOf { it.totalAmount }
            val currentMonthTotal = monthlyList.lastOrNull()?.totalAmount ?: 0.0
            val previousMonthTotal = monthlyList.getOrNull(monthlyList.size - 2)?.totalAmount ?: 0.0

            RevenueUiState(
                months = monthlyList,
                sixMonthTotal = sixMonthTotal,
                currentMonthTotal = currentMonthTotal,
                previousMonthTotal = previousMonthTotal
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RevenueUiState()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    suspend fun getMemberById(id: Int): Member? {
        return repository.getMemberById(id)
    }

    fun saveMember(
        existingId: Int?,
        name: String,
        phone: String,
        photoPath: String?,
        previousPhotoPath: String?,
        joinDate: Long,
        feeAmount: Double,
        feeDueDate: Long,
        recordInitialPayment: Boolean = false,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            if (previousPhotoPath != null && previousPhotoPath != photoPath) {
                PhotoStorageHelper.deletePhotoIfExists(previousPhotoPath)
            }
            val member = Member(
                id = existingId ?: 0,
                name = name.trim(),
                phone = phone.trim(),
                photoPath = photoPath,
                joinDate = joinDate,
                feeAmount = feeAmount,
                feeDueDate = feeDueDate
            )
            if (existingId != null && existingId > 0) {
                repository.updateMember(member)
            } else {
                repository.insertMember(member, recordInitialPayment = recordInitialPayment)
            }
            onSaved()
        }
    }

    fun deleteMember(member: Member, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            PhotoStorageHelper.deletePhotoIfExists(member.photoPath)
            repository.deleteMember(member)
            onDeleted()
        }
    }

    fun markFeePaid(member: Member) {
        viewModelScope.launch {
            repository.markFeePaid(member)
        }
    }

    class Factory(private val repository: MemberRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GymViewModel::class.java)) {
                return GymViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
