package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VioletAccent
import kotlin.math.roundToInt

@Composable
fun BeforeAfterSlider(
    beforeBitmap: Bitmap,
    afterBitmap: Bitmap,
    modifier: Modifier = Modifier,
    sliderPosition: Float = 0.5f,
    onPositionChanged: (Float) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .testTag("before_after_slider_container")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val splitX = widthPx * sliderPosition

        // 1. Base layer: Original (Before)
        Image(
            bitmap = beforeBitmap.asImageBitmap(),
            contentDescription = "Original with Watermark",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Clipped layer: Inpainted (After) on the right side
        val afterWidthFraction = (1f - sliderPosition).coerceIn(0f, 1f)
        val totalWidthDp = maxWidth
        if (afterWidthFraction > 0.001f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .width((totalWidthDp * afterWidthFraction))
                    .clipToBounds()
            ) {
                // Align image inside container to maintain aspect alignment
                Image(
                    bitmap = afterBitmap.asImageBitmap(),
                    contentDescription = "Cleaned After Video Frame",
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(totalWidthDp)
                        .align(Alignment.TopEnd),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // 3. Vertical Divider Line & Touch Handle
        Box(
            modifier = Modifier
                .offset { IntOffset(splitX.roundToInt() - 2, 0) }
                .fillMaxHeight()
                .width(4.dp)
                .background(CyanAccent)
        )

        // Handle Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(splitX.roundToInt() - 20, (constraints.maxHeight / 2) - 20) }
                .size(40.dp)
                .background(StudioBackground, CircleShape)
                .border(2.dp, CyanAccent, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newPos = ((splitX + dragAmount.x) / widthPx).coerceIn(0.05f, 0.95f)
                        onPositionChanged(newPos)
                    }
                }
                .testTag("before_after_thumb"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = "Slide to Compare",
                tint = CyanAccent,
                modifier = Modifier.size(22.dp)
            )
        }

        // Labels
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(StudioBackground.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BEFORE (WITH LOGO)",
                color = Color(0xFFFFB300),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(StudioBackground.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AFTER (CLEANED)",
                color = CyanAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
