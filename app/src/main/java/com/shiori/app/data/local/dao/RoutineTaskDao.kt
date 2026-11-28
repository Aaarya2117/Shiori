package com.shiori.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shiori.app.data.local.entity.RoutineTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineTaskDao {
    @Query("SELECT * FROM routine_tasks ORDER BY sortOrder ASC, id ASC")
    fun observeAllTasks(): Flow<List<RoutineTaskEntity>>

    @Query("SELECT * FROM routine_tasks ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllTasks(): List<RoutineTaskEntity>

    @Query("SELECT COUNT(*) FROM routine_tasks")
    suspend fun countTasks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: RoutineTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<RoutineTaskEntity>)

    @Update
    suspend fun updateTask(task: RoutineTaskEntity)

    @Query("DELETE FROM routine_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)
}
