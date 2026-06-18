package com.slachdevm.mrrgmobile.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.slachdevm.mrrgmobile.domain.model.JobStatus
import androidx.compose.runtime.getValue

@Composable
fun StatusChip(status: JobStatus) {
    val (label, color) = when (status) {
        JobStatus.PENDING -> "🟡 Pending" to Color(0xFFFFC107)
        JobStatus.SCHEDULED -> "🔵 Scheduled" to Color(0xFF2196F3)
        JobStatus.IN_PROGRESS -> "🟣 In progress" to Color(0xFF9C27B0)
        JobStatus.READY_FOR_CONFIRMATION -> "🟠 Waiting validation" to Color(0xFFFF9800)
        JobStatus.DONE -> "🟢 Completed" to Color(0xFF4CAF50)
        JobStatus.ARCHIVED -> "⚫ Archived" to Color(0xFF757575)
        JobStatus.TO_BE_FIXED -> "🔴 Callback" to Color(0xFFF44336)
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = color.copy(alpha = 0.15f),
        animationSpec = tween(250),
        label = "StatusChipContainerColorAnimation"
    )

    val animatedLabelColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(250),
        label = "StatusChipLabelColorAnimation"
    )

    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = animatedContainerColor,
            labelColor = animatedLabelColor
        )
    )
}