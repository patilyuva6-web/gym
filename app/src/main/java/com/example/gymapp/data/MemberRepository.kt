package com.example.gymapp.data

import com.example.gymapp.util.DateUtils
import kotlinx.coroutines.flow.Flow

class MemberRepository(
    private val memberDao: MemberDao,
    private val feePaymentDao: FeePaymentDao
) {

    val allMembers: Flow<List<Member>> = memberDao.getAllMembers()

    fun getPaymentsSince(sinceMillis: Long): Flow<List<FeePayment>> =
        feePaymentDao.getPaymentsSince(sinceMillis)

    suspend fun getMemberById(id: Int): Member? = memberDao.getMemberById(id)

    suspend fun getAllMembersOnce(): List<Member> = memberDao.getAllMembersOnce()

    suspend fun insertMember(
        member: Member,
        recordInitialPayment: Boolean = false
    ): Long {
        val insertedId = memberDao.insertMember(member)
        if (recordInitialPayment && member.feeAmount > 0.0) {
            feePaymentDao.insertPayment(
                FeePayment(
                    memberId = insertedId.toInt(),
                    memberName = member.name,
                    amount = member.feeAmount,
                    paidAt = member.joinDate
                )
            )
        }
        return insertedId
    }

    suspend fun updateMember(member: Member) = memberDao.updateMember(member)

    suspend fun deleteMember(member: Member) = memberDao.deleteMember(member)

    suspend fun markFeePaid(member: Member): Member {
        val updatedDueDate = DateUtils.addOneMonth(member.feeDueDate)
        val updated = member.copy(feeDueDate = updatedDueDate)
        memberDao.updateMember(updated)
        if (member.feeAmount > 0.0) {
            feePaymentDao.insertPayment(
                FeePayment(
                    memberId = member.id,
                    memberName = member.name,
                    amount = member.feeAmount,
                    paidAt = System.currentTimeMillis()
                )
            )
        }
        return updated
    }
}
