package com.example.gymapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {

    @Query("SELECT * FROM members ORDER BY name COLLATE NOCASE ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members ORDER BY feeDueDate ASC")
    suspend fun getAllMembersOnce(): List<Member>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Int): Member?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteMemberById(id: Int)
}
