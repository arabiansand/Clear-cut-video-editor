package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CleanupProject
import com.example.data.model.ExportFormat
import com.example.data.model.ExportResolution
import com.example.data.model.InpaintAlgorithm
import com.example.data.model.NormalizedRect
import com.example.data.model.SelectionType
import com.example.data.repository.ProjectRepository
import com.example.engine.InpaintEngine
import com.example.engine.SampleClipInfo
import com.example.engine.SampleVideos
import com.example.engine.SceneType
import com.example.ui.screens.BatchItem
import com.example.ui.screens.BatchStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class VideoCleanupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    private val prefs = application.getSharedPreferences("mark_f_prefs", Context.MODE_PRIVATE)

    val savedProjects: StateFlow<List<CleanupProject>>

    // Current Project state
    private val _activeProject = MutableStateFlow<CleanupProject?>(null)
    val activeProject: StateFlow<CleanupProject?> = _activeProject.asStateFlow()

    private val _currentBoxes = MutableStateFlow<List<NormalizedRect>>(emptyList())
    val currentBoxes: StateFlow<List<NormalizedRect>> = _currentBoxes.asStateFlow()

    private val _selectedBoxId = MutableStateFlow<String?>(null)
    val selectedBoxId: StateFlow<String?> = _selectedBoxId.asStateFlow()

    // Undo / Redo Stacks
    private val undoStack = mutableListOf<List<NormalizedRect>>()
    private val redoStack = mutableListOf<List<NormalizedRect>>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Playback & Frame Scrubber
    private val _currentTimeMs = MutableStateFlow(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Inpainting Engine Settings
    private val _algorithm = MutableStateFlow(InpaintAlgorithm.CONTENT_AWARE)
    val algorithm: StateFlow<InpaintAlgorithm> = _algorithm.asStateFlow()

    private val _removeScratches = MutableStateFlow(false)
    val removeScratches: StateFlow<Boolean> = _removeScratches.asStateFlow()

    private val _denoise = MutableStateFlow(false)
    val denoise: StateFlow<Boolean> = _denoise.asStateFlow()

    private val _scratchSensitivity = MutableStateFlow(0.5f)
    val scratchSensitivity: StateFlow<Float> = _scratchSensitivity.asStateFlow()

    // Frame Bitmaps
    private val _originalFrame = MutableStateFlow<Bitmap?>(null)
    val originalFrame: StateFlow<Bitmap?> = _originalFrame.asStateFlow()

    private val _cleanedFrame = MutableStateFlow<Bitmap?>(null)
    val cleanedFrame: StateFlow<Bitmap?> = _cleanedFrame.asStateFlow()

    // Export State
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    private val _exportStatusText = MutableStateFlow("")
    val exportStatusText: StateFlow<String> = _exportStatusText.asStateFlow()

    private val _exportCompleted = MutableStateFlow(false)
    val exportCompleted: StateFlow<Boolean> = _exportCompleted.asStateFlow()

    private val _hasAcceptedOnboarding = MutableStateFlow(prefs.getBoolean("accepted_onboarding", false))
    val hasAcceptedOnboarding: StateFlow<Boolean> = _hasAcceptedOnboarding.asStateFlow()

    // Batch Processing State
    private val _batchItems = MutableStateFlow<List<BatchItem>>(
        listOf(
            BatchItem("b1", "Creator Reel Clip 01", "0:12", "Top-Right Logo", BatchStatus.QUEUED, 0f),
            BatchItem("b2", "Creator Reel Clip 02", "0:15", "Top-Right Logo", BatchStatus.QUEUED, 0f),
            BatchItem("b3", "Camera Take 04", "0:22", "Bottom-Left Time", BatchStatus.QUEUED, 0f)
        )
    )
    val batchItems: StateFlow<List<BatchItem>> = _batchItems.asStateFlow()
    private val _isBatchRunning = MutableStateFlow(false)
    val isBatchRunning: StateFlow<Boolean> = _isBatchRunning.asStateFlow()

    private var playbackJob: Job? = null
    private var inpaintJob: Job? = null

    init {
        val db = AppDatabase.getInstance(application)
        repository = ProjectRepository(db.projectDao())
        savedProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun acceptOnboarding() {
        prefs.edit().putBoolean("accepted_onboarding", true).apply()
        _hasAcceptedOnboarding.value = true
    }

    fun loadSample(sample: SampleClipInfo) {
        val proj = CleanupProject(
            id = System.currentTimeMillis(),
            title = sample.title,
            videoUri = "sample://${sample.id}",
            durationMs = 15000L,
            width = 1920,
            height = 1080,
            fps = 30,
            watermarkBoxesJson = boxesToJson(sample.defaultBoxes),
            inpaintMode = sample.defaultInpaintMode,
            removeScratches = sample.hasScratches,
            isSample = true
        )
        openProject(proj, sample.defaultBoxes)
    }

    fun loadUserVideo(uri: Uri) {
        val proj = CleanupProject(
            id = 0,
            title = "Personal Take ${System.currentTimeMillis() % 10000}",
            videoUri = uri.toString(),
            durationMs = 12000L,
            width = 1920,
            height = 1080,
            fps = 30,
            watermarkBoxesJson = "[]",
            inpaintMode = "CONTENT_AWARE",
            isSample = false
        )
        viewModelScope.launch {
            val id = repository.saveProject(proj)
            val saved = proj.copy(id = id)
            // Add a default watermark box
            val defaultBox = listOf(
                NormalizedRect(
                    x = 0.65f, y = 0.05f, width = 0.30f, height = 0.12f,
                    label = "Watermark", type = SelectionType.WATERMARK_LOGO
                )
            )
            openProject(saved, defaultBox)
        }
    }

    fun selectSavedProject(projectId: Long) {
        viewModelScope.launch {
            val proj = repository.getProjectOnce(projectId)
            if (proj != null) {
                val boxes = jsonToBoxes(proj.watermarkBoxesJson)
                openProject(proj, boxes)
            }
        }
    }

    private fun openProject(proj: CleanupProject, boxes: List<NormalizedRect>) {
        _activeProject.value = proj
        _currentBoxes.value = boxes
        _selectedBoxId.value = boxes.firstOrNull()?.id
        _currentTimeMs.value = 0L
        _isPlaying.value = false
        _removeScratches.value = proj.removeScratches
        _denoise.value = proj.denoiseVideo
        _scratchSensitivity.value = proj.scratchSensitivity
        _algorithm.value = try {
            InpaintAlgorithm.valueOf(proj.inpaintMode)
        } catch (_: Exception) {
            InpaintAlgorithm.CONTENT_AWARE
        }

        undoStack.clear()
        redoStack.clear()
        updateUndoRedoStates()

        extractAndInpaintCurrentFrame()
    }

    fun closeActiveProject() {
        playbackJob?.cancel()
        _isPlaying.value = false
        _activeProject.value = null
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_activeProject.value?.id == id) {
                closeActiveProject()
            }
        }
    }

    // --- Box Selection & Editing ---

    private fun pushUndo() {
        undoStack.add(ArrayList(_currentBoxes.value))
        if (undoStack.size > 25) undoStack.removeAt(0)
        redoStack.clear()
        updateUndoRedoStates()
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(ArrayList(_currentBoxes.value))
            _currentBoxes.value = undoStack.removeAt(undoStack.lastIndex)
            updateUndoRedoStates()
            triggerInpaint()
            persistCurrentProject()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(ArrayList(_currentBoxes.value))
            _currentBoxes.value = redoStack.removeAt(redoStack.lastIndex)
            updateUndoRedoStates()
            triggerInpaint()
            persistCurrentProject()
        }
    }

    fun selectBox(id: String) {
        _selectedBoxId.value = id
    }

    fun updateBox(updated: NormalizedRect) {
        pushUndo()
        val list = _currentBoxes.value.toMutableList()
        val index = list.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            list[index] = updated
            _currentBoxes.value = list
            triggerInpaint()
            persistCurrentProject()
        }
    }

    fun deleteBox(id: String) {
        pushUndo()
        _currentBoxes.value = _currentBoxes.value.filter { it.id != id }
        if (_selectedBoxId.value == id) {
            _selectedBoxId.value = _currentBoxes.value.firstOrNull()?.id
        }
        triggerInpaint()
        persistCurrentProject()
    }

    fun addBox(type: SelectionType) {
        pushUndo()
        val newBox = when (type) {
            SelectionType.WATERMARK_LOGO -> NormalizedRect(
                x = 0.65f, y = 0.06f, width = 0.30f, height = 0.12f,
                label = "Logo Watermark", type = type
            )
            SelectionType.TIMESTAMP_DATE -> NormalizedRect(
                x = 0.05f, y = 0.82f, width = 0.40f, height = 0.10f,
                label = "Date / Time", type = type
            )
            SelectionType.TEXT_SUBTITLE -> NormalizedRect(
                x = 0.15f, y = 0.72f, width = 0.70f, height = 0.14f,
                label = "Subtitle Overlay", type = type
            )
            SelectionType.DUST_SCRATCH -> NormalizedRect(
                x = 0.30f, y = 0.20f, width = 0.15f, height = 0.15f,
                label = "Dust / Spot", type = type
            )
            SelectionType.UNWANTED_OBJECT -> NormalizedRect(
                x = 0.40f, y = 0.40f, width = 0.20f, height = 0.20f,
                label = "Object", type = type
            )
        }
        _currentBoxes.value = _currentBoxes.value + newBox
        _selectedBoxId.value = newBox.id
        triggerInpaint()
        persistCurrentProject()
    }

    fun applyPreset(preset: String) {
        pushUndo()
        val box = when (preset) {
            "Top-Right Logo" -> NormalizedRect(x = 0.66f, y = 0.05f, width = 0.30f, height = 0.12f, label = "Logo (TR)", type = SelectionType.WATERMARK_LOGO)
            "Bottom-Left Time" -> NormalizedRect(x = 0.04f, y = 0.82f, width = 0.38f, height = 0.10f, label = "Timestamp (BL)", type = SelectionType.TIMESTAMP_DATE)
            "Lower-Third" -> NormalizedRect(x = 0.05f, y = 0.70f, width = 0.48f, height = 0.16f, label = "Lower-Third", type = SelectionType.TEXT_SUBTITLE)
            else -> NormalizedRect(x = 0.05f, y = 0.05f, width = 0.25f, height = 0.12f, label = "Corner Watermark", type = SelectionType.WATERMARK_LOGO)
        }
        _currentBoxes.value = _currentBoxes.value + box
        _selectedBoxId.value = box.id
        triggerInpaint()
        persistCurrentProject()
    }

    // --- Settings & Algorithms ---

    fun setAlgorithm(algo: InpaintAlgorithm) {
        _algorithm.value = algo
        triggerInpaint()
        persistCurrentProject()
    }

    fun toggleScratches(enabled: Boolean) {
        _removeScratches.value = enabled
        triggerInpaint()
        persistCurrentProject()
    }

    fun toggleDenoise(enabled: Boolean) {
        _denoise.value = enabled
        triggerInpaint()
        persistCurrentProject()
    }

    fun setScratchSensitivity(sens: Float) {
        _scratchSensitivity.value = sens
        triggerInpaint()
        persistCurrentProject()
    }

    // --- Transport & Timeline ---

    fun seekTo(timeMs: Long) {
        val proj = _activeProject.value ?: return
        _currentTimeMs.value = timeMs.coerceIn(0L, proj.durationMs)
        extractAndInpaintCurrentFrame()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            _isPlaying.value = false
            playbackJob?.cancel()
        } else {
            _isPlaying.value = true
            startPlaybackLoop()
        }
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val proj = _activeProject.value ?: return@launch
            while (_isPlaying.value) {
                delay(100L) // 10 fps UI step for smooth playback in container
                val next = _currentTimeMs.value + 100L
                if (next >= proj.durationMs) {
                    _currentTimeMs.value = 0L
                } else {
                    _currentTimeMs.value = next
                }
                extractAndInpaintCurrentFrame()
            }
        }
    }

    private fun extractAndInpaintCurrentFrame() {
        val proj = _activeProject.value ?: return
        val progress = if (proj.durationMs > 0) (_currentTimeMs.value.toFloat() / proj.durationMs).coerceIn(0f, 1f) else 0f

        viewModelScope.launch(Dispatchers.Default) {
            val sourceBitmap = if (proj.isSample) {
                val scene = when {
                    proj.videoUri.contains("sample_drone") -> SceneType.DRONE_SUNSET
                    proj.videoUri.contains("sample_studio") -> SceneType.STUDIO_INTERVIEW
                    proj.videoUri.contains("sample_vintage") -> SceneType.VINTAGE_ARCHIVE
                    else -> SceneType.TECH_VLOG
                }
                SampleVideos.generateFrame(scene, progress)
            } else {
                extractFrameFromUri(proj.videoUri, _currentTimeMs.value)
                    ?: SampleVideos.generateFrame(SceneType.TECH_VLOG, progress)
            }

            _originalFrame.value = sourceBitmap
            runInpaintingOn(sourceBitmap)
        }
    }

    private fun triggerInpaint() {
        val orig = _originalFrame.value ?: return
        inpaintJob?.cancel()
        inpaintJob = viewModelScope.launch(Dispatchers.Default) {
            runInpaintingOn(orig)
        }
    }

    private suspend fun runInpaintingOn(sourceBitmap: Bitmap) {
        val result = InpaintEngine.processFrame(
            source = sourceBitmap,
            boxes = _currentBoxes.value,
            algorithm = _algorithm.value,
            removeScratches = _removeScratches.value,
            denoise = _denoise.value,
            scratchSensitivity = _scratchSensitivity.value
        )
        _cleanedFrame.value = result
    }

    private fun extractFrameFromUri(uriString: String, timeMs: Long): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(getApplication(), Uri.parse(uriString))
            retriever.getFrameAtTime(timeMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (_: Exception) {
            null
        }
    }

    private fun persistCurrentProject() {
        val proj = _activeProject.value ?: return
        val updated = proj.copy(
            watermarkBoxesJson = boxesToJson(_currentBoxes.value),
            inpaintMode = _algorithm.value.name,
            removeScratches = _removeScratches.value,
            denoiseVideo = _denoise.value,
            scratchSensitivity = _scratchSensitivity.value,
            updatedAt = System.currentTimeMillis()
        )
        _activeProject.value = updated
        viewModelScope.launch {
            repository.saveProject(updated)
        }
    }

    // --- Export Simulation & Rendering Pipeline ---

    fun startExport(format: ExportFormat, resolution: ExportResolution, preserveAudio: Boolean) {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            _isExporting.value = true
            _exportProgress.value = 0f
            _exportCompleted.value = false
            _exportStatusText.value = "Extracting video frames and isolating audio stream..."

            val totalFrames = 30
            for (i in 1..totalFrames) {
                delay(80L)
                _exportProgress.value = i.toFloat() / totalFrames
                when {
                    i < 8 -> _exportStatusText.value = "Formulating inpainting boundary masks (Frame $i/$totalFrames)..."
                    i < 20 -> _exportStatusText.value = "Applying ${_algorithm.value.displayName} texture synthesis ($i/$totalFrames)..."
                    i < 28 -> _exportStatusText.value = "Running temporal consistency filter & scratch removal..."
                    else -> _exportStatusText.value = "Re-multiplexing synchronized audio track to ${format.name}..."
                }
            }

            // Save sample exported artifact
            val context = getApplication<Application>()
            val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
            val outFile = File(exportDir, "clean_${System.currentTimeMillis()}.${format.extension}")
            try {
                _cleanedFrame.value?.let { bmp ->
                    FileOutputStream(outFile).use { fos ->
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                }
            } catch (_: Exception) {}

            _exportStatusText.value = "Rendering complete! Output saved to: ${outFile.name}"
            _exportCompleted.value = true
            _isExporting.value = false

            // Update project status in DB
            val updated = proj.copy(
                status = "EXPORTED",
                lastExportUri = outFile.absolutePath,
                updatedAt = System.currentTimeMillis()
            )
            _activeProject.value = updated
            repository.saveProject(updated)
        }
    }

    fun dismissExport() {
        _isExporting.value = false
        _exportCompleted.value = false
        _exportProgress.value = 0f
    }

    // --- Batch Processing Queue ---

    fun startBatchProcessing() {
        if (_isBatchRunning.value) return
        viewModelScope.launch {
            _isBatchRunning.value = true
            val items = _batchItems.value.toMutableList()

            for (i in items.indices) {
                if (items[i].status == BatchStatus.COMPLETED) continue

                items[i] = items[i].copy(status = BatchStatus.PROCESSING, progress = 0f)
                _batchItems.value = items.toList()

                for (step in 1..10) {
                    delay(120L)
                    items[i] = items[i].copy(progress = step / 10f)
                    _batchItems.value = items.toList()
                }

                items[i] = items[i].copy(status = BatchStatus.COMPLETED, progress = 1.0f)
                _batchItems.value = items.toList()
            }

            _isBatchRunning.value = false
        }
    }

    fun addSampleToBatch() {
        val newId = "b_${System.currentTimeMillis()}"
        val num = _batchItems.value.size + 1
        val newItem = BatchItem(
            id = newId,
            title = "Personal Camera Take #$num",
            durationText = "0:${10 + num * 2}",
            watermarkZone = if (num % 2 == 0) "Bottom-Left Time" else "Top-Right Logo",
            status = BatchStatus.QUEUED,
            progress = 0f
        )
        _batchItems.value = _batchItems.value + newItem
    }

    fun removeBatchItem(id: String) {
        _batchItems.value = _batchItems.value.filter { it.id != id }
    }

    // --- JSON serialization helpers for NormalizedRect ---

    private fun boxesToJson(boxes: List<NormalizedRect>): String {
        val arr = JSONArray()
        boxes.forEach { box ->
            val obj = JSONObject().apply {
                put("id", box.id)
                put("x", box.x.toDouble())
                put("y", box.y.toDouble())
                put("width", box.width.toDouble())
                put("height", box.height.toDouble())
                put("label", box.label)
                put("type", box.type.name)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun jsonToBoxes(json: String): List<NormalizedRect> {
        val result = mutableListOf<NormalizedRect>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                result.add(
                    NormalizedRect(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        x = obj.optDouble("x", 0.0).toFloat(),
                        y = obj.optDouble("y", 0.0).toFloat(),
                        width = obj.optDouble("width", 0.2).toFloat(),
                        height = obj.optDouble("height", 0.1).toFloat(),
                        label = obj.optString("label", "Watermark"),
                        type = try {
                            SelectionType.valueOf(obj.optString("type", "WATERMARK_LOGO"))
                        } catch (_: Exception) {
                            SelectionType.WATERMARK_LOGO
                        }
                    )
                )
            }
        } catch (_: Exception) {}
        return result
    }
}
