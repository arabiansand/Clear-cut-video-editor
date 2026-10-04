package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CleanupProject
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM cleanup_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<CleanupProject>>

    @Query("SELECT * FROM cleanup_projects WHERE id = :id LIMIT 1")
    fun getProjectById(id: Long): Flow<CleanupProject?>

    @Query("SELECT * FROM cleanup_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectByIdOnce(id: Long): CleanupProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: CleanupProject): Long

    @Update
    suspend fun updateProject(project: CleanupProject)

    @Query("DELETE FROM cleanup_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("DELETE FROM cleanup_projects")
    suspend fun deleteAll()
}
