package com.aicc.coldcall.feature.callplan

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val CALL_PLAN_ROUTE = "callplan"

fun NavGraphBuilder.callPlanScreen(
    onContactClick: (id: String, name: String, phone: String) -> Unit,
) {
    composable(CALL_PLAN_ROUTE) {
        CallPlanScreen(onContactClick = onContactClick)
    }
}
