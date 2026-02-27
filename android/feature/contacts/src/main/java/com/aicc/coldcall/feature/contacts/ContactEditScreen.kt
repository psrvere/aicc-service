package com.aicc.coldcall.feature.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicc.coldcall.core.model.DealStage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: ContactEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Contact" else "New Contact") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
            )

            OutlinedTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChanged,
                label = { Text("Phone *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.phoneError != null,
                supportingText = state.phoneError?.let { { Text(it) } },
            )

            OutlinedTextField(
                value = state.business,
                onValueChange = viewModel::onBusinessChanged,
                label = { Text("Business") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.city,
                onValueChange = viewModel::onCityChanged,
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = state.industry,
                onValueChange = viewModel::onIndustryChanged,
                label = { Text("Industry") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text("Deal Stage")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DealStage.entries.forEach { stage ->
                    FilterChip(
                        selected = state.dealStage == stage,
                        onClick = { viewModel.onDealStageChanged(stage) },
                        label = { Text(stage.name) },
                    )
                }
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChanged,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            OutlinedTextField(
                value = state.nextFollowUp,
                onValueChange = viewModel::onNextFollowUpChanged,
                label = { Text("Next Follow-up (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            ) {
                Text(if (state.isEditMode) "Update" else "Create")
            }
        }
    }
}
