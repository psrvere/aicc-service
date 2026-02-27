package com.aicc.coldcall.feature.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.ui.ContactCard
import com.aicc.coldcall.core.ui.SearchBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactListScreen(
    onContactClick: (String) -> Unit,
    onAddContact: () -> Unit,
    viewModel: ContactListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Contacts") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddContact) {
                Icon(Icons.Default.Add, contentDescription = "Add contact")
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = "Search contacts...",
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DealStage.entries.forEach { stage ->
                        FilterChip(
                            selected = state.dealStageFilter == stage,
                            onClick = {
                                viewModel.onDealStageFilterChanged(
                                    if (state.dealStageFilter == stage) null else stage
                                )
                            },
                            label = { Text(stage.name) },
                        )
                    }
                }

                if (state.contacts.isEmpty() && !state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (state.searchQuery.isNotEmpty() || state.dealStageFilter != null)
                                "No contacts match your filters"
                            else
                                "No contacts yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.contacts, key = { it.id }) { contact ->
                            ContactCard(
                                contact = contact,
                                onClick = { onContactClick(contact.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
