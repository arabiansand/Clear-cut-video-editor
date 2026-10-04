package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.data.model.CleanupProject
import com.example.data.model.NormalizedRect
import com.example.data.model.SelectionType

data class SampleClipInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val durationText: String,
    val defaultBoxes: List<NormalizedRect>,
    val defaultInpaintMode: String,
    val hasScratches: Boolean,
    val sceneType: SceneType
)

enum class SceneType {
    TECH_VLOG,
    DRONE_SUNSET,
    STUDIO_INTERVIEW,
    VINTAGE_ARCHIVE
}

object SampleVideos {

    val SAMPLES = listOf(
        SampleClipInfo(
            id = "sample_tech_vlog",
            title = "Tech Studio B-Roll",
            subtitle = "Contains creator corner watermark & branding tag",
            durationText = "0:12",
            defaultBoxes = listOf(
                NormalizedRect(
                    id = "box_1",
                    x = 0.68f,
                    y = 0.08f,
                    width = 0.28f,
                    height = 0.12f,
                    label = "Logo Watermark",
                    type = SelectionType.WATERMARK_LOGO
                )
            ),
            defaultInpaintMode = "CONTENT_AWARE",
            hasScratches = false,
            sceneType = SceneType.TECH_VLOG
        ),
        SampleClipInfo(
            id = "sample_drone_sunset",
            title = "Aerial Coastline Clip",
            subtitle = "Contains automated camera timestamp & GPS timecode",
            durationText = "0:18",
            defaultBoxes = listOf(
                NormalizedRect(
                    id = "box_2",
                    x = 0.04f,
                    y = 0.82f,
                    width = 0.38f,
                    height = 0.10f,
                    label = "Date/Time Stamp",
                    type = SelectionType.TIMESTAMP_DATE
                )
            ),
            defaultInpaintMode = "CONTENT_AWARE",
            hasScratches = false,
            sceneType = SceneType.DRONE_SUNSET
        ),
        SampleClipInfo(
            id = "sample_studio_talk",
            title = "Interview Lower-Third",
            subtitle = "Contains old overlay title graphic & handle",
            durationText = "0:15",
            defaultBoxes = listOf(
                NormalizedRect(
                    id = "box_3",
                    x = 0.05f,
                    y = 0.70f,
                    width = 0.45f,
                    height = 0.16f,
                    label = "Lower-Third Text",
                    type = SelectionType.TEXT_SUBTITLE
                )
            ),
            defaultInpaintMode = "TELEA_FAST_MARCHING",
            hasScratches = false,
            sceneType = SceneType.STUDIO_INTERVIEW
        ),
        SampleClipInfo(
            id = "sample_vintage_film",
            title = "Archive Film Restoration",
            subtitle = "Contains celluloid dust, scratches, and vintage timestamp",
            durationText = "0:20",
            defaultBoxes = listOf(
                NormalizedRect(
                    id = "box_4",
                    x = 0.32f,
                    y = 0.88f,
                    width = 0.36f,
                    height = 0.08f,
                    label = "Timecode",
                    type = SelectionType.TIMESTAMP_DATE
                )
            ),
            defaultInpaintMode = "CONTENT_AWARE",
            hasScratches = true,
            sceneType = SceneType.VINTAGE_ARCHIVE
        )
    )

    /**
     * Generates a realistic high-definition video frame for the sample scene at the given playback position.
     */
    fun generateFrame(scene: SceneType, progress: Float, width: Int = 960, height: Int = 540): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        when (scene) {
            SceneType.TECH_VLOG -> renderTechVlogScene(canvas, width, height, progress)
            SceneType.DRONE_SUNSET -> renderDroneSunsetScene(canvas, width, height, progress)
            SceneType.STUDIO_INTERVIEW -> renderStudioInterviewScene(canvas, width, height, progress)
            SceneType.VINTAGE_ARCHIVE -> renderVintageArchiveScene(canvas, width, height, progress)
        }

        return bitmap
    }

    private fun renderTechVlogScene(canvas: Canvas, w: Int, h: Int, progress: Float) {
        // High-tech modern dark studio background gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(Color.rgb(18, 24, 38), Color.rgb(28, 38, 64), Color.rgb(12, 16, 28)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Studio lighting flare
        val flarePaint = Paint().apply {
            color = Color.argb(60, 0, 229, 255)
            maskFilter = null
        }
        val flareX = w * (0.3f + progress * 0.4f)
        canvas.drawCircle(flareX, h * 0.4f, w * 0.35f, flarePaint)

        // Tech desk / laptop silhouette
        val deskPaint = Paint().apply { color = Color.rgb(35, 48, 75) }
        canvas.drawRect(0f, h * 0.65f, w.toFloat(), h.toFloat(), deskPaint)

        // Laptop glow
        val lapPaint = Paint().apply { color = Color.rgb(50, 70, 110) }
        canvas.drawRoundRect(w * 0.32f, h * 0.45f, w * 0.68f, h * 0.72f, 16f, 16f, lapPaint)

        // Watermark: @CreatorMark_2024 at top-right
        drawWatermarkBadge(
            canvas = canvas,
            x = (w * 0.68f).toInt(),
            y = (h * 0.08f).toInt(),
            width = (w * 0.28f).toInt(),
            height = (h * 0.12f).toInt(),
            text = "© @CreatorMark_2024",
            subtext = "4K 60FPS STUDIO CLIP",
            isCorner = true
        )
    }

    private fun renderDroneSunsetScene(canvas: Canvas, w: Int, h: Int, progress: Float) {
        // Sunset sky gradient
        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, h * 0.65f,
                intArrayOf(Color.rgb(240, 98, 146), Color.rgb(255, 167, 38), Color.rgb(255, 238, 88)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h * 0.65f, skyPaint)

        // Sun
        val sunPaint = Paint().apply { color = Color.rgb(255, 248, 225) }
        canvas.drawCircle(w * 0.5f, h * 0.45f, w * 0.08f, sunPaint)

        // Ocean ocean waves gradient
        val seaPaint = Paint().apply {
            shader = LinearGradient(
                0f, h * 0.65f, 0f, h.toFloat(),
                intArrayOf(Color.rgb(24, 76, 120), Color.rgb(10, 35, 60)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, h * 0.65f, w.toFloat(), h.toFloat(), seaPaint)

        // Drone timestamp at bottom-left: 2024-06-12 18:42:09
        val seconds = (progress * 15).toInt()
        val timeString = "2024-06-12 18:42:${String.format("%02d", seconds)} GMT"
        drawTimestampOverlay(
            canvas = canvas,
            x = (w * 0.04f).toInt(),
            y = (h * 0.82f).toInt(),
            width = (w * 0.38f).toInt(),
            height = (h * 0.10f).toInt(),
            text = timeString,
            altText = "LAT: 34.0194° N LON: 118.4912° W"
        )
    }

    private fun renderStudioInterviewScene(canvas: Canvas, w: Int, h: Int, progress: Float) {
        // Deep interior bokeh background
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(Color.rgb(30, 25, 45), Color.rgb(55, 35, 75), Color.rgb(20, 15, 30)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Bokeh orbs
        val orbPaint = Paint().apply { color = Color.argb(45, 255, 200, 100) }
        canvas.drawCircle(w * 0.2f, h * 0.3f, 70f, orbPaint)
        canvas.drawCircle(w * 0.75f, h * 0.25f, 90f, orbPaint)

        // Interview speaker silhouette
        val headPaint = Paint().apply { color = Color.rgb(40, 30, 50) }
        canvas.drawCircle(w * 0.6f, h * 0.45f, w * 0.12f, headPaint)
        canvas.drawRect(w * 0.45f, h * 0.55f, w * 0.75f, h.toFloat(), headPaint)

        // Lower third banner overlay
        drawLowerThirdBanner(
            canvas = canvas,
            x = (w * 0.05f).toInt(),
            y = (h * 0.70f).toInt(),
            width = (w * 0.45f).toInt(),
            height = (h * 0.16f).toInt(),
            title = "MARK FOREMAN",
            role = "CREATIVE DIRECTOR & EDITOR",
            channelTag = "@MARK_FILMS_ORIGINAL"
        )
    }

    private fun renderVintageArchiveScene(canvas: Canvas, w: Int, h: Int, progress: Float) {
        // Vintage sepia tone background
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(Color.rgb(180, 155, 120), Color.rgb(140, 115, 80), Color.rgb(90, 70, 50)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Old street building silhouettes
        val bldgPaint = Paint().apply { color = Color.rgb(75, 55, 40) }
        canvas.drawRect(w * 0.1f, h * 0.35f, w * 0.35f, h.toFloat(), bldgPaint)
        canvas.drawRect(w * 0.4f, h * 0.25f, w * 0.65f, h.toFloat(), bldgPaint)
        canvas.drawRect(w * 0.7f, h * 0.4f, w * 0.95f, h.toFloat(), bldgPaint)

        // Film scratches (white and dark streaks for restoration testing)
        val scratchPaint = Paint().apply {
            color = Color.argb(190, 245, 240, 230)
            strokeWidth = 2f
        }
        val xOffset = ((progress * 1000).toInt() % 70)
        canvas.drawLine(w * 0.25f + xOffset, 0f, w * 0.25f + xOffset, h.toFloat(), scratchPaint)
        canvas.drawLine(w * 0.65f - (xOffset / 2), 0f, w * 0.65f - (xOffset / 2), h.toFloat(), scratchPaint)

        // Dust spots
        val dustPaint = Paint().apply { color = Color.argb(160, 40, 30, 20) }
        canvas.drawCircle(w * 0.45f + (xOffset * 2), h * 0.3f, 4f, dustPaint)
        canvas.drawCircle(w * 0.75f - xOffset, h * 0.6f, 3f, dustPaint)

        // Timecode stamp: 01:24:19:08
        drawTimecodeBox(
            canvas = canvas,
            x = (w * 0.32f).toInt(),
            y = (h * 0.88f).toInt(),
            width = (w * 0.36f).toInt(),
            height = (h * 0.08f).toInt(),
            timecode = "TC 01:24:19:${String.format("%02d", ((progress * 30).toInt() % 30))}"
        )
    }

    private fun drawWatermarkBadge(
        canvas: Canvas,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        text: String,
        subtext: String,
        isCorner: Boolean
    ) {
        // Semi-transparent pill backing typical of creator logos
        val bgPaint = Paint().apply {
            color = Color.argb(160, 15, 23, 42)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.argb(120, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val rectF = RectF(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat())
        canvas.drawRoundRect(rectF, 12f, 12f, bgPaint)
        canvas.drawRoundRect(rectF, 12f, 12f, borderPaint)

        // Text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = height * 0.38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(210, 0, 229, 255)
            textSize = height * 0.24f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        canvas.drawText(text, x + width * 0.08f, y + height * 0.48f, textPaint)
        canvas.drawText(subtext, x + width * 0.08f, y + height * 0.82f, subPaint)
    }

    private fun drawTimestampOverlay(
        canvas: Canvas,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        text: String,
        altText: String
    ) {
        val bgPaint = Paint().apply {
            color = Color.argb(150, 0, 0, 0)
        }
        val rectF = RectF(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat())
        canvas.drawRoundRect(rectF, 8f, 8f, bgPaint)

        val fontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 220, 40) // Amber digital cam color
            textSize = height * 0.42f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val altPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = height * 0.28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        canvas.drawText(text, x + width * 0.05f, y + height * 0.48f, fontPaint)
        canvas.drawText(altText, x + width * 0.05f, y + height * 0.85f, altPaint)
    }

    private fun drawLowerThirdBanner(
        canvas: Canvas,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        title: String,
        role: String,
        channelTag: String
    ) {
        // Red accent bar
        val barPaint = Paint().apply { color = Color.rgb(244, 63, 94) }
        canvas.drawRect(x.toFloat(), y.toFloat(), x + 12f, (y + height).toFloat(), barPaint)

        // Dark box
        val bgPaint = Paint().apply { color = Color.argb(200, 15, 23, 42) }
        canvas.drawRect(x + 12f, y.toFloat(), (x + width).toFloat(), (y + height).toFloat(), bgPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = height * 0.32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val rolePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = height * 0.22f
        }
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 229, 255)
            textSize = height * 0.20f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        canvas.drawText(title, x + 24f, y + height * 0.35f, titlePaint)
        canvas.drawText(role, x + 24f, y + height * 0.62f, rolePaint)
        canvas.drawText(channelTag, x + 24f, y + height * 0.88f, tagPaint)
    }

    private fun drawTimecodeBox(
        canvas: Canvas,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        timecode: String
    ) {
        val bgPaint = Paint().apply { color = Color.argb(160, 20, 20, 20) }
        canvas.drawRoundRect(
            RectF(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat()),
            6f, 6f, bgPaint
        )
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 255, 128)
            textSize = height * 0.65f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        canvas.drawText(timecode, x + width * 0.08f, y + height * 0.72f, textPaint)
    }
}
