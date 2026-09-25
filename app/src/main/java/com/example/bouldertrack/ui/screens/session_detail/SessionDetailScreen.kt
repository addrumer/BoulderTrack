package com.example.bouldertrack.ui.screens.session_detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.bouldertrack.ui.components.formatGradeDisplayName
import com.example.bouldertrack.ui.components.formatTagDisplayName
import com.example.bouldertrack.ui.theme.getClimbingGradeColor
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import com.example.bouldertrack.ui.components.EditSessionDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import com.example.bouldertrack.domain.manager.TimerManager

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.bouldertrack.R
import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.model.ClimbStyle
import com.example.bouldertrack.presentation.sessiondetail.SessionDetailViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Ekran szczegółów sesji wspinaczkowej.
 *
 * Umożliwia wspinaczom:
 * - Podgląd postępów treningu (liczba przejść Flash / Top / Projekt)
 * - Przeglądanie zalogowanych boulderów wraz z tagami, notatkami i multimediami patentów
 * - Obsługę stopera odpoczynku między próbami
 * - Rejestrowanie nowych przejść przez dolny arkusz (bottom sheet)
 * - Generowanie estetycznego podsumowania sesji do udostępnienia
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: SessionDetailViewModel = koinViewModel()
) {
    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    var editingBoulder by remember { mutableStateOf<BoulderProblem?>(null) }
    var showEditSessionDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var boulderToDelete by remember { mutableStateOf<BoulderProblem?>(null) }
    var viewerPhotos by remember { mutableStateOf<List<String>?>(null) }

    if (viewerPhotos != null) {
        FullScreenMediaViewer(
            mediaUris = viewerPhotos!!,
            onDismiss = { viewerPhotos = null }
        )
    }

    if (showShareDialog && uiState.session != null) {
        ShareSessionDialog(
            session = uiState.session!!,
            boulders = uiState.boulders,
            onDismiss = { showShareDialog = false }
        )
    }

    if (showEditSessionDialog && uiState.session != null) {
        EditSessionDialog(
            initialLocation = uiState.session!!.location,
            initialDate = uiState.session!!.date,
            initialNotes = uiState.session!!.notes,
            gymSuggestions = uiState.allGyms,
            onDismiss = { showEditSessionDialog = false },
            onSave = { loc, date, notes ->
                viewModel.updateSession(loc, date, notes)
            }
        )
    }

    if (boulderToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { boulderToDelete = null },
            title = { Text(stringResource(R.string.usuwanie_balda)) },
            text = { Text(stringResource(R.string.czy_usunac_przejscie)) },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        boulderToDelete?.let { viewModel.deleteBoulder(it.id) }
                        boulderToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.usun), color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { boulderToDelete = null }) {
                    Text(stringResource(R.string.anuluj))
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.session?.location ?: stringResource(R.string.sesja),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.wroc),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditSessionDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edytuj_sesje),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.udostepnij),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.dodaj_bald))
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header Stats
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBox(label = stringResource(R.string.flash), count = uiState.flashCount, color = Color(0xFF4CAF50))
                        StatBox(label = stringResource(R.string.top), count = uiState.topCount, color = Color(0xFF2196F3))
                        StatBox(label = stringResource(R.string.projekt), count = uiState.projectCount, color = Color(0xFFFF9800))
                    }
                }

                // Stoper odpoczynku umieszczony na górze ekranu (nigdy nie nachodzi na przycisk FAB dodawania balda)
                RestTimerCard()

                if (uiState.boulders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.brak_baldow),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showBottomSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.dodaj_bald))
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.boulders, key = { it.id }) { boulder ->
                            BoulderCard(
                                boulder = boulder,
                                onEditClick = { editingBoulder = boulder },
                                onDeleteClick = { boulderToDelete = boulder },
                                onPhotoClick = { viewerPhotos = it }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AddBoulderBottomSheet(
            onDismiss = { showBottomSheet = false },
            defaultGradeScale = uiState.defaultGradeScale,
            onSave = { grade, style, attempts, tags, note, uriStrings ->
                viewModel.addBoulder(context, grade, style, attempts, tags, note, uriStrings)
                showBottomSheet = false
            }
        )
    }

    if (editingBoulder != null) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AddBoulderBottomSheet(
            initialBoulder = editingBoulder,
            onDismiss = { editingBoulder = null },
            defaultGradeScale = uiState.defaultGradeScale,
            onSave = { grade, style, attempts, tags, note, uriStrings ->
                editingBoulder?.let { b ->
                    viewModel.updateBoulder(context, b.id, grade, style, attempts, tags, note, uriStrings)
                }
                editingBoulder = null
            }
        )
    }
}

/**
 * Kafelek podsumowania wyświetlający liczbę przejść dla określonego stylu (Flash, Top, Projekt).
 */
@Composable
fun StatBox(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Karta pojedynczego bouldera zawierająca plakietkę stylu, licznik prób,
 * tagi chwytów, notatkę o patentach oraz galerię załączonych zdjęć i wideo.
 */
@Composable
fun BoulderCard(
    boulder: BoulderProblem,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onPhotoClick: (List<String>) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val visualColor = getClimbingGradeColor(boulder.grade.value)
                        if (visualColor != null) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(visualColor, CircleShape)
                                    .then(
                                        if (visualColor == Color(0xFFF5F5F5) || visualColor == Color.White) {
                                            Modifier.border(1.dp, Color.Gray, CircleShape)
                                        } else Modifier
                                    )
                            )
                        }
                        Text(
                            text = formatGradeDisplayName(boulder.grade.value),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        val badgeColor = when (boulder.style) {
                            ClimbStyle.FLASH -> Color(0xFF4CAF50)
                            ClimbStyle.TOP -> Color(0xFF2196F3)
                            ClimbStyle.PROJECT -> Color(0xFFFF9800)
                        }
                        Text(
                            text = boulder.style.name,
                            color = Color.White,
                            modifier = Modifier
                                .background(badgeColor, MaterialTheme.shapes.small)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium
                        )

                        Text(
                            text = stringResource(R.string.liczba_prob_wartosc, boulder.attempts),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (boulder.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            boulder.tags.forEach { tag ->
                                SuggestionChip(onClick = {}, label = { Text(formatTagDisplayName(tag)) })
                            }
                        }
                    }

                    if (!boulder.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = boulder.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edytuj_bald),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.usun),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (boulder.photoUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(boulder.photoUris) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = stringResource(R.string.zdjecie_balda),
                            modifier = Modifier
                                .size(100.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPhotoClick(boulder.photoUris) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}





/**
 * Pełnoekranowa przeglądarka multimediów (zdjęć i wideo) z obsługą gestów zoomu i pagera.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenMediaViewer(
    mediaUris: List<String>,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).systemBarsPadding()) {
            val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { mediaUris.size })
            var isZoomed by remember { mutableStateOf(false) }
            
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isZoomed
            ) { page ->
                val uriString = mediaUris[page]
                val isVideo = uriString.endsWith(".mp4", ignoreCase = true) || uriString.contains("video", ignoreCase = true)
                
                if (isVideo) {
                    val exoPlayer = remember {
                        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                            setMediaItem(androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(uriString)))
                            prepare()
                            playWhenReady = true
                        }
                    }
                    
                    androidx.compose.runtime.DisposableEffect(exoPlayer) {
                        onDispose {
                            exoPlayer.release()
                        }
                    }
                    
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            androidx.media3.ui.PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    var scale by remember { mutableFloatStateOf(1f) }
                    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                    var imageSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
                    
                    LaunchedEffect(scale) {
                        isZoomed = scale > 1f
                    }
                    
                    LaunchedEffect(pagerState.currentPage) {
                        scale = 1f
                        offset = androidx.compose.ui.geometry.Offset.Zero
                    }
                    
                    coil.compose.AsyncImage(
                        model = uriString,
                        contentDescription = stringResource(R.string.powiekszone_zdjecie),
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { imageSize = it }
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    do {
                                        val event = awaitPointerEvent()
                                        val pointers = event.changes
                                        
                                        val isMultiTouch = pointers.size >= 2
                                        val isCurrentlyZoomed = scale > 1.01f
                                        
                                        if (isMultiTouch || isCurrentlyZoomed) {
                                            val zoomChange = event.calculateZoom()
                                            val panChange = event.calculatePan()
                                            
                                            if (zoomChange != 1f || panChange != androidx.compose.ui.geometry.Offset.Zero) {
                                                scale = (scale * zoomChange).coerceIn(1f, 5f)
                                                val maxX = (imageSize.width * (scale - 1)) / 2f
                                                val maxY = (imageSize.height * (scale - 1)) / 2f
                                                offset = androidx.compose.ui.geometry.Offset(
                                                    x = (offset.x + panChange.x).coerceIn(-maxX, maxX),
                                                    y = (offset.y + panChange.y).coerceIn(-maxY, maxY)
                                                )
                                            }
                                            pointers.forEach { if (it.positionChanged()) it.consume() }
                                        }
                                    } while (event.changes.any { it.pressed })
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = { tapOffset ->
                                        if (scale > 1f) {
                                            scale = 1f
                                            offset = androidx.compose.ui.geometry.Offset.Zero
                                        } else {
                                            scale = 2.5f
                                            val maxX = (imageSize.width * (scale - 1)) / 2f
                                            val maxY = (imageSize.height * (scale - 1)) / 2f
                                            offset = androidx.compose.ui.geometry.Offset(
                                                x = (imageSize.width / 2f - tapOffset.x) * scale,
                                                y = (imageSize.height / 2f - tapOffset.y) * scale
                                            ).let {
                                                androidx.compose.ui.geometry.Offset(it.x.coerceIn(-maxX, maxX), it.y.coerceIn(-maxY, maxY))
                                            }
                                        }
                                    }
                                )
                            }
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
            }
            
            TopAppBar(
                title = { 
                    if (mediaUris.size > 1) {
                        Text("${pagerState.currentPage + 1} / ${mediaUris.size}", color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij", tint = Color.White)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    }
}

/**
 * Nowoczesna, wyrazista karta stopera odpoczynku między wstawkami.
 *
 * Zaprojektowana z myślą o czytelności z odległości (np. telefon leżący na materacu wspinaczkowym).
 * Posiada duży zegar cyfrowy, płynny pasek postępu, szybkie przyciski regulacji czasu (-30s, +30s, +1m)
 * oraz wyraźną sygnalizację zakończenia odpoczynku ze zmianą kolorystyki i animacją.
 */
@Composable
fun RestTimerCard(
    modifier: Modifier = Modifier
) {
    val timeRemaining by TimerManager.timeRemaining.collectAsStateWithLifecycle()
    val targetTime by TimerManager.targetTime.collectAsStateWithLifecycle()
    val isRunning by TimerManager.isRunning.collectAsStateWithLifecycle()

    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)
    val isFinished = timeRemaining == 0

    // Płynna animacja postępu odliczania
    val progressTarget = if (targetTime > 0) (timeRemaining.toFloat() / targetTime.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 400),
        label = "timer_progress"
    )

    // Dynamiczna kolorystyka w zależności od stanu odliczania
    val cardBgColor by animateColorAsState(
        targetValue = when {
            isFinished -> Color(0xFF4CAF50).copy(alpha = 0.15f)
            isRunning -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        },
        label = "timer_card_bg"
    )

    val borderStroke = when {
        isFinished -> BorderStroke(1.5.dp, Color(0xFF4CAF50).copy(alpha = 0.8f))
        isRunning -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        else -> null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Górny wiersz: Etykieta stanu + Zegar + Główny przycisk akcji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Informacja o stanie
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isFinished) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (isFinished) stringResource(R.string.gotowy_na_wstawke) else stringResource(R.string.stoper_odpoczynku),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isFinished) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = when {
                            isFinished -> stringResource(R.string.czas_na_wstawke_opis)
                            isRunning -> stringResource(R.string.odpoczywaj)
                            else -> stringResource(R.string.odpoczywaj)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Cyfrowy licznik czasu oraz okrągły przycisk akcji (Play / Pause / Nowa wstawka)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = when {
                            isFinished -> Color(0xFF4CAF50)
                            isRunning -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    FilledIconButton(
                        onClick = {
                            if (isFinished) {
                                TimerManager.startTimer()
                            } else {
                                TimerManager.toggleTimer()
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isFinished) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = when {
                                isFinished -> Icons.Default.PlayArrow
                                isRunning -> Icons.Default.Pause
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = when {
                                isFinished -> stringResource(R.string.nowa_wstawka)
                                isRunning -> stringResource(R.string.wstrzymaj)
                                else -> stringResource(R.string.wznow)
                            }
                        )
                    }
                }
            }

            // Pasek postępu odliczania
            LinearProgressIndicator(
                progress = { if (isFinished) 1f else animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isFinished) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            // Szybkie chipy regulacji czasu (-30s, +30s, +1m) oraz reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SuggestionChip(
                        onClick = { TimerManager.addTime(-30) },
                        label = { Text("-30s", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    SuggestionChip(
                        onClick = { TimerManager.addTime(30) },
                        label = { Text("+30s", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    SuggestionChip(
                        onClick = { TimerManager.addTime(60) },
                        label = { Text("+1m", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                TextButton(
                    onClick = { TimerManager.resetTimer() },
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.resetuj),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.resetuj),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

/**
 * Okno dialogowe prezentujące estetyczną, gotową do zrzutu ekranu kartę z podsumowaniem sesji.
 */
@Composable
fun ShareSessionDialog(
    session: com.example.bouldertrack.domain.model.Session,
    boulders: List<com.example.bouldertrack.domain.model.BoulderProblem>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18191E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF8235).copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "BoulderTrack",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = session.location.ifBlank { "Mocna Sesja" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = session.date.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                val sentCount = boulders.count { it.style != ClimbStyle.PROJECT }
                val maxGrade = boulders.maxByOrNull { it.grade.difficultyRank() }?.grade?.value?.uppercase() ?: "-"
                val totalAttempts = boulders.sumOf { it.attempts }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = sentCount.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.topow),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = maxGrade,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Max Grade",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalAttempts.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.wstawek),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                val flashCount = boulders.count { it.style == ClimbStyle.FLASH }
                val topCount = boulders.count { it.style == ClimbStyle.TOP }
                val projectCount = boulders.count { it.style == ClimbStyle.PROJECT }

                if (flashCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚡ $flashCount Flash",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFB300)
                    )
                }

                val context = androidx.compose.ui.platform.LocalContext.current

                fun sendShareIntent() {
                    val summaryText = buildString {
                        appendLine("🧗‍♂️ Trening w BoulderTrack!")
                        appendLine("📍 Miejsce: ${session.location.ifBlank { "Ściana wspinaczkowa" }}")
                        appendLine("📅 Data: ${session.date}")
                        appendLine("🏆 Max wycena: $maxGrade")
                        appendLine("📊 Przejścia: $sentCount (⚡ $flashCount Flash, ✅ $topCount Top)")
                        if (projectCount > 0) {
                            appendLine("🔨 W toku: $projectCount projektów")
                        }
                        appendLine("🔥 Łącznie wstawek: $totalAttempts")
                        if (!session.notes.isNullOrBlank()) {
                            appendLine("📝 Notatka: ${session.notes}")
                        }
                        appendLine()
                        append("#BoulderTrack #Climbing #Bouldering")
                    }

                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Podsumowanie treningu boulderowego")
                        putExtra(android.content.Intent.EXTRA_TEXT, summaryText)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Udostępnij podsumowanie sesji"))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.zamknij))
                    }

                    Button(
                        onClick = {
                            sendShareIntent()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.udostepnij_sesje_przycisk), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
