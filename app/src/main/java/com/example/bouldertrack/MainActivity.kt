package com.example.bouldertrack

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.bouldertrack.domain.model.AppThemeMode
import com.example.bouldertrack.ui.components.LiquidGlassNavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.bouldertrack.ui.components.GlobalTimerBar
import com.example.bouldertrack.ui.navigation.BetaLibrary
import com.example.bouldertrack.ui.navigation.Projects
import com.example.bouldertrack.ui.navigation.SessionDetail
import com.example.bouldertrack.ui.navigation.SessionList
import com.example.bouldertrack.ui.navigation.Settings
import com.example.bouldertrack.ui.navigation.Stats
import com.example.bouldertrack.ui.screens.beta_library.BetaLibraryScreen
import com.example.bouldertrack.ui.screens.projects.ProjectsScreen
import com.example.bouldertrack.ui.screens.session_detail.SessionDetailScreen
import com.example.bouldertrack.ui.screens.session_list.SessionListScreen
import com.example.bouldertrack.ui.screens.settings.SettingsScreen
import com.example.bouldertrack.ui.screens.stats.StatsScreen
import com.example.bouldertrack.ui.theme.BoulderTrackTheme

/**
 * Główna aktywność aplikacji hostująca nawigację Jetpack Compose oraz szkielet interfejsu (Scaffold).
 *
 * Dziedziczy po [AppCompatActivity], co umożliwia dynamiczną zmianę języka aplikacji w locie
 * za pośrednictwem [androidx.appcompat.app.AppCompatDelegate.setApplicationLocales].
 */
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bouldertrack.domain.repository.PreferencesRepository
import org.koin.core.context.GlobalContext

class MainActivity : AppCompatActivity() {

    private val preferencesRepository: PreferencesRepository by lazy {
        GlobalContext.get().get()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentTheme by preferencesRepository.appThemeFlow.collectAsStateWithLifecycle(
                initialValue = preferencesRepository.getAppTheme()
            )

            BoulderTrackTheme(themeMode = currentTheme) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                    ) {}
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = SessionList,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = {
                            slideIntoContainer(
                                towards = navSlideDirection(),
                                animationSpec = tween(300)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = navSlideDirection(),
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = navSlideDirection(),
                                animationSpec = tween(300)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = navSlideDirection(),
                                animationSpec = tween(300)
                            )
                        }
                    ) {
                        composable<SessionList> {
                            SessionListScreen(
                                onNavigateToSession = { sessionId ->
                                    navController.navigate(SessionDetail(sessionId))
                                },
                                onNavigateToStats = {
                                    navController.navigate(Stats)
                                },
                                onNavigateToBetaLibrary = {
                                    navController.navigate(BetaLibrary)
                                },
                                onNavigateToSettings = {
                                    navController.navigate(Settings)
                                },
                                onNavigateToProjects = {
                                    navController.navigate(Projects)
                                }
                            )
                        }
                        composable<SessionDetail> { backStackEntry ->
                            val detail = backStackEntry.toRoute<SessionDetail>()
                            SessionDetailScreen(
                                sessionId = detail.sessionId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable<Stats> {
                            StatsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable<BetaLibrary> {
                            BetaLibraryScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable<Projects> {
                            ProjectsScreen(
                                onNavigateToSession = { sessionId ->
                                    navController.navigate(SessionDetail(sessionId))
                                }
                            )
                        }
                        composable<Settings> {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // Pływający dolny obszar (stoper odpoczynku + liquid glass pasek nawigacji)
                    // Unosi się bezpośrednio nad przewijaną zawartością, ukazując ją pod półprzezroczystym szkłem
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Transparent)
                    ) {
                        val isSessionDetail = currentDestination?.hierarchy?.any { it.route?.contains("SessionDetail") == true } == true
                        if (!isSessionDetail) {
                            GlobalTimerBar()
                        }

                        val isAmoled = currentTheme == AppThemeMode.AMOLED

                        LiquidGlassNavigationBar(
                            currentDestination = currentDestination,
                            onNavigateToHome = {
                                val isAtSessionListOnly = currentDestination?.route?.endsWith("SessionList") == true
                                if (!isAtSessionListOnly) {
                                    val popped = navController.popBackStack(SessionList, inclusive = false)
                                    if (!popped) {
                                        navController.navigate(SessionList) {
                                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            },
                            onNavigateToProjects = {
                                val isChildRoute = currentDestination?.route?.let { it.contains("SessionDetail") || it.contains("Settings") } == true
                                if (isChildRoute) {
                                    navController.popBackStack(SessionList, inclusive = false)
                                }
                                navController.navigate(Projects) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToStats = {
                                val isChildRoute = currentDestination?.route?.let { it.contains("SessionDetail") || it.contains("Settings") } == true
                                if (isChildRoute) {
                                    navController.popBackStack(SessionList, inclusive = false)
                                }
                                navController.navigate(Stats) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToBetaLibrary = {
                                val isChildRoute = currentDestination?.route?.let { it.contains("SessionDetail") || it.contains("Settings") } == true
                                if (isChildRoute) {
                                    navController.popBackStack(SessionList, inclusive = false)
                                }
                                navController.navigate(BetaLibrary) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            isAmoled = isAmoled
                        )
                    }
                }
            }
        }
    }
}

/**
 * Zwraca logiczną pozycję (indeks) ekranu w strukturze nawigacji aplikacji:
 * 0: Home (SessionList)
 * 1: Projekty
 * 2: Statystyki
 * 3: Baza Patentów (BetaLibrary)
 * 4: Ekrany szczegółowe / podrzędne (SessionDetail, Settings itp.)
 */
private fun getScreenOrder(route: String?): Int = when {
    route == null -> 0
    route.contains("SessionList") -> 0
    route.contains("Projects") -> 1
    route.contains("Stats") -> 2
    route.contains("BetaLibrary") -> 3
    else -> 4
}

/**
 * Ustala dynamiczny kierunek animacji slajdu na podstawie relacji indeksu ekranu docelowego i początkowego:
 * - Przejście w prawo (np. Home -> Projekty, Projekty -> Statystyki): nowy ekran wsuwa się z prawej do lewej (SlideDirection.Left).
 * - Przejście w lewo (np. Statystyki -> Projekty, Projekty -> Home): nowy ekran wsuwa się z lewej do prawej (SlideDirection.Right).
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.navSlideDirection(): AnimatedContentTransitionScope.SlideDirection {
    val fromOrder = getScreenOrder(initialState.destination.route)
    val toOrder = getScreenOrder(targetState.destination.route)
    return if (toOrder >= fromOrder) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
}
