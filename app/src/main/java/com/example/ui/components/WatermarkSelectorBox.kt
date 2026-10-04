package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NormalizedRect
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RoseError
import com.example.ui.theme.SelectionBoxBorder
import com.example.ui.theme.SelectionBoxFill
import com.example.ui.theme.StudioBackground
import kotlin.math.roundToInt

@Composable
fun WatermarkSelectorOverlay(
    modifier: Modifier = Modifier,
    boxes: List<NormalizedRect>,
    selectedBoxId: String?,
    onSelectBox: (String) -> Unit,
    onUpdateBox: (NormalizedRect) -> Unit,
    onDeleteBox: (String) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().testTag("watermark_overlay_container")) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()

        if (containerWidthPx <= 0 || containerHeightPx <= 0) return@BoxWithConstraints

        boxes.forEach { rect ->
            val isSelected = rect.id == selectedBoxId
            val boxLeft = (rect.x * containerWidthPx).roundToInt()
            val boxTop = (rect.y * containerHeightPx).roundToInt()
            val boxWidth = (rect.width * containerWidthPx).roundToInt().coerceAtLeast(40)
            val boxHeight = (rect.height * containerHeightPx).roundToInt().coerceAtLeast(30)

            Box(
                modifier = Modifier
                    .offset { IntOffset(boxLeft, boxTop) }
                    .size(
                        width = (boxWidth / (containerWidthPx / maxWidth.value)).dp,
                        height = (boxHeight / (containerHeightPx / maxHeight.value)).dp
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) SelectionBoxFill else Color(0x1800E5FF))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) CyanAccent else SelectionBoxBorder.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .pointerInput(rect.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onSelectBox(rect.id)
                            val deltaXNorm = dragAmount.x / containerWidthPx
                            val deltaYNorm = dragAmount.y / containerHeightPx
                            val newX = (rect.x + deltaXNorm).coerceIn(0f, 1f - rect.width)
                            val newY = (rect.y + deltaYNorm).coerceIn(0f, 1f - rect.height)
                            onUpdateBox(rect.copy(x = newX, y = newY))
                        }
                    }
                    .testTag("selection_box_${rect.id}")
            ) {
                // Top label and delete button
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(StudioBackground.copy(alpha = 0.85f), RoundedCornerShape(bottomEnd = 6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rect.label,
                        color = CyanAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                        IconButton(
                            onClick = { onDeleteBox(rect.id) },
                            modifier = Modifier.size(18.dp).padding(start = 2.dp).testTag("delete_box_${rect.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete Selection",
                                tint = RoseError,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Center Move Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                        .background(CyanAccent.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Move",
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Bottom-right Resize Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .background(CyanAccent, RoundedCornerShape(topStart = 6.dp))
                        .pointerInput(rect.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onSelectBox(rect.id)
                                val deltaWNorm = dragAmount.x / containerWidthPx
                                val deltaHNorm = dragAmount.y / containerHeightPx
                                val newW = (rect.width + deltaWNorm).coerceIn(0.04f, 1f - rect.x)
                                val newH = (rect.height + deltaHNorm).coerceIn(0.03f, 1f - rect.y)
                                onUpdateBox(rect.copy(width = newW, height = newH))
                            }
                        }
                        .testTag("resize_handle_${rect.id}")
                )
            }
        }
    }
}
