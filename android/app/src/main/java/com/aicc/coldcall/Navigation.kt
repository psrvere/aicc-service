package com.aicc.coldcall

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.aicc.coldcall.feature.callplan.CALL_PLAN_ROUTE
import com.aicc.coldcall.feature.callplan.callPlanScreen
import com.aicc.coldcall.feature.calling.postCallScreen
import com.aicc.coldcall.feature.calling.preCallScreen
import com.aicc.coldcall.feature.settings.SettingsScreen
import androidx.navigation.compose.composable
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = CALL_PLAN_ROUTE,
        modifier = modifier,
    ) {
        callPlanScreen(
            onContactClick = { id, name, phone ->
                val encodedName = Uri.encode(name)
                val encodedPhone = Uri.encode(phone)
                navController.navigate("precall/$id/$encodedName/$encodedPhone")
            },
        )

        preCallScreen(
            onCallNow = { phone ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                context.startActivity(intent)
            },
            onNavigateToPostCall = { contactId, contactName ->
                val encodedName = Uri.encode(contactName)
                navController.navigate("postcall/$contactId/$encodedName")
            },
        )

        postCallScreen(
            onSaved = {
                navController.popBackStack(CALL_PLAN_ROUTE, inclusive = false)
            },
        )

        composable("contacts") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Contacts — coming soon")
            }
        }

        composable("dashboard") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Dashboard — coming soon")
            }
        }

        composable("settings") {
            SettingsScreen()
        }
    }
}
