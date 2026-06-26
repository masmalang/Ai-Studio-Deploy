package com.example.data

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    fun getProjectById(id: Int): Flow<ProjectEntity?> = projectDao.getProjectById(id)

    suspend fun insertProject(project: ProjectEntity): Int = projectDao.insertProject(project).toInt()

    suspend fun updateProject(project: ProjectEntity) = projectDao.updateProject(project)

    suspend fun deleteProjectById(id: Int) = projectDao.deleteProjectById(id)

    fun getFilesForProject(projectId: Int): Flow<List<ProjectFileEntity>> = projectDao.getFilesForProject(projectId)

    suspend fun getFileByPath(projectId: Int, filePath: String): ProjectFileEntity? = projectDao.getFileByPath(projectId, filePath)

    suspend fun insertFile(file: ProjectFileEntity): Int = projectDao.insertFile(file).toInt()

    suspend fun updateFile(file: ProjectFileEntity) = projectDao.updateFile(file)

    suspend fun deleteFileByPath(projectId: Int, filePath: String) = projectDao.deleteFileByPath(projectId, filePath)

    fun getBuildLogsForProject(projectId: Int): Flow<List<BuildLogEntity>> = projectDao.getBuildLogsForProject(projectId)

    suspend fun insertBuildLog(log: BuildLogEntity): Int = projectDao.insertBuildLog(log).toInt()

    suspend fun clearBuildLogs(projectId: Int) = projectDao.clearBuildLogs(projectId)
}
