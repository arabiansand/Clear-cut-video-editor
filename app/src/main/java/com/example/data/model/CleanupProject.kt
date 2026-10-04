package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cleanup_projects")
data class CleanupProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val videoUri: String,
    val durationMs: Long = 10000L,
    val width: Int = 1920,
    val height: Int = 1080,
    val fps: Int = 30,
    val watermarkBoxesJson: String = "[]",
    val inpaintMode: String = "CONTENT_AWARE",
    val removeScratches: Boolean = true,
    val denoiseVideo: Boolean = false,
    val scratchSensitivity: Float = 0.5f,
    val status: String = "DRAFT", // DRAFT, PROCESSED, EXPORTED
    val lastExportUri: String? = null,
    val thumbnailUri: String? = null,
    val isSample: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class NormalizedRect(
    val id: String = java.util.UUID.randomUUID().toString(),
    val x: Float, // 0f .. 1f
    val y: Float, // 0f .. 1f
    val width: Float, // 0f .. 1f
    val height: Float, // 0f .. 1f
    val label: String = "Watermark",
    val type: SelectionType = SelectionType.WATERMARK_LOGO
)

enum class SelectionType(val displayName: String) {
    WATERMARK_LOGO("Logo / Watermark"),
    TIMESTAMP_DATE("Date & Time Stamp"),
    TEXT_SUBTITLE("Text / Lower Third"),
    DUST_SCRATCH("Dust / Scratch"),
    UNWANTED_OBJECT("Clutter / Object")
}

enum class InpaintAlgorithm(val displayName: String, val description: String) {
    CONTENT_AWARE(
        "AI Content-Aware Fill",
        "Propagates surrounding scene textures and structural gradient vectors into the region."
    ),
    TELEA_FAST_MARCHING(
        "Telea Fast Marching",
        "Smooth boundary interpolation using gradient-weighted Laplacian field."
    ),
    TEMPORAL_PATCH(
        "Temporal Patch Match",
        "Multi-frame temporal motion estimation for moving backgrounds."
    ),
    SMART_EDGE_BLUR(
        "Smart Edge Blur",
        "Soft edge blending with background tint matching."
    )
}

enum class ExportFormat(val extension: String, val mimeType: String, val label: String) {
    MP4("mp4", "video/mp4", "MP4 (H.264 Video)"),
    MOV("mov", "video/quicktime", "MOV (Apple ProRes / QuickTime)"),
    GIF("gif", "image/gif", "GIF (High-FPS Animation)"),
    WEBM("webm", "video/webm", "WebM (High-Efficiency Web)")
}

enum class ExportResolution(val label: String, val scale: Float, val aspect: String) {
    ORIGINAL("Original Resolution (1080p)", 1.0f, "16:9"),
    HD_720("720p HD (Fast Export)", 0.66f, "16:9"),
    SD_480("480p SD (Compact)", 0.44f, "16:9"),
    SQUARE("1:1 Square (Instagram / Post)", 1.0f, "1:1"),
    VERTICAL_9_16("9:16 Vertical (Reels / Shorts / TikTok)", 1.0f, "9:16")
}
