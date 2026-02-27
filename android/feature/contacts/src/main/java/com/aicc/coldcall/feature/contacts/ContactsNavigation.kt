package com.aicc.coldcall.feature.contacts

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation

const val CONTACTS_GRAPH_ROUTE = "contacts_graph"
const val CONTACT_LIST_ROUTE = "contacts"
const val CONTACT_DETAIL_ROUTE = "contacts/{contactId}"
const val CONTACT_EDIT_ROUTE = "contacts/{contactId}/edit"
const val CONTACT_CREATE_ROUTE = "contacts/new"

fun NavGraphBuilder.contactsGraph(navController: NavController) {
    navigation(startDestination = CONTACT_LIST_ROUTE, route = CONTACTS_GRAPH_ROUTE) {
        composable(CONTACT_LIST_ROUTE) {
            ContactListScreen(
                onContactClick = { id ->
                    navController.navigate("contacts/$id")
                },
                onAddContact = {
                    navController.navigate(CONTACT_CREATE_ROUTE)
                },
            )
        }

        composable(CONTACT_CREATE_ROUTE) {
            ContactEditScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = CONTACT_DETAIL_ROUTE,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
        ) {
            ContactDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditContact = { id ->
                    navController.navigate("contacts/$id/edit")
                },
            )
        }

        composable(
            route = CONTACT_EDIT_ROUTE,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
        ) {
            ContactEditScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
