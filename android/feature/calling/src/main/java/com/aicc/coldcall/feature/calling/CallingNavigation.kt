package com.aicc.coldcall.feature.calling

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.time.LocalDate

const val PRE_CALL_ROUTE = "precall/{contactId}/{contactName}"
const val POST_CALL_ROUTE = "postcall/{contactId}/{contactName}?recordingFilePath={recordingFilePath}"

fun NavGraphBuilder.preCallScreen(
    onCallNow: (phone: String) -> Unit,
    onNavigateToPostCall: (contactId: String, contactName: String) -> Unit,
) {
    composable(
        route = PRE_CALL_ROUTE,
        arguments = listOf(
            navArgument("contactId") { type = NavType.StringType },
            navArgument("contactName") { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val contactId = backStackEntry.arguments?.getString("contactId") ?: return@composable
        val contactName = backStackEntry.arguments?.getString("contactName") ?: return@composable

        val viewModel = hiltViewModel<PreCallViewModel, PreCallViewModel.Factory> { factory ->
            factory.create(contactId)
        }

        PreCallScreen(
            onCallNow = onCallNow,
            onNavigateToPostCall = { onNavigateToPostCall(contactId, contactName) },
            viewModel = viewModel,
        )
    }
}

fun NavGraphBuilder.postCallScreen(
    onSaved: () -> Unit,
) {
    composable(
        route = POST_CALL_ROUTE,
        arguments = listOf(
            navArgument("contactId") { type = NavType.StringType },
            navArgument("contactName") { type = NavType.StringType },
            navArgument("recordingFilePath") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { backStackEntry ->
        val contactId = backStackEntry.arguments?.getString("contactId") ?: return@composable
        val contactName = backStackEntry.arguments?.getString("contactName") ?: return@composable
        val recordingFilePath = backStackEntry.arguments?.getString("recordingFilePath")?.let {
            URLDecoder.decode(it, "UTF-8")
        }

        val viewModel = hiltViewModel<PostCallViewModel, PostCallViewModel.Factory> { factory ->
            factory.create(contactId, contactName, { LocalDate.now() }, recordingFilePath)
        }

        PostCallScreen(
            onSaved = onSaved,
            viewModel = viewModel,
        )
    }
}
