package com.aicc.coldcall.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.ui.DealStageBadge
import com.aicc.coldcall.core.ui.DealStageColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        if (state.error != null && state.stats == com.aicc.coldcall.core.model.DashboardStats()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    ) {
                        StatChip(label = "Calls Today", value = state.stats.callsToday.toString(), modifier = Modifier.weight(1f))
                        StatChip(label = "Connected", value = state.stats.connectedToday.toString(), modifier = Modifier.weight(1f))
                        StatChip(label = "Conv. Rate", value = "${(state.stats.conversionRate * 100).toInt()}%", modifier = Modifier.weight(1f))
                        StatChip(label = "Streak", value = "${state.stats.streak}d", modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Text(
                        text = "Pipeline",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                }

                if (state.stats.pipeline.isEmpty()) {
                    item {
                        Text(
                            text = "No pipeline data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    val maxCount = state.stats.pipeline.values.maxOrNull() ?: 1

                    items(state.stats.pipeline.entries.toList()) { (stage, count) ->
                        PipelineRow(stage = stage, count = count, maxCount = maxCount)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PipelineRow(stage: DealStage, count: Int, maxCount: Int) {
    val color = DealStageColors.forStage(stage)
    val fraction = if (maxCount > 0) count.toFloat() / maxCount else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DealStageBadge(
            stage = stage,
            modifier = Modifier.weight(0.3f),
        )

        Box(
            modifier = Modifier
                .weight(0.55f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.2f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color),
            )
        }

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.15f),
        )
    }
}
