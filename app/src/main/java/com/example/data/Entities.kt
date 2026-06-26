package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val prompt: String,
    val packageName: String,
    val targetTheme: String, // e.g. "Dark Cosmic", "Teal Minimalist", "Solarized Light", "Brutalist"
    val status: String = "Draft", // "Draft", "Building", "Build Error", "Deployed"
    val githubRepo: String = "",
    val buildCount: Int = 0,
    val lastBuildSuccess: Boolean = false,
    val lastBuildTime: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "project_files",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class ProjectFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val filePath: String,
    val content: String
)

@Entity(
    tableName = "build_logs",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class BuildLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val logType: String, // "INFO", "SUCCESS", "ERROR"
    val message: String
)
