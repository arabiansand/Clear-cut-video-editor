package com.example.data.repository

import com.example.data.db.ProjectDao
import com.example.data.model.CleanupProject
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<CleanupProject>> = projectDao.getAllProjects()

    fun getProject(id: Long): Flow<CleanupProject?> = projectDao.getProjectById(id)

    suspend fun getProjectOnce(id: Long): CleanupProject? = projectDao.getProjectByIdOnce(id)

    suspend fun saveProject(project: CleanupProject): Long {
        return if (project.id == 0L) {
            projectDao.insertProject(project)
        } else {
            projectDao.updateProject(project)
            project.id
        }
    }

    suspend fun deleteProject(id: Long) {
        projectDao.deleteProjectById(id)
    }
}
