package com.example.gymapp.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.R
import com.example.gymapp.ui.screens.AddEditMemberScreen
import com.example.gymapp.ui.screens.DueExpiredScreen
import com.example.gymapp.ui.screens.MemberListScreen
import com.example.gymapp.viewmodel.GymViewModel
import kotlinx.serialization.Serializable

@Serializable
data object MembersRoute

@Serializable
data object DueExpiredRoute

@Serializable
data class AddEditMemberRoute(val memberId: Int? = null)

@Composable
fun GymAppRoot(
    viewModel: GymViewModel,
    openDueScreenSignal: Int
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val membersUiState by viewModel.membersUiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val dueExpiredUiState by viewModel.dueExpiredUiState.collectAsStateWithLifecycle()
    val revenueUiState by viewModel.revenueUiState.collectAsStateWithLifecycle()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(openDueScreenSignal) {
        if (openDueScreenSignal > 0) {
            navController.navigate(DueExpiredRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelTab = currentDestination?.hasRoute<MembersRoute>() == true ||
        currentDestination?.hasRoute<DueExpiredRoute>() == true
    val dueBadgeCount = dueExpiredUiState.totalCount

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            bottomBar = {
                if (isTopLevelTab && !useNavigationRail) {
                    NavigationBar {
                        val isMembersSelected = currentDestination?.hasRoute<MembersRoute>() == true
                        val isDueSelected = currentDestination?.hasRoute<DueExpiredRoute>() == true

                        NavigationBarItem(
                            selected = isMembersSelected,
                            onClick = {
                                navController.navigate(MembersRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isMembersSelected) {
                                        Icons.Filled.Group
                                    } else {
                                        Icons.Outlined.Group
                                    },
                                    contentDescription = stringResource(R.string.tab_members)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_members)) },
                            modifier = Modifier.testTag("tab_members")
                        )

                        NavigationBarItem(
                            selected = isDueSelected,
                            onClick = {
                                navController.navigate(DueExpiredRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (dueBadgeCount > 0) {
                                            Badge { Text(dueBadgeCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isDueSelected) {
                                            Icons.Filled.EventBusy
                                        } else {
                                            Icons.Outlined.EventBusy
                                        },
                                        contentDescription = stringResource(R.string.tab_due_expired)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.tab_due_expired)) },
                            modifier = Modifier.testTag("tab_due_expired")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTopLevelTab && useNavigationRail) {
                    NavigationRail {
                        val isMembersSelected = currentDestination?.hasRoute<MembersRoute>() == true
                        val isDueSelected = currentDestination?.hasRoute<DueExpiredRoute>() == true

                        NavigationRailItem(
                            selected = isMembersSelected,
                            onClick = {
                                navController.navigate(MembersRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isMembersSelected) {
                                        Icons.Filled.Group
                                    } else {
                                        Icons.Outlined.Group
                                    },
                                    contentDescription = stringResource(R.string.tab_members)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_members)) },
                            modifier = Modifier.testTag("rail_tab_members")
                        )

                        NavigationRailItem(
                            selected = isDueSelected,
                            onClick = {
                                navController.navigate(DueExpiredRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (dueBadgeCount > 0) {
                                            Badge { Text(dueBadgeCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isDueSelected) {
                                            Icons.Filled.EventBusy
                                        } else {
                                            Icons.Outlined.EventBusy
                                        },
                                        contentDescription = stringResource(R.string.tab_due_expired)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.tab_due_expired)) },
                            modifier = Modifier.testTag("rail_tab_due_expired")
                        )
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = MembersRoute,
                    modifier = Modifier.weight(1f)
                ) {
                    composable<MembersRoute> {
                        MemberListScreen(
                            uiState = membersUiState,
                            revenueUiState = revenueUiState,
                            searchQuery = searchQuery,
                            showNotificationPermissionPrompt = !hasNotificationPermission,
                            onRequestNotificationPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                }
                            },
                            onSearchQueryChange = viewModel::onSearchQueryChange,
                            onAddMemberClick = {
                                navController.navigate(AddEditMemberRoute(memberId = null))
                            },
                            onMemberClick = { id ->
                                navController.navigate(AddEditMemberRoute(memberId = id))
                            }
                        )
                    }

                    composable<DueExpiredRoute> {
                        DueExpiredScreen(
                            uiState = dueExpiredUiState,
                            onMarkFeePaid = viewModel::markFeePaid
                        )
                    }

                    composable<AddEditMemberRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<AddEditMemberRoute>()
                        AddEditMemberScreen(
                            memberId = route.memberId,
                            loadMember = viewModel::getMemberById,
                            onSaveMember = viewModel::saveMember,
                            onDeleteMember = viewModel::deleteMember,
                            onMarkFeePaidImmediate = viewModel::markFeePaid,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
