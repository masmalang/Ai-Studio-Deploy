package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Int): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)

    // Files
    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY filePath ASC")
    fun getFilesForProject(projectId: Int): Flow<List<ProjectFileEntity>>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND filePath = :filePath LIMIT 1")
    suspend fun getFileByPath(projectId: Int, filePath: String): ProjectFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProjectFileEntity): Long

    @Update
    suspend fun updateFile(file: ProjectFileEntity)

    @Query("DELETE FROM project_files WHERE projectId = :projectId AND filePath = :filePath")
    suspend fun deleteFileByPath(projectId: Int, filePath: String)

    // Build Logs
    @Query("SELECT * FROM build_logs WHERE projectId = :projectId ORDER BY timestamp ASC")
    fun getBuildLogsForProject(projectId: Int): Flow<List<BuildLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildLog(log: BuildLogEntity): Long

    @Query("DELETE FROM build_logs WHERE projectId = :projectId")
    suspend fun clearBuildLogs(projectId: Int)
}
