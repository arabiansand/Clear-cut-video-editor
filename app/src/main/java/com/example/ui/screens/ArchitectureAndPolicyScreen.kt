package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@Composable
fun ArchitectureAndPolicyScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportFeedbackSent by remember { mutableStateOf(false) }

    val tabs = listOf("Pipeline & Architecture", "Compliance & Ethics", "Fair Use & TOS")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground)
            .testTag("architecture_policy_screen")
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp).testTag("button_back_policy")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Mark. F. Studio Docs",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Architecture, Inpainting AI & Legal Compliance",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = { showReportDialog = true },
                modifier = Modifier.size(36.dp).testTag("button_report_misuse")
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Report Misuse",
                    tint = RoseError
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = StudioSurface,
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = CyanAccent
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) CyanAccent else TextSecondary
                        )
                    }
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTabIndex) {
                0 -> PipelineArchitectureTab()
                1 -> ComplianceChecklistTab(onOpenReport = { showReportDialog = true })
                2 -> FairUseAndTermsTab()
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showReportDialog) {
        ReportMisuseDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = {
                showReportDialog = false
                reportFeedbackSent = true
            }
        )
    }

    if (reportFeedbackSent) {
        AlertDialog(
            onDismissRequest = { reportFeedbackSent = false },
            confirmButton = {
                TextButton(onClick = { reportFeedbackSent = false }) {
                    Text("OK", color = CyanAccent)
                }
            },
            title = { Text("Report Received", color = TextPrimary) },
            text = {
                Text(
                    "Thank you for helping keep Mark. F. compliant. Our trust & safety team reviews reported incidents to prevent tool abuse.",
                    color = TextSecondary
                )
            },
            containerColor = StudioSurface
        )
    }
}

@Composable
private fun PipelineArchitectureTab() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Architecture overview banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Inpainting Pipeline Design",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mark. F. combines localized spatial boundary gradient propagation with temporal continuity filters to eliminate watermarks and scratches without noticeable artifact smearing.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Interactive Pipeline Stages Diagram
        Text(
            text = "STEP-BY-STEP INPAINTING STAGES",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        val stages = listOf(
            Triple("01", "Video Demuxing & Frame Extraction", "MediaMetadataRetriever / MediaCodec extracts synchronized frame buffers and isolates audio tracks for untouched pass-through."),
            Triple("02", "Mask Formulation & Dilation", "User-selected normalized bounding boxes or scratch vectors are expanded by 3-5px to eliminate edge aliasing."),
            Triple("03", "Boundary Gradient Sampling", "Samples perimeter pixels along cardinal axes to calculate Poisson gradient fields and structure vectors."),
            Triple("04", "Spatial Synthesis (Telea / Content-Aware)", "Synthesizes textures from surrounding context into the masked region with distance-weighted Laplacian interpolation."),
            Triple("05", "Temporal Smoothing Filter", "Multi-frame median filter prevents inter-frame luminance flicker across moving background scenes."),
            Triple("06", "Audio Re-Muxing & Export", "MediaMuxer encapsulates processed frames with original audio stream at original resolution and chosen FPS.")
        )

        stages.forEach { (step, name, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioSurface)
                    .border(1.dp, StudioCardBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(CyanAccent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = step, color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = desc, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }

        // AI Model Comparison
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioSurfaceVariant)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "Supported Inpainting Models",
                    color = VioletAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• AI Content-Aware Fill: On-device texture synthesis (Instant 60fps preview)\n" +
                            "• Telea Fast Marching: Smooth Laplacian flow for text & timestamp stamps\n" +
                            "• ProPainter / E2FGVI compatibility: High-capacity recurrent flow matching for complex motion\n" +
                            "• LaMa (Large Mask Inpainting): Fast Fourier convolution architecture for large bounding boxes",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun ComplianceChecklistTab(onOpenReport: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(EmeraldSuccess.copy(alpha = 0.1f))
                .border(1.dp, EmeraldSuccess.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Play Store & App Store Compliance Verified",
                        color = EmeraldSuccess,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configured exclusively for creator self-service restoration.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        val checklist = listOf(
            Pair("Creator Ownership Certification Gate", "Requires explicit affirmative consent checkbox on every export certifying ownership."),
            Pair("Zero-Permission Android Photo Picker", "Never requests broad READ_EXTERNAL_STORAGE. Uses system photo picker directly."),
            Pair("No Automated Scraping of Platforms", "Does not contain auto-detectors for TikTok, Snapchat, Instagram, or YouTube stamps."),
            Pair("No Dynamic Code Loading (DCL)", "All inpainting engines run native compiled code without downloading remote .dex or .so binaries."),
            Pair("Misuse Reporting Mechanism", "Embedded flag system allowing users and rights holders to report unauthorized uses."),
            Pair("On-Device Processing", "Personal footage remains entirely private on the user's device unless cloud processing is selected.")
        )

        checklist.forEach { (title, detail) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioSurface)
                    .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = detail, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onOpenReport,
            colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant),
            modifier = Modifier.fillMaxWidth().testTag("button_open_report_dialog")
        ) {
            Icon(imageVector = Icons.Default.ReportProblem, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Report an Infringement or Safety Concern", color = AmberWarning, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FairUseAndTermsTab() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Creator Terms of Service & Fair Use", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "1. Permitted Uses:\n" +
                            "Mark. F. is designed for content creators, videographers, and archivists to:\n" +
                            "• Remove their own outdated handles, handles with changed usernames, or legacy logos.\n" +
                            "• Erase automated camera date/time stamps, GPS coordinates, and camera UI artifacts from personal raw takes.\n" +
                            "• Clean film scratches, sensor dust particles, and minor lens clutter from home movies and personal archive film.\n\n" +
                            "2. Prohibited Uses:\n" +
                            "You agree NOT to use Mark. F. to:\n" +
                            "• Strip copyright attribution or watermarks from third-party artists or platforms without written license.\n" +
                            "• Re-upload or syndicate unauthorized content to video-sharing networks.\n" +
                            "• Remove digital watermarks intended for provenance and child safety.\n\n" +
                            "3. Privacy Policy:\n" +
                            "All video frames are analyzed locally in device memory. Video buffers are cleared upon session close. No personal video data is uploaded to remote servers without user request.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun ReportMisuseDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Report Misuse or Copyright Issue", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "If you believe this application is being misused to infringe your rights, describe the concern below.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    label = { Text("Your Email") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = StudioCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_report_email")
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Description of Issue") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = StudioCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("input_report_desc")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = RoseError)
            ) {
                Text("Submit Report", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = StudioSurface
    )
}
