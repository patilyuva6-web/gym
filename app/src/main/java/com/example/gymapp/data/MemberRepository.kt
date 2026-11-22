package com.example.gymapp.data

import com.example.gymapp.util.DateUtils
import kotlinx.coroutines.flow.Flow

class MemberRepository(private val memberDao: MemberDao) {

    val allMembers: Flow<List<Member>> = memberDao.getAllMembers()

    suspend fun getMemberById(id: Int): Member? = memberDao.getMemberById(id)

    suspend fun getAllMembersOnce(): List<Member> = memberDao.getAllMembersOnce()

    suspend fun insertMember(member: Member): Long = memberDao.insertMember(member)

    suspend fun updateMember(member: Member) = memberDao.updateMember(member)

    suspend fun deleteMember(member: Member) = memberDao.deleteMember(member)

    suspend fun markFeePaid(member: Member): Member {
        val updatedDueDate = DateUtils.addOneMonth(member.feeDueDate)
        val updated = member.copy(feeDueDate = updatedDueDate)
        memberDao.updateMember(updated)
        return updated
    }
}
