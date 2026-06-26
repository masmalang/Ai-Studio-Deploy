package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.BuildLogEntity
import com.example.data.ProjectEntity
import com.example.data.ProjectFileEntity
import com.example.data.ProjectViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.SophisticatedBackground
import com.example.ui.theme.SophisticatedBorderAccent
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedNavBackground
import com.example.ui.theme.AccentPillBg
import com.example.ui.theme.DeepBlueText
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.WarningGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.CodeText
import com.example.ui.theme.DarkGrayText
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DroidCraftApp()
            }
        }
    }
}

@Composable
fun DroidCraftApp() {
    val viewModel: ProjectViewModel = viewModel()
    val allProjects by viewModel.allProjects.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val projectFiles by viewModel.projectFiles.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val buildLogs by viewModel.buildLogs.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isBuilding by viewModel.isBuilding.collectAsState()
    val isDebugging by viewModel.isDebugging.collectAsState()
    val isPushing by viewModel.isPushing.collectAsState()
    val debugExplanation by viewModel.debugExplanation.collectAsState()
    val apiError by viewModel.apiError.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()

    var activeTab by remember { mutableStateOf("projects") }
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = SophisticatedNavBackground,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "projects",
                    onClick = { activeTab = "projects" },
                    label = { Text("Projects", color = if (activeTab == "projects") BluePrimary else DarkGrayText) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Projects", tint = if (activeTab == "projects") BluePrimary else DarkGrayText) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = SophisticatedSurface)
                )
                NavigationBarItem(
                    selected = activeTab == "workspace",
                    onClick = { activeTab = "workspace" },
                    enabled = selectedProject != null,
                    label = { Text("IDE Workspace", color = if (selectedProject == null) DarkGrayText.copy(alpha = 0.4f) else if (activeTab == "workspace") BluePrimary else DarkGrayText) },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Workspace", tint = if (selectedProject == null) DarkGrayText.copy(alpha = 0.4f) else if (activeTab == "workspace") BluePrimary else DarkGrayText) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = SophisticatedSurface)
                )
                NavigationBarItem(
                    selected = activeTab == "diagnostics",
                    onClick = { activeTab = "diagnostics" },
                    enabled = selectedProject != null,
                    label = { Text("Git & Deploy", color = if (selectedProject == null) DarkGrayText.copy(alpha = 0.4f) else if (activeTab == "diagnostics") BluePrimary else DarkGrayText) },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Diagnostics", tint = if (selectedProject == null) DarkGrayText.copy(alpha = 0.4f) else if (activeTab == "diagnostics") BluePrimary else DarkGrayText) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = SophisticatedSurface)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SophisticatedBackground)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "projects" -> {
                    ProjectsScreen(
                        allProjects = allProjects,
                        selectedProject = selectedProject,
                        customApiKey = customApiKey,
                        onSaveApiKey = { viewModel.saveCustomApiKey(it) },
                        onProjectSelect = { project ->
                            viewModel.selectProject(project)
                            activeTab = "workspace"
                        },
                        onProjectDelete = { viewModel.deleteProject(it) },
                        onCreateClick = { showCreateDialog = true }
                    )
                }
                "workspace" -> {
                    WorkspaceScreen(
                        selectedProject = selectedProject!!,
                        projectFiles = projectFiles,
                        selectedFile = selectedFile,
                        buildLogs = buildLogs,
                        isGenerating = isGenerating,
                        isBuilding = isBuilding,
                        apiError = apiError,
                        onFileSelect = { viewModel.selectFile(it) },
                        onFileContentChange = { file, content -> viewModel.updateFileContent(file, content) },
                        onGenerateCode = { viewModel.generateCodeWithGemini() },
                        onBuildProject = { simError -> viewModel.runAutomatedBuild(simError) }
                    )
                }
                "diagnostics" -> {
                    DiagnosticsScreen(
                        selectedProject = selectedProject!!,
                        selectedFile = selectedFile,
                        buildLogs = buildLogs,
                        isDebugging = isDebugging,
                        isPushing = isPushing,
                        debugExplanation = debugExplanation,
                        onAskDebug = { errorLog -> viewModel.askGeminiToDebug(errorLog) },
                        onPushGit = { url, commit -> viewModel.pushToGitHub(url, commit) }
                    )
                }
            }

            if (showCreateDialog) {
                CreateProjectDialog(
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name, desc, prompt, packageName, theme ->
                        viewModel.createProject(name, desc, prompt, packageName, theme)
                        showCreateDialog = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    allProjects: List<ProjectEntity>,
    selectedProject: ProjectEntity?,
    customApiKey: String,
    onSaveApiKey: (String) -> Unit,
    onProjectSelect: (ProjectEntity) -> Unit,
    onProjectDelete: (ProjectEntity) -> Unit,
    onCreateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (allProjects.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "DROIDCRAFT AI",
                    style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Serif),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "No Limits Mobile App Architect & Build System",
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkGrayText,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.img_cyber_coder_1782443391390),
                    contentDescription = "Futuristic compiler banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, SophisticatedBorder, RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Design, generate, build and deploy Android apps native with Gemini 3.5 Flash inside a secure local IDE pipeline. Create a new project to start crafting your custom code instantly.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CodeText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                ApiKeySection(
                    customApiKey = customApiKey,
                    onSaveApiKey = onSaveApiKey,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onCreateClick,
                    modifier = Modifier
                        .testTag("create_first_project_button")
                        .widthIn(min = 220.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = DeepBlueText)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CREATE NEW PROJECT", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "PROJECTS STORE",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = BluePrimary
                        )
                        Text(
                            text = "Secure local cloud-based container database",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkGrayText
                        )
                    }

                    FloatingActionButton(
                        onClick = onCreateClick,
                        containerColor = BluePrimary,
                        contentColor = DeepBlueText,
                        modifier = Modifier.testTag("create_project_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Project")
                    }
                }

                ApiKeySection(
                    customApiKey = customApiKey,
                    onSaveApiKey = onSaveApiKey,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(allProjects) { project ->
                        ProjectItemCard(
                            project = project,
                            isSelected = selectedProject?.id == project.id,
                            onSelect = { onProjectSelect(project) },
                            onDelete = { onProjectDelete(project) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectItemCard(
    project: ProjectEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isSelected) BluePrimary else SophisticatedBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                when (project.status) {
                                    "Deployed" -> TerminalGreen
                                    "Build Error" -> ErrorRed
                                    "Building" -> WarningGold
                                    else -> DarkGrayText
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = project.name.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = BluePrimary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete project",
                        tint = ErrorRed.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = CodeText,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "pkg: ${project.packageName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkGrayText,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "theme: ${project.targetTheme}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (project.buildCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = SophisticatedBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Builds: ${project.buildCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CodeText
                    )
                    Text(
                        text = "Last: ${if (project.lastBuildSuccess) "SUCCESSFUL" else "FAILED"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (project.lastBuildSuccess) TerminalGreen else ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WorkspaceScreen(
    selectedProject: ProjectEntity,
    projectFiles: List<ProjectFileEntity>,
    selectedFile: ProjectFileEntity?,
    buildLogs: List<BuildLogEntity>,
    isGenerating: Boolean,
    isBuilding: Boolean,
    apiError: String?,
    onFileSelect: (ProjectFileEntity) -> Unit,
    onFileContentChange: (ProjectFileEntity, String) -> Unit,
    onGenerateCode: () -> Unit,
    onBuildProject: (String?) -> Unit
) {
    var simulateBugSelected by remember { mutableStateOf<String?>(null) }
    var showBugDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Upper banner with project status info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACTIVE CONTAINER: ${selectedProject.name.uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = BluePrimary
                    )
                    Text(
                        text = "Build: ${selectedProject.status} | Files: ${projectFiles.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkGrayText
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onGenerateCode,
                        enabled = !isGenerating && !isBuilding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = DeepBlueText,
                            disabledContainerColor = SophisticatedSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("gemini_gen_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = DeepBlueText, strokeWidth = 1.5.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Gen", modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GEN APP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onBuildProject(simulateBugSelected) },
                        enabled = !isGenerating && !isBuilding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = DeepBlueText,
                            disabledContainerColor = SophisticatedSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("gradle_build_button")
                    ) {
                        if (isBuilding) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = DeepBlueText, strokeWidth = 1.5.dp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GRADLE BUILD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (apiError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, ErrorRed)
            ) {
                Text(
                    text = "Gemini API Connection Required: Make sure to insert your GEMINI_API_KEY in the AI Studio Secrets Panel before executing code-gen.",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed
                )
            }
        }

        // File Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            projectFiles.forEach { file ->
                val isActive = selectedFile?.id == file.id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActive) SophisticatedSurface else Color.Transparent)
                        .border(
                            1.dp,
                            if (isActive) BluePrimary else SophisticatedBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onFileSelect(file) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = file.filePath,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) BluePrimary else DarkGrayText,
                        maxLines = 1
                    )
                }
            }
        }

        // Editor Workspace & Input Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .clip(RoundedCornerShape(8.dp))
                .background(SophisticatedSurface)
                .border(1.dp, SophisticatedBorder, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            if (selectedFile != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FILE: ${selectedFile.filePath}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BluePrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "UTF-8",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkGrayText,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    HorizontalDivider(color = SophisticatedBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxSize()) {
                        // Line numbers
                        val lineCount = selectedFile.content.split('\n').size
                        Column(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .width(24.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            for (i in 1..lineCount) {
                                Text(
                                    text = i.toString(),
                                    color = DarkGrayText.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Code editing TextField
                        BasicTextField(
                            value = selectedFile.content,
                            onValueChange = { onFileContentChange(selectedFile, it) },
                            textStyle = TextStyle(
                                color = CodeText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            cursorBrush = SolidColor(BluePrimary),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("code_editor_field")
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No file loaded", color = DarkGrayText)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Simulated Compiler controls (Inject bug)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = "Debug", tint = WarningGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "DEBUGGER SIMULATOR:",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarningGold,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(SophisticatedSurface)
                    .border(1.dp, SophisticatedBorder, RoundedCornerShape(4.dp))
                    .clickable { showBugDialog = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = simulateBugSelected ?: "NONE (STABLE BUILD)",
                    color = if (simulateBugSelected == null) TerminalGreen else ErrorRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Terminal Console Log
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .border(1.dp, SophisticatedBorder, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "DROIDCRAFT BUILD TERMINAL CONSOLE",
                    fontSize = 10.sp,
                    color = TerminalGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                HorizontalDivider(color = SophisticatedBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                val lazyListState = rememberLazyListState()
                LaunchedEffect(buildLogs.size) {
                    if (buildLogs.isNotEmpty()) {
                        lazyListState.animateScrollToItem(buildLogs.size - 1)
                    }
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (buildLogs.isEmpty()) {
                        item {
                            Text(
                                text = "Terminal idle. Ready to execute automated build task pipeline.",
                                color = DarkGrayText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        items(buildLogs) { log ->
                            Text(
                                text = log.message,
                                color = when (log.logType) {
                                    "SUCCESS" -> TerminalGreen
                                    "ERROR" -> ErrorRed
                                    else -> CodeText
                                },
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBugDialog) {
        Dialog(onDismissRequest = { showBugDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                border = BorderStroke(1.dp, WarningGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SELECT BUG TO INJECT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = WarningGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val bugs = listOf(
                        "UNRESOLVED" to "Unresolved reference: ColorSchemeTheme",
                        "TYPE" to "Type mismatch: expected String, found Int",
                        "IMPORT" to "Unresolved reference: SuperButton",
                        "SYNTAX" to "Syntax error: expression expected near '}'"
                    )

                    bugs.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    simulateBugSelected = key
                                    showBugDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = simulateBugSelected == key,
                                onClick = {
                                    simulateBugSelected = key
                                    showBugDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = WarningGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, color = CodeText, fontSize = 13.sp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                simulateBugSelected = null
                                showBugDialog = false
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = simulateBugSelected == null,
                            onClick = {
                                simulateBugSelected = null
                                showBugDialog = false
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = WarningGold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "NONE (STABLE BUILD)", color = TerminalGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticsScreen(
    selectedProject: ProjectEntity,
    selectedFile: ProjectFileEntity?,
    buildLogs: List<BuildLogEntity>,
    isDebugging: Boolean,
    isPushing: Boolean,
    debugExplanation: String?,
    onAskDebug: (String) -> Unit,
    onPushGit: (String, String) -> Unit
) {
    var gitRepoUrl by remember { mutableStateOf(if (selectedProject.githubRepo.isNotEmpty()) selectedProject.githubRepo else "https://github.com/mania7353/${selectedProject.name.lowercase().replace(" ", "-")}") }
    var commitMessage by remember { mutableStateOf("Initial commit: Generated by DroidCraft AI Studio Builder") }
    var buildDeployedLink by remember { mutableStateOf("") }
    
    val hasError = buildLogs.any { log -> log.logType == "ERROR" }
    val errorLog = buildLogs.find { log -> log.logType == "ERROR" }?.message ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "AI DEBUGGER & REPO SYNC",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = BluePrimary
        )
        Text(
            text = "Real-time error analysis and container deployment",
            style = MaterialTheme.typography.bodySmall,
            color = DarkGrayText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Real-Time Diagnostic Segment
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, if (hasError) ErrorRed.copy(alpha = 0.5f) else BluePrimary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REAL-TIME COMPILER DIAGNOSTICS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (hasError) ErrorRed else TerminalGreen
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (hasError) ErrorRed.copy(alpha = 0.1f) else TerminalGreen.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (hasError) "ERRORS DETECTED" else "BUILD CLEAN",
                            color = if (hasError) ErrorRed else TerminalGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (hasError) {
                    Text(
                        text = "LAST ERROR RECEIVED:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorRed
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black)
                            .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = errorLog,
                            color = ErrorRed,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = { onAskDebug(errorLog) },
                        enabled = !isDebugging,
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_fix_button")
                    ) {
                        if (isDebugging) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        } else {
                            Text("ASK GEMINI TO DEBUG CODE", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Stable", tint = TerminalGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No active compile errors in workspace. Project compiles clean.",
                            color = CodeText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (debugExplanation != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "GEMINI DIAGNOSTIC ANALYSIS:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black)
                            .border(1.dp, BluePrimary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = debugExplanation,
                            color = CodeText,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // GitHub Repository Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "GITHUB REPOSITORY INTEGRATION",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = gitRepoUrl,
                    onValueChange = { gitRepoUrl = it },
                    label = { Text("Repository URL") },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = DarkGrayText,
                        focusedLabelColor = BluePrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    label = { Text("Commit Message") },
                    textStyle = TextStyle(fontSize = 12.sp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = DarkGrayText,
                        focusedLabelColor = BluePrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onPushGit(gitRepoUrl, commitMessage) },
                    enabled = !isPushing,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = DeepBlueText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("github_push_button")
                ) {
                    if (isPushing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DeepBlueText)
                    } else {
                        Text("PUSH TO REPOSITORY REMOTE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Secure Cloud Deployment Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SECURE CLOUD DEPLOYMENT",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Deploy containerized build artifact to high speed Asian CDN deployment endpoints with zero generation limits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkGrayText
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        buildDeployedLink = "https://ais-pre-droidcraft-116038885107.asia-southeast1.run.app"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = DeepBlueText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cloud_deploy_button")
                ) {
                    Text("DEPLOY BUILD UNLIMITED", fontWeight = FontWeight.Bold)
                }

                if (buildDeployedLink.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DEPLOYMENT LINK LIVE:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TerminalGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black)
                            .border(1.dp, TerminalGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = buildDeployedLink,
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("com.aistudio.app") }
    var theme by remember { mutableStateOf("Dark Cosmic") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, BluePrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "NEW CONTAINER PROJECT",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = BluePrimary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("App Name (e.g. Pizza Delivery)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        focusedLabelColor = BluePrimary
                    )
                )

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name") },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        focusedLabelColor = BluePrimary
                    )
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Brief Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        focusedLabelColor = BluePrimary
                    )
                )

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("AI Generation Prompt") },
                    placeholder = { Text("Explain features, buttons, state, and visual specs of the app you want Gemini to write...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        focusedLabelColor = BluePrimary
                    )
                )

                // Theme selection
                Text(
                    text = "TARGET CODING THEME STYLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold
                )

                val themes = listOf("Dark Cosmic", "Teal Minimalist", "Solarized Amber", "Retro Brutalist")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themes.forEach { t ->
                        val isSel = theme == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) BluePrimary else Color.Black)
                                .border(
                                    1.dp,
                                    if (isSel) BluePrimary else DarkGrayText.copy(alpha = 0.3f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { theme = t }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t.split(" ").first(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) DeepBlueText else CodeText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", color = ErrorRed, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && packageName.isNotBlank()) {
                                onConfirm(name, desc, prompt, packageName, theme)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, contentColor = DeepBlueText),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirm_create_project_button")
                    ) {
                        Text("PROVISION CONTAINER", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeySection(
    customApiKey: String,
    onSaveApiKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var keyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security",
                        tint = BluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GEMINI API CONSOLE CONFIG",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Text(
                    text = if (customApiKey.isNotBlank()) "ACTIVE & SECURED" else "REQUIRED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (customApiKey.isNotBlank()) TerminalGreen else WarningGold,
                    modifier = Modifier
                        .background(
                            if (customApiKey.isNotBlank()) TerminalGreen.copy(alpha = 0.15f) else WarningGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your custom Gemini API Key below. This key is saved locally and will be used inside DroidCraft's code compiler and debugger services.",
                style = MaterialTheme.typography.bodySmall,
                color = DarkGrayText,
                lineHeight = 14.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    placeholder = { Text("AIzaSy...", color = DarkGrayText.copy(alpha = 0.5f)) },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick = { isPasswordVisible = !isPasswordVisible },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = if (isPasswordVisible) "HIDE" else "SHOW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_api_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedLabelColor = BluePrimary
                    )
                )

                Button(
                    onClick = {
                        onSaveApiKey(keyInput)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (keyInput != customApiKey) BluePrimary else AccentPillBg,
                        contentColor = if (keyInput != customApiKey) DeepBlueText else CodeText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("save_api_key_button")
                ) {
                    Text(
                        text = "SAVE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
