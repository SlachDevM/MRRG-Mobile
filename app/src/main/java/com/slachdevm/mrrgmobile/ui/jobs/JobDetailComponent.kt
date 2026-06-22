package com.slachdevm.mrrgmobile.ui.jobs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slachdevm.mrrgmobile.R
import com.slachdevm.mrrgmobile.domain.model.Job
import com.slachdevm.mrrgmobile.domain.model.JobStatus
import com.slachdevm.mrrgmobile.ui.components.StatusChip
import com.slachdevm.mrrgmobile.ui.components.toJobTypeLabel
import com.slachdevm.mrrgmobile.ui.components.toPriorityLabel

@Composable
fun JobInfoSection(
    job: Job,
    onStartNavigation: (String) -> Unit,
    onCallClient: (String) -> Unit
) {
    Text(text = job.clientName, style = MaterialTheme.typography.headlineMedium)
    Text(text = job.clientAddress, style = MaterialTheme.typography.bodyLarge)
    TextButton(onClick = { onStartNavigation(job.clientAddress) }) {
        Text(stringResource(R.string.action_start_navigation))
    }
    Text(
        text = job.clientPhone,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary
    )
    TextButton(onClick = { onCallClient(job.clientPhone) }) {
        Text(stringResource(R.string.action_call_client))
    }

    Spacer(modifier = Modifier.height(16.dp))

    DetailItem(label = stringResource(R.string.label_type), value = job.jobTypes.toJobTypeLabel())
    StatusChip(status = job.status)
    DetailItem(label = stringResource(R.string.label_priority), value = job.priorityLevel.toPriorityLabel())
    DetailItem(label = stringResource(R.string.label_details), value = job.details ?: stringResource(R.string.no_extra_details))

    if (!job.assignedWorkerNames.isNullOrBlank()) {
        DetailItem(label = stringResource(R.string.label_assigned_workers), value = job.assignedWorkerNames)
    } else if (!job.assignedWorkers.isNullOrBlank()) {
        DetailItem(label = stringResource(R.string.label_assigned_workers), value = stringResource(R.string.assigned_workers_unavailable))
    }
}

@Composable
fun NotesSection(
    notesText: String,
    isUpdating: Boolean,
    onNotesChange: (String) -> Unit,
    onSaveNotes: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.label_notes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = notesText,
            onValueChange = onNotesChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text(stringResource(R.string.placeholder_add_notes)) }
        )
        Button(
            onClick = onSaveNotes,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp),
            enabled = !isUpdating
        ) {
            Text(stringResource(R.string.action_save_notes))
        }
    }
}

@Composable
fun CompleteJobButton(
    status: JobStatus,
    isUpdating: Boolean,
    error: String?,
    onCompleteJob: () -> Unit
) {
    if (status != JobStatus.IN_PROGRESS && status != JobStatus.READY_FOR_CONFIRMATION) {
        return
    }

    val completeEnabled = status != JobStatus.READY_FOR_CONFIRMATION && !isUpdating

    Button(
        onClick = onCompleteJob,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        enabled = completeEnabled
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            if (status == JobStatus.READY_FOR_CONFIRMATION)
                stringResource(R.string.status_waiting_confirmation)
            else
                stringResource(R.string.action_complete)
        )
    }

    if (error != null) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
