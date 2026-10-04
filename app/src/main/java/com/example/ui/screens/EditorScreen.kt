package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CleanupProject
import com.example.data.model.InpaintAlgorithm
import com.example.data.model.NormalizedRect
import com.example.data.model.SelectionType
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.TimelineScrubber
import com.example.ui.components.WatermarkSelectorOverlay
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

enum class PreviewMode {
    SELECTION_EDIT,
    BEFORE_AFTER_SLIDER,
    CLEANED_PREVIEW
}

@Composable
fun EditorScreen(
    project: CleanupProject,
    originalFrame: Bitmap,
    cleanedFrame: Bitmap,
    boxes: List<NormalizedRect>,
    selectedBoxId: String?,
    canUndo: Boolean,
    canRedo: Boolean,
    currentTimeMs: Long,
    isPlaying: Boolean,
    algorithm: InpaintAlgorithm,
    removeScratches: Boolean,
    denoise: Boolean,
    scratchSensitivity: Float,
    onNavigateBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSelectBox: (String) -> Unit,
    onUpdateBox: (NormalizedRect) -> Unit,
    onDeleteBox: (String) -> Unit,
    onAddBox: (SelectionType) -> Unit,
    onApplyPreset: (String) -> Unit,
    onSetAlgorithm: (InpaintAlgorithm) -> Unit,
    onToggleScratches: (Boolean) -> Unit,
    onToggleDenoise: (Boolean) -> Unit,
    onSetSensitivity: (Float) -> Unit,
    onSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenInfo: () -> Unit
) {
    BackHandler { onNavigateBack() }

    var previewMode by remember { mutableStateOf(PreviewMode.SELECTION_EDIT) }
    var sliderSplitPosition by remember { mutableFloatStateOf(0.5f) }
    var activeBottomTab by remember { mutableIntStateOf(0) } // 0: Boxes, 1: Algorithms, 2: Restoration
    var showAlgorithmMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground)
            .testTag("editor_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp).testTag("button_back_editor")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = project.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${boxes.size} clean zones • ${algorithm.displayName}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Undo / Redo
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(32.dp).testTag("button_undo")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) TextPrimary else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(32.dp).testTag("button_redo")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) TextPrimary else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Export Button
                Button(
                    onClick = onOpenExport,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("button_open_export_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Preview Mode Selector Pill Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(StudioSurface)
                    .border(1.dp, StudioCardBorder, RoundedCornerShape(20.dp))
                    .padding(3.dp)
            ) {
                val modes = listOf(
                    Triple(PreviewMode.SELECTION_EDIT, Icons.Default.Edit, "Select Boxes"),
                    Triple(PreviewMode.BEFORE_AFTER_SLIDER, Icons.AutoMirrored.Filled.CompareArrows, "Before / After"),
                    Triple(PreviewMode.CLEANED_PREVIEW, Icons.Default.Visibility, "Cleaned Frame")
                )

                modes.forEach { (mode, icon, label) ->
                    val isSel = previewMode == mode
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) CyanAccent else Color.Transparent)
                            .clickable { previewMode = mode }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSel) Color.Black else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            color = if (isSel) Color.Black else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Main Video Canvas Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            when (previewMode) {
                PreviewMode.SELECTION_EDIT -> {
                    // Frame with Draggable Watermark Selector Overlay
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = originalFrame.asImageBitmap(),
                            contentDescription = "Video Frame",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        WatermarkSelectorOverlay(
                            boxes = boxes,
                            selectedBoxId = selectedBoxId,
                            onSelectBox = onSelectBox,
                            onUpdateBox = onUpdateBox,
                            onDeleteBox = onDeleteBox
                        )
                    }
                }

                PreviewMode.BEFORE_AFTER_SLIDER -> {
                    BeforeAfterSlider(
                        beforeBitmap = originalFrame,
                        afterBitmap = cleanedFrame,
                        sliderPosition = sliderSplitPosition,
                        onPositionChanged = { sliderSplitPosition = it }
                    )
                }

                PreviewMode.CLEANED_PREVIEW -> {
                    Image(
                        bitmap = cleanedFrame.asImageBitmap(),
                        contentDescription = "Cleaned Result",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // Timeline Transport & Scrubber
        TimelineScrubber(
            currentTimeMs = currentTimeMs,
            totalDurationMs = project.durationMs,
            isPlaying = isPlaying,
            onTogglePlay = onTogglePlay,
            onSeek = onSeek,
            fps = project.fps,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        // Bottom Tool Configuration Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(bottom = 8.dp)
        ) {
            // Tabs: [Selection Tools] [Inpaint Engine] [Video Restoration]
            TabRow(
                selectedTabIndex = activeBottomTab,
                containerColor = StudioSurface,
                contentColor = CyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeBottomTab]),
                        color = CyanAccent
                    )
                }
            ) {
                Tab(
                    selected = activeBottomTab == 0,
                    onClick = { activeBottomTab = 0 },
                    text = { Text("Selection Zones (${boxes.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = activeBottomTab == 1,
                    onClick = { activeBottomTab = 1 },
                    text = { Text("AI Inpaint Fill", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = activeBottomTab == 2,
                    onClick = { activeBottomTab = 2 },
                    text = { Text("Restoration & Dust", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                when (activeBottomTab) {
                    0 -> SelectionZonesPanel(
                        boxes = boxes,
                        selectedBoxId = selectedBoxId,
                        onAddBox = onAddBox,
                        onApplyPreset = onApplyPreset,
                        onSelectBox = onSelectBox,
                        onDeleteBox = onDeleteBox
                    )
                    1 -> InpaintAlgorithmPanel(
                        currentAlgorithm = algorithm,
                        onSelectAlgorithm = onSetAlgorithm,
                        onOpenDocs = onOpenInfo
                    )
                    2 -> RestorationFiltersPanel(
                        removeScratches = removeScratches,
                        denoise = denoise,
                        scratchSensitivity = scratchSensitivity,
                        onToggleScratches = onToggleScratches,
                        onToggleDenoise = onToggleDenoise,
                        onSetSensitivity = onSetSensitivity
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionZonesPanel(
    boxes: List<NormalizedRect>,
    selectedBoxId: String?,
    onAddBox: (SelectionType) -> Unit,
    onApplyPreset: (String) -> Unit,
    onSelectBox: (String) -> Unit,
    onDeleteBox: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Action Buttons to add Watermark / Timestamp / Text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onAddBox(SelectionType.WATERMARK_LOGO) },
                colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(36.dp).testTag("button_add_logo_box")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Logo Box", color = TextPrimary, fontSize = 11.sp)
            }

            Button(
                onClick = { onAddBox(SelectionType.TIMESTAMP_DATE) },
                colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(36.dp).testTag("button_add_timestamp_box")
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Time Stamp", color = TextPrimary, fontSize = 11.sp)
            }

            Button(
                onClick = { onAddBox(SelectionType.TEXT_SUBTITLE) },
                colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(36.dp).testTag("button_add_text_box")
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Text Overlay", color = TextPrimary, fontSize = 11.sp)
            }
        }

        // Quick Presets Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Presets:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            val presets = listOf("Top-Right Logo", "Bottom-Left Time", "Lower-Third", "Corner Watermark")
            presets.forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioSurfaceVariant)
                        .clickable { onApplyPreset(preset) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(text = preset, color = TextSecondary, fontSize = 10.sp)
                }
            }
        }

        if (boxes.isEmpty()) {
            Text(
                text = "Tap '+ Logo Box' or a preset to place a draggable selection box over the mark to remove.",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun InpaintAlgorithmPanel(
    currentAlgorithm: InpaintAlgorithm,
    onSelectAlgorithm: (InpaintAlgorithm) -> Unit,
    onOpenDocs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SELECT INPAINTING FILL ENGINE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.clickable(onClick = onOpenDocs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Architecture Docs", color = CyanAccent, fontSize = 10.sp)
            }
        }

        InpaintAlgorithm.values().forEach { algo ->
            val isSel = currentAlgorithm == algo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSel) CyanAccent.copy(alpha = 0.15f) else StudioSurfaceVariant)
                    .border(1.dp, if (isSel) CyanAccent else StudioCardBorder, RoundedCornerShape(8.dp))
                    .clickable { onSelectAlgorithm(algo) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = algo.displayName, color = if (isSel) CyanAccent else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = algo.description, color = TextSecondary, fontSize = 9.sp, maxLines = 1)
                }
                if (isSel) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RestorationFiltersPanel(
    removeScratches: Boolean,
    denoise: Boolean,
    scratchSensitivity: Float,
    onToggleScratches: (Boolean) -> Unit,
    onToggleDenoise: (Boolean) -> Unit,
    onSetSensitivity: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Scratch & Dust Clean Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StudioSurfaceVariant)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Celluloid Dust & Scratch Filter", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Cleans film lines and sensor dust automatically", color = TextSecondary, fontSize = 9.sp)
            }
            Switch(
                checked = removeScratches,
                onCheckedChange = onToggleScratches,
                colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CyanAccent.copy(alpha = 0.35f)),
                modifier = Modifier.testTag("switch_scratches")
            )
        }

        // Noise / Grain reduction switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StudioSurfaceVariant)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Video Noise & Grain Smoothing", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Reduces digital sensor noise around filled patches", color = TextSecondary, fontSize = 9.sp)
            }
            Switch(
                checked = denoise,
                onCheckedChange = onToggleDenoise,
                colors = SwitchDefaults.colors(checkedThumbColor = VioletAccent, checkedTrackColor = VioletAccent.copy(alpha = 0.35f)),
                modifier = Modifier.testTag("switch_denoise")
            )
        }

        if (removeScratches) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sensitivity:", color = TextMuted, fontSize = 10.sp)
                Slider(
                    value = scratchSensitivity,
                    onValueChange = onSetSensitivity,
                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Text("${(scratchSensitivity * 100).toInt()}%", color = CyanAccent, fontSize = 10.sp)
            }
        }
    }
}
