package com.aicc.coldcall.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aicc.coldcall.core.model.Disposition

private fun Disposition.label(): String = when (this) {
    Disposition.Connected -> "Connected"
    Disposition.NoAnswer -> "No Answer"
    Disposition.Voicemail -> "Voicemail"
    Disposition.Callback -> "Callback"
    Disposition.NotInterested -> "Not Interested"
    Disposition.WrongNumber -> "Wrong Number"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DispositionPicker(
    selected: Disposition?,
    onSelect: (Disposition) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Disposition.entries.forEach { disposition ->
            FilterChip(
                selected = disposition == selected,
                onClick = { onSelect(disposition) },
                label = { Text(disposition.label()) },
            )
        }
    }
}
