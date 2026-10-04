package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MediaStoreVideoPicker
import com.example.ui.screens.ArchitectureAndPolicyScreen
import com.example.ui.screens.BatchProcessingScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ExportDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.VideoCleanupViewModel

enum class AppDestination {
    HOME,
    MEDIA_STORE_PICKER,
    EDITOR,
    BATCH,
    ARCHITECTURE_DOCS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VideoCleanupApp()
            }
        }
    }
}

@Composable
fun VideoCleanupApp(
    viewModel: VideoCleanupViewModel = viewModel()
) {
    var destination by remember { mutableStateOf(AppDestination.HOME) }
    var showExportDialog by remember { mutableStateOf(false) }

    val savedProjects by viewModel.savedProjects.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val currentBoxes by viewModel.currentBoxes.collectAsStateWithLifecycle()
    val selectedBoxId by viewModel.selectedBoxId.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val currentTimeMs by viewModel.currentTimeMs.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val algorithm by viewModel.algorithm.collectAsStateWithLifecycle()
    val removeScratches by viewModel.removeScratches.collectAsStateWithLifecycle()
    val denoise by viewModel.denoise.collectAsStateWithLifecycle()
    val scratchSensitivity by viewModel.scratchSensitivity.collectAsStateWithLifecycle()
    val originalFrame by viewModel.originalFrame.collectAsStateWithLifecycle()
    val cleanedFrame by viewModel.cleanedFrame.collectAsStateWithLifecycle()
    val hasAcceptedOnboarding by viewModel.hasAcceptedOnboarding.collectAsStateWithLifecycle()

    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val exportStatusText by viewModel.exportStatusText.collectAsStateWithLifecycle()
    val exportCompleted by viewModel.exportCompleted.collectAsStateWithLifecycle()

    val batchItems by viewModel.batchItems.collectAsStateWithLifecycle()
    val isBatchRunning by viewModel.isBatchRunning.collectAsStateWithLifecycle()

    // Handle back button according to destination
    when (destination) {
        AppDestination.HOME -> { /* system default back */ }
        AppDestination.MEDIA_STORE_PICKER -> {
            BackHandler {
                destination = AppDestination.HOME
            }
        }
        AppDestination.EDITOR -> {
            BackHandler {
                viewModel.closeActiveProject()
                destination = AppDestination.HOME
            }
        }
        AppDestination.BATCH -> {
            BackHandler {
                destination = AppDestination.HOME
            }
        }
        AppDestination.ARCHITECTURE_DOCS -> {
            BackHandler {
                destination = if (activeProject != null) AppDestination.EDITOR else AppDestination.HOME
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (destination) {
                AppDestination.HOME -> {
                    HomeScreen(
                        projects = savedProjects,
                        hasAcceptedOnboarding = hasAcceptedOnboarding,
                        onAcceptOnboarding = { viewModel.acceptOnboarding() },
                        onSelectProject = { projectId ->
                            viewModel.selectSavedProject(projectId)
                            destination = AppDestination.EDITOR
                        },
                        onSelectSample = { sample ->
                            viewModel.loadSample(sample)
                            destination = AppDestination.EDITOR
                        },
                        onOpenGalleryVideo = { uri ->
                            viewModel.loadUserVideo(uri)
                            destination = AppDestination.EDITOR
                        },
                        onNavigateToMediaStorePicker = {
                            destination = AppDestination.MEDIA_STORE_PICKER
                        },
                        onDeleteProject = { id ->
                            viewModel.deleteProject(id)
                        },
                        onNavigateToBatch = {
                            destination = AppDestination.BATCH
                        },
                        onNavigateToArchitecture = {
                            destination = AppDestination.ARCHITECTURE_DOCS
                        }
                    )
                }

                AppDestination.MEDIA_STORE_PICKER -> {
                    MediaStoreVideoPicker(
                        onVideoSelected = { uri ->
                            viewModel.loadUserVideo(uri)
                            destination = AppDestination.EDITOR
                        },
                        onNavigateBack = {
                            destination = AppDestination.HOME
                        }
                    )
                }

                AppDestination.EDITOR -> {
                    val proj = activeProject
                    val orig = originalFrame
                    val clean = cleanedFrame

                    if (proj != null && orig != null && clean != null) {
                        EditorScreen(
                            project = proj,
                            originalFrame = orig,
                            cleanedFrame = clean,
                            boxes = currentBoxes,
                            selectedBoxId = selectedBoxId,
                            canUndo = canUndo,
                            canRedo = canRedo,
                            currentTimeMs = currentTimeMs,
                            isPlaying = isPlaying,
                            algorithm = algorithm,
                            removeScratches = removeScratches,
                            denoise = denoise,
                            scratchSensitivity = scratchSensitivity,
                            onNavigateBack = {
                                viewModel.closeActiveProject()
                                destination = AppDestination.HOME
                            },
                            onUndo = { viewModel.undo() },
                            onRedo = { viewModel.redo() },
                            onSelectBox = { id -> viewModel.selectBox(id) },
                            onUpdateBox = { box -> viewModel.updateBox(box) },
                            onDeleteBox = { id -> viewModel.deleteBox(id) },
                            onAddBox = { type -> viewModel.addBox(type) },
                            onApplyPreset = { preset -> viewModel.applyPreset(preset) },
                            onSetAlgorithm = { algo -> viewModel.setAlgorithm(algo) },
                            onToggleScratches = { enabled -> viewModel.toggleScratches(enabled) },
                            onToggleDenoise = { enabled -> viewModel.toggleDenoise(enabled) },
                            onSetSensitivity = { sens -> viewModel.setScratchSensitivity(sens) },
                            onSeek = { ms -> viewModel.seekTo(ms) },
                            onTogglePlay = { viewModel.togglePlayPause() },
                            onOpenExport = { showExportDialog = true },
                            onOpenInfo = { destination = AppDestination.ARCHITECTURE_DOCS }
                        )
                    }
                }

                AppDestination.BATCH -> {
                    BatchProcessingScreen(
                        items = batchItems,
                        isProcessing = isBatchRunning,
                        onNavigateBack = { destination = AppDestination.HOME },
                        onStartBatch = { viewModel.startBatchProcessing() },
                        onAddSampleToBatch = { viewModel.addSampleToBatch() },
                        onRemoveItem = { id -> viewModel.removeBatchItem(id) }
                    )
                }

                AppDestination.ARCHITECTURE_DOCS -> {
                    ArchitectureAndPolicyScreen(
                        onNavigateBack = {
                            destination = if (activeProject != null) AppDestination.EDITOR else AppDestination.HOME
                        }
                    )
                }
            }

            // Export Dialog
            if (showExportDialog) {
                ExportDialog(
                    onDismiss = {
                        viewModel.dismissExport()
                        showExportDialog = false
                    },
                    onStartExport = { format, resolution, preserveAudio ->
                        viewModel.startExport(format, resolution, preserveAudio)
                    },
                    isExporting = isExporting,
                    exportProgress = exportProgress,
                    exportStatusText = exportStatusText,
                    exportCompleted = exportCompleted,
                    onShareOrOpen = {
                        viewModel.dismissExport()
                        showExportDialog = false
                    }
                )
            }
        }
    }
}
