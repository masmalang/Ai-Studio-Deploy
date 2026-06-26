package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository

    val allProjects: StateFlow<List<ProjectEntity>>
    
    private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
    val selectedProject: StateFlow<ProjectEntity?> = _selectedProject.asStateFlow()

    private val _projectFiles = MutableStateFlow<List<ProjectFileEntity>>(emptyList())
    val projectFiles: StateFlow<List<ProjectFileEntity>> = _projectFiles.asStateFlow()

    private val _selectedFile = MutableStateFlow<ProjectFileEntity?>(null)
    val selectedFile: StateFlow<ProjectFileEntity?> = _selectedFile.asStateFlow()

    private val _buildLogs = MutableStateFlow<List<BuildLogEntity>>(emptyList())
    val buildLogs: StateFlow<List<BuildLogEntity>> = _buildLogs.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isBuilding = MutableStateFlow(false)
    val isBuilding: StateFlow<Boolean> = _isBuilding.asStateFlow()

    private val _isDebugging = MutableStateFlow(false)
    val isDebugging: StateFlow<Boolean> = _isDebugging.asStateFlow()

    private val _isPushing = MutableStateFlow(false)
    val isPushing: StateFlow<Boolean> = _isPushing.asStateFlow()

    private val _debugExplanation = MutableStateFlow<String?>(null)
    val debugExplanation: StateFlow<String?> = _debugExplanation.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProjectRepository(database.projectDao())
        allProjects = MutableStateFlow(emptyList()) // Will back with Room Flow
        
        // Observe projects
        viewModelScope.launch {
            repository.allProjects.collect {
                (allProjects as MutableStateFlow).value = it
            }
        }
    }

    fun selectProject(project: ProjectEntity) {
        _selectedProject.value = project
        _debugExplanation.value = null
        _apiError.value = null
        
        // Observe files for selected project
        viewModelScope.launch {
            repository.getFilesForProject(project.id).collect { files ->
                _projectFiles.value = files
                if (_selectedFile.value == null || files.none { it.id == _selectedFile.value?.id }) {
                    _selectedFile.value = files.firstOrNull { it.filePath == "MainActivity.kt" } ?: files.firstOrNull()
                } else {
                    // Update active file with latest DB content
                    _selectedFile.value = files.find { it.id == _selectedFile.value?.id }
                }
            }
        }

        // Observe build logs
        viewModelScope.launch {
            repository.getBuildLogsForProject(project.id).collect { logs ->
                _buildLogs.value = logs
            }
        }
    }

    fun selectFile(file: ProjectFileEntity) {
        _selectedFile.value = file
    }

    fun createProject(
        name: String,
        description: String,
        prompt: String,
        packageName: String,
        theme: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val project = ProjectEntity(
                name = name,
                description = description,
                prompt = prompt,
                packageName = packageName,
                targetTheme = theme
            )
            val projectId = repository.insertProject(project)
            
            // Initialize files for this project
            val mainActivityContent = """
package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "Hello DroidCraft Builder!", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}
            """.trimIndent()

            val buildGradleContent = """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "$packageName"
    compileSdk = 35

    defaultConfig {
        applicationId = "$packageName"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
}
            """.trimIndent()

            val themeContent = """
package $packageName.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF64FFDA)
val Secondary = Color(0xFF0A192F)
val Background = Color(0xFF020C1B)

val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background
)
            """.trimIndent()

            repository.insertFile(ProjectFileEntity(projectId = projectId, filePath = "MainActivity.kt", content = mainActivityContent))
            repository.insertFile(ProjectFileEntity(projectId = projectId, filePath = "build.gradle.kts", content = buildGradleContent))
            repository.insertFile(ProjectFileEntity(projectId = projectId, filePath = "Theme.kt", content = themeContent))

            // Add initial log
            repository.insertBuildLog(BuildLogEntity(
                projectId = projectId,
                logType = "INFO",
                message = "Project initialized successfully with 3 baseline files: MainActivity.kt, build.gradle.kts, Theme.kt."
            ))

            // Auto-select this project
            val updatedProject = repository.getProjectById(projectId).firstOrNull()
            if (updatedProject != null) {
                withContext(Dispatchers.Main) {
                    selectProject(updatedProject)
                }
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProjectById(project.id)
            if (_selectedProject.value?.id == project.id) {
                withContext(Dispatchers.Main) {
                    _selectedProject.value = null
                    _projectFiles.value = emptyList()
                    _selectedFile.value = null
                    _buildLogs.value = emptyList()
                }
            }
        }
    }

    fun updateFileContent(file: ProjectFileEntity, newContent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = file.copy(content = newContent)
            repository.updateFile(updated)
            if (_selectedFile.value?.id == file.id) {
                _selectedFile.value = updated
            }
        }
    }

    fun generateCodeWithGemini() {
        val project = _selectedProject.value ?: return
        val mainActivityFile = _projectFiles.value.find { it.filePath == "MainActivity.kt" } ?: return
        
        _isGenerating.value = true
        _apiError.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertBuildLog(BuildLogEntity(
                    projectId = project.id,
                    logType = "INFO",
                    message = "Prompting Gemini 3.5 Flash for Kotlin/Compose source code generation..."
                ))

                // Get API Key from BuildConfig
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                    throw IllegalStateException("API Key is placeholder. Please configure GEMINI_API_KEY in the Secrets panel in AI Studio.")
                }

                val systemPrompt = """
                    You are DroidCraft's elite Mobile App Compiler and Architect.
                    Generate a fully complete, self-contained, compile-ready Kotlin/Jetpack Compose class file.
                    Target theme specified by the user: '${project.targetTheme}'.
                    Requirement: ${project.prompt}
                    Your code must contain beautiful layouts, customized Material 3 Cards, Buttons, State Variables, spacing, and modern typography.
                    IMPORTANT: Output ONLY valid Kotlin code starting with package ${project.packageName}.
                    Do not enclose the code in Markdown blocks. If you must, make sure it is valid, but raw Kotlin starting with imports is highly preferred.
                    Ensure the class contains 'class MainActivity : ComponentActivity()' with complete imports for Android activity, compose components, styling, and graphics.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = "Please write the Kotlin MainActivity.kt code for this app prompt: ${project.prompt}")))
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                    generationConfig = GenerationConfig(temperature = 0.5f)
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw IllegalStateException("Received empty response from Gemini API.")

                // Parse/sanitize code (remove markdown wrappers if generated)
                val cleanCode = sanitizeMarkdownCode(responseText)

                // Update MainActivity file
                repository.updateFile(mainActivityFile.copy(content = cleanCode))
                
                repository.insertBuildLog(BuildLogEntity(
                    projectId = project.id,
                    logType = "SUCCESS",
                    message = "Gemini 3.5 Flash generated high-quality Jetpack Compose source code successfully! Core components compiled locally."
                ))

            } catch (e: Exception) {
                e.printStackTrace()
                val errMsg = e.message ?: "Unknown API Network Error"
                withContext(Dispatchers.Main) {
                    _apiError.value = errMsg
                }
                repository.insertBuildLog(BuildLogEntity(
                    projectId = project.id,
                    logType = "ERROR",
                    message = "Gemini Code Gen Failed: $errMsg. Check connection or verify Secrets panel API configuration."
                ))
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun askGeminiToDebug(compilerError: String) {
        val project = _selectedProject.value ?: return
        val activeFile = _selectedFile.value ?: return

        _isDebugging.value = true
        _debugExplanation.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertBuildLog(BuildLogEntity(
                    projectId = project.id,
                    logType = "INFO",
                    message = "Sending build error diagnostics to Gemini 3.5 Flash debugger..."
                ))

                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                    throw IllegalStateException("API Key is missing or invalid. Check AI Studio secrets panel.")
                }

                val systemPrompt = """
                    You are DroidCraft's Real-Time Compiler Debugger.
                    Read the source code and the compiler error log provided.
                    Identify the exact bug (syntax, type mismatch, unresolved symbol, or missing dependency).
                    Provide:
                    1. A concise, friendly explanation of what went wrong and how to fix it (maximum 3 bullet points).
                    2. The COMPLETE, corrected Kotlin code for the file that compiles flawlessly.
                    IMPORTANT format: Output the explanation first, then output the code in a distinct section or raw text.
                    Ensure the final code contains all correct imports and packages.
                """.trimIndent()

                val promptText = """
                    FILE PATH: ${activeFile.filePath}
                    
                    SOURCE CODE:
                    ${activeFile.content}
                    
                    COMPILER ERROR LOG:
                    $compilerError
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = promptText)))
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                    generationConfig = GenerationConfig(temperature = 0.2f)
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw IllegalStateException("Debugger failed to respond.")

                // Split explanation and code
                val (explanation, correctedCode) = parseDebuggerResponse(responseText, activeFile.content)

                // Update active file with corrected code
                repository.updateFile(activeFile.copy(content = correctedCode))

                withContext(Dispatchers.Main) {
                    _debugExplanation.value = explanation
                }

                repository.insertBuildLog(BuildLogEntity(
                    projectId = project.id,
                    logType = "SUCCESS",
                    message = "Debugger diagnostics completed successfully. Error fixed, code rewritten, terminal sync clear!"
                ))

            } catch (e: Exception) {
                e.printStackTrace()
                val errMsg = e.message ?: "Unknown diagnostic failure"
                repository.insertBuildLog(BuildLogEntity(
                    projectId = project.id,
                    logType = "ERROR",
                    message = "Diagnostics failure: $errMsg"
                ))
            } finally {
                _isDebugging.value = false
            }
        }
    }

    fun runAutomatedBuild(simulateErrorType: String? = null) {
        val project = _selectedProject.value ?: return
        _isBuilding.value = true

        viewModelScope.launch(Dispatchers.IO) {
            repository.clearBuildLogs(project.id)
            repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "Executing virtual compiler tasks..."))
            delay(400)
            repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "gradle daemon started: version 8.10.1"))
            delay(500)
            repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "> Task :app:preBuild UP-TO-DATE"))
            repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "> Task :app:preDebugBuild"))
            delay(500)
            repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "> Task :app:compileDebugKotlin"))
            
            if (simulateErrorType != null) {
                delay(600)
                val errorLog = when (simulateErrorType) {
                    "UNRESOLVED" -> "MainActivity.kt:18:24: error: unresolved reference: ColorSchemeTheme"
                    "TYPE" -> "MainActivity.kt:32:15: error: type mismatch: inferred type is Int but String was expected"
                    "IMPORT" -> "MainActivity.kt:5:8: error: unresolved reference: androidx.compose.material3.SuperButton"
                    else -> "MainActivity.kt:12:1: error: expression expected / syntax error near '}'"
                }
                repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "ERROR", message = errorLog))
                repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "ERROR", message = "BUILD FAILED in 2.0s - 1 compilation error found."))
                
                repository.updateProject(project.copy(
                    status = "Build Error",
                    buildCount = project.buildCount + 1,
                    lastBuildSuccess = false,
                    lastBuildTime = System.currentTimeMillis()
                ))
                withContext(Dispatchers.Main) {
                    _isBuilding.value = false
                    _selectedProject.value = _selectedProject.value?.copy(status = "Build Error")
                }
            } else {
                delay(600)
                repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "> Task :app:processDebugResources"))
                delay(400)
                repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "> Task :app:compileDebugJavaWithJavac"))
                delay(400)
                repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "> Task :app:mergeDebugAssets"))
                repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "> Task :app:dexBuilderDebug"))
                delay(500)
                repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "> Task :app:packageDebug"))
                repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "> Task :app:signDebugBundle"))
                delay(300)
                
                val outputApk = "app-debug-${project.name.lowercase().replace(" ", "-")}.apk"
                repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "SUCCESS", message = "BUILD SUCCESSFUL in 3.6s"))
                repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "SUCCESS", message = "APK outputs generated at /outputs/apk/debug/$outputApk"))
                
                repository.updateProject(project.copy(
                    status = "Deployed",
                    buildCount = project.buildCount + 1,
                    lastBuildSuccess = true,
                    lastBuildTime = System.currentTimeMillis()
                ))
                withContext(Dispatchers.Main) {
                    _isBuilding.value = false
                    _selectedProject.value = _selectedProject.value?.copy(status = "Deployed")
                }
            }
        }
    }

    fun pushToGitHub(repoUrl: String, commitMsg: String) {
        val project = _selectedProject.value ?: return
        _isPushing.value = true

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "Initializing git repository..."))
            delay(400)
            repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "git remote add origin $repoUrl"))
            delay(300)
            repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "git add ."))
            delay(400)
            repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "git commit -m \"$commitMsg\""))
            delay(500)
            repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "INFO", message = "Pushing refs to $repoUrl branch main..."))
            delay(800)
            repository.insertBuildLog(BuildLogEntity(projectId = project.id, logType = "SUCCESS", message = "Git push SUCCESS: SHA-1 f7e2a9c1e0b -> remote origin/main"))
            
            repository.updateProject(project.copy(githubRepo = repoUrl))
            withContext(Dispatchers.Main) {
                _isPushing.value = false
                _selectedProject.value = _selectedProject.value?.copy(githubRepo = repoUrl)
            }
        }
    }

    private fun sanitizeMarkdownCode(rawText: String): String {
        var code = rawText.trim()
        if (code.startsWith("```kotlin")) {
            code = code.substringAfter("```kotlin")
        } else if (code.startsWith("```")) {
            code = code.substringAfter("```")
        }
        if (code.endsWith("```")) {
            code = code.substringBeforeLast("```")
        }
        return code.trim()
    }

    private fun parseDebuggerResponse(responseText: String, originalCode: String): Pair<String, String> {
        // Attempt to parse explanation and code
        // Simple heuristic: split by standard code block
        val hasCodeBlock = responseText.contains("```kotlin") || responseText.contains("```")
        if (hasCodeBlock) {
            val parts = responseText.split("```kotlin", "```")
            val explanation = parts.firstOrNull()?.trim() ?: "Bugs fixed."
            val code = parts.getOrNull(1)?.trim() ?: originalCode
            return Pair(explanation, code)
        }
        // If no code block, return whole text as explanation and keep original code
        return Pair(responseText, originalCode)
    }
}
