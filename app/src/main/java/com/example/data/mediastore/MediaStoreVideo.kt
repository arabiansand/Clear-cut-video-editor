package com.example.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class LocalMediaVideo(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAddedSec: Long,
    val width: Int,
    val height: Int,
    val thumbnailBitmap: Bitmap? = null
) {
    val formattedDuration: String
        get() {
            val totalSec = durationMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            return String.format(Locale.US, "%02d:%02d", min, sec)
        }

    val formattedSize: String
        get() {
            val mb = sizeBytes.toDouble() / (1024 * 1024)
            return if (mb >= 1000) {
                String.format(Locale.US, "%.1f GB", mb / 1024.0)
            } else {
                String.format(Locale.US, "%.1f MB", mb)
            }
        }

    val resolutionLabel: String
        get() {
            return when {
                width >= 3840 || height >= 2160 -> "4K UHD"
                width >= 1920 || height >= 1080 -> "1080p Full HD"
                width >= 1280 || height >= 720 -> "720p HD"
                width > 0 && height > 0 -> "${width}x${height}"
                else -> "Standard Video"
            }
        }
}

object MediaStoreVideoHelper {

    /**
     * Queries MediaStore.Video.Media.EXTERNAL_CONTENT_URI to retrieve all local videos on the device.
     */
    suspend fun queryLocalVideos(context: Context): List<LocalMediaVideo> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<LocalMediaVideo>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val widthCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Video_$id"
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val dateAdded = cursor.getLong(dateCol)
                    val width = if (widthCol != -1) cursor.getInt(widthCol) else 1920
                    val height = if (heightCol != -1) cursor.getInt(heightCol) else 1080

                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                    // Load thumbnail safely
                    var thumb: Bitmap? = null
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            thumb = context.contentResolver.loadThumbnail(contentUri, Size(320, 180), null)
                        } else {
                            val retriever = MediaMetadataRetriever()
                            retriever.setDataSource(context, contentUri)
                            thumb = retriever.getFrameAtTime(1000000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                            retriever.release()
                        }
                    } catch (_: Exception) {}

                    videoList.add(
                        LocalMediaVideo(
                            id = id,
                            uri = contentUri,
                            displayName = name,
                            durationMs = duration,
                            sizeBytes = size,
                            dateAddedSec = dateAdded,
                            width = width,
                            height = height,
                            thumbnailBitmap = thumb
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Handle security or cursor query exceptions safely
        }

        videoList
    }
}
