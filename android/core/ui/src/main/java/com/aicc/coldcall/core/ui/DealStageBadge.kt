package com.aicc.coldcall.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aicc.coldcall.core.model.DealStage

private val dealStageLabels = mapOf(
    DealStage.New to "New",
    DealStage.Contacted to "Contacted",
    DealStage.Qualified to "Qualified",
    DealStage.Proposal to "Proposal",
    DealStage.Negotiation to "Negotiation",
    DealStage.Won to "Won",
    DealStage.Lost to "Lost",
    DealStage.NotInterested to "Not Interested",
)

@Composable
fun DealStageBadge(stage: DealStage, modifier: Modifier = Modifier) {
    val color = DealStageColors.forStage(stage)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f),
        contentColor = color,
    ) {
        Text(
            text = dealStageLabels[stage] ?: stage.name,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
