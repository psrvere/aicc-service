package com.aicc.coldcall.feature.calling

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aicc.coldcall.core.ui.DispositionPicker
import com.aicc.coldcall.core.ui.LoadingOverlay

@Composable
fun PostCallScreen(
    onSaved: () -> Unit,
    viewModel: PostCallViewModel,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved && state.aiPipelineStatus == null) {
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Log Call",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = state.contactName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "How did the call go?",
            style = MaterialTheme.typography.titleSmall,
        )

        DispositionPicker(
            selected = state.disposition,
            onSelect = { viewModel.selectDisposition(it) },
        )

        state.followUpDate?.let { date ->
            Text(
                text = "Follow-up: $date",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        OutlinedTextField(
            value = state.summary,
            onValueChange = { viewModel.updateSummary(it) },
            label = { Text("Call Summary (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
        )

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        // AI Pipeline section
        val aiStatus = state.aiPipelineStatus
        when {
            aiStatus == AiPipelineStatus.COMPLETED && state.aiSummary != null -> {
                AISummaryCard(
                    summary = state.aiSummary!!,
                    onSummaryEdit = { viewModel.updateAiSummary(it) },
                    onConfirm = onSaved,
                    onRegenerate = { viewModel.regenerateSummary() },
                )
            }
            aiStatus == AiPipelineStatus.ERROR -> {
                Text(
                    text = state.aiError ?: "AI pipeline failed",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            aiStatus != null -> {
                AiPipelineProgress(status = aiStatus)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.saveCall() },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.disposition != null && !state.isSaving,
        ) {
            Text(if (state.isSaving) "Saving..." else "Save")
        }
    }

    if (state.isSaving) {
        LoadingOverlay()
    }
}
