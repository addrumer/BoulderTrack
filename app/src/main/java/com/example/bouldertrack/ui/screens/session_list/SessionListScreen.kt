package com.example.bouldertrack.ui.screens.session_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terrain
import com.example.bouldertrack.ui.components.EditSessionDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.bouldertrack.R
import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.model.Grade
import com.example.bouldertrack.domain.model.SessionWithStats
import com.example.bouldertrack.presentation.sessionlist.SessionListUiState
import com.example.bouldertrack.presentation.sessionlist.SessionListViewModel
import com.example.bouldertrack.presentation.stats.StatsUiState
import com.example.bouldertrack.presentation.stats.StatsViewModel
import com.example.bouldertrack.ui.components.formatGradeDisplayName
import com.example.bouldertrack.ui.theme.getClimbingGradeColor
import org.koin.androidx.compose.koinViewModel

/**
 * Główny ekran dashboardu wyświetlający listę sesji wspinaczkowych, podsumowanie aktywności w miesiącu,
 * kafelki szybkich statystyk oraz karuzelę aktywnych projektów boulderowych.
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SessionListScreen(
    onNavigateToSession: (Long) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToBetaLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProjects: () -> Unit = {},
    viewModel: SessionListViewModel = koinViewModel(),
    statsViewModel: StatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statsState by statsViewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    // Dialog tworzenia nowej sesji treningowej
    if (showDialog) {
        var location by remember { mutableStateOf("") }
        val allGyms = when (val s = uiState) {
            is SessionListUiState.Success -> s.allGyms
            else -> emptyList()
        }
        val suggestions = if (location.isBlank()) allGyms else allGyms.filter { it.contains(location, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.nowa_sesja)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text(stringResource(R.string.nazwa_lokalizacji)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (suggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.ostatnie_scianki),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            suggestions.take(5).forEach { gym ->
                                SuggestionChip(
                                    onClick = { location = gym },
                                    label = { Text(gym) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createSession(location)
                        showDialog = false
                    }
                ) { Text(stringResource(R.string.start)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.anuluj)) }
            }
        )
    }

    // Dialog potwierdzenia usunięcia wybranej sesji
    var sessionToDelete by remember { mutableStateOf<SessionWithStats?>(null) }
    var sessionToEdit by remember { mutableStateOf<SessionWithStats?>(null) }

    if (sessionToEdit != null) {
        val allGyms = when (val s = uiState) {
            is SessionListUiState.Success -> s.allGyms
            else -> emptyList()
        }
        EditSessionDialog(
            initialLocation = sessionToEdit!!.location,
            initialDate = sessionToEdit!!.date,
            initialNotes = sessionToEdit!!.notes,
            gymSuggestions = allGyms,
            onDismiss = { sessionToEdit = null },
            onSave = { loc, date, notes ->
                sessionToEdit?.let { s ->
                    viewModel.updateSession(s.id, loc, date, notes)
                }
                sessionToEdit = null
            }
        )
    }

    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text(stringResource(R.string.usuwanie_sesji)) },
            text = { Text(stringResource(R.string.czy_usunac_sesje, sessionToDelete?.location ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        sessionToDelete?.let { viewModel.deleteSession(it.id) }
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.usun), color = MaterialTheme.colorScheme.onError) }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) { Text(stringResource(R.string.anuluj)) }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is SessionListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is SessionListUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        DashboardHeader(
                            totalBoulders = statsState.totalBoulders.toInt(),
                            onNavigateToSettings = onNavigateToSettings
                        )
                        QuickStatsGrid(statsState)

                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.brak_sesji),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StartSessionButton(onClick = { showDialog = true })
                    }
                }
                is SessionListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            DashboardHeader(
                                totalBoulders = statsState.totalBoulders.toInt(),
                                onNavigateToSettings = onNavigateToSettings
                            )
                        }

                        item { QuickStatsGrid(statsState) }

                        item { StartSessionButton(onClick = { showDialog = true }) }

                        item {
                            ActiveProjectsRow(
                                projects = state.activeProjects,
                                onNavigateToProjects = onNavigateToProjects
                            )
                        }

                        item {
                            Text(
                                text = stringResource(R.string.twoje_sesje),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Wyszukiwarka sesji
                        item {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.szukaj_sesji_lub_sciany),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Szukaj",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    if (state.searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Wyczyść",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }

                        // Filtry ścianek (widoczne jeśli w bazie jest więcej niż 1 ściana)
                        if (state.allGyms.size > 1) {
                            item {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item {
                                        FilterChip(
                                            selected = state.selectedGym == null,
                                            onClick = { viewModel.onGymFilterChange(null) },
                                            label = { Text(stringResource(R.string.wszystkie_sciany)) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                    items(state.allGyms) { gym ->
                                        FilterChip(
                                            selected = state.selectedGym == gym,
                                            onClick = { viewModel.onGymFilterChange(gym) },
                                            label = { Text(gym) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Informacja o braku wyników filtrowania
                        if (state.sessions.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.brak_wynikow_wyszukiwania),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        items(state.sessions, key = { it.id }) { session ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        sessionToDelete = session
                                        false
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.usun),
                                            tint = Color.White
                                        )
                                    }
                                },
                                content = {
                                    SessionCard(
                                        session = session,
                                        onClick = { onNavigateToSession(session.id) },
                                        onEditClick = { sessionToEdit = session }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Górny nagłówek z powitaniem użytkownika, podsumowaniem przejść w miesiącu i skrótem do Ustawień.
 */
@Composable
fun DashboardHeader(totalBoulders: Int, onNavigateToSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.powitanie),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.passa_miesiac, totalBoulders),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = stringResource(R.string.ustawienia),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Siatka kafelków szybkiego podglądu statystyk (Top/Flash, maksymalna wycena, liczba fleszy).
 */
@Composable
fun QuickStatsGrid(statsState: StatsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            modifier = Modifier.weight(1f),
            title = "Top/Flash",
            value = "${statsState.topCount + statsState.flashCount}",
            icon = Icons.Default.DoneAll,
            iconColor = MaterialTheme.colorScheme.primary
        )
        val maxGrade = statsState.bouldersByGrade.keys.maxWithOrNull(
            compareBy<String> { Grade(it).difficultyRank() }
        ) ?: "-"
        QuickStatCard(
            modifier = Modifier.weight(1f),
            title = "Max Wycena",
            value = maxGrade,
            icon = Icons.Default.Landscape,
            iconColor = MaterialTheme.colorScheme.primary
        )
        QuickStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.flash),
            value = "${statsState.flashCount}",
            icon = Icons.Default.Bolt,
            iconColor = Color(0xFFFFC107)
        )
    }
}

/**
 * Kompaktowy kafelek statystyki z ikoną, dużą liczbą oraz etykietą opisową.
 */
@Composable
fun QuickStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Przycisk akcji rozpoczynający nową sesję wspinaczkową.
 */
@Composable
fun StartSessionButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.rozpocznij_nowa_sesje), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Karta pojedynczej sesji wyświetlająca lokalizację, datę oraz liczbę pokonanych boulderów z podziałem na style.
 */
@Composable
fun SessionCard(
    session: SessionWithStats,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = session.location,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edytuj_sesje),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = session.date.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val maxGradeText = session.maxGrade ?: stringResource(R.string.brak)
            Text(
                text = stringResource(R.string.sesja_podsumowanie, session.totalBoulders, maxGradeText),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (session.flashCount > 0 || session.topCount > 0 || session.projectCount > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (session.flashCount > 0) {
                        SessionPillBadge(
                            text = "${stringResource(R.string.flash)}: ${session.flashCount}",
                            color = Color(0xFFFFC107)
                        )
                    }
                    if (session.topCount > 0) {
                        SessionPillBadge(
                            text = "${stringResource(R.string.top)}: ${session.topCount}",
                            color = Color(0xFF4CAF50)
                        )
                    }
                    if (session.projectCount > 0) {
                        SessionPillBadge(
                            text = "${stringResource(R.string.projekt)}: ${session.projectCount}",
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Zaokrąglony tag statusowy informujący o liczbie przejść w określonym stylu.
 */
@Composable
fun SessionPillBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

/**
 * Pozioma karuzela prezentująca aktywne, nieukończone projekty użytkownika.
 */
@Composable
fun ActiveProjectsRow(projects: List<BoulderProblem>, onNavigateToProjects: () -> Unit) {
    if (projects.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToProjects() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.aktualne_projekty),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.moje_projekty),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(projects, key = { it.id }) { project ->
                Card(
                    modifier = Modifier.width(160.dp).clickable { onNavigateToProjects() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {
                        if (project.photoUris.isNotEmpty()) {
                            AsyncImage(
                                model = project.photoUris.first(),
                                contentDescription = "Projekt",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terrain,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        val visualColor = getClimbingGradeColor(project.grade.value)
                        val gradeDisplayName = formatGradeDisplayName(project.grade.value)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(topEnd = 8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (visualColor != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(visualColor, CircleShape)
                                    )
                                }
                                Text(gradeDisplayName, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
