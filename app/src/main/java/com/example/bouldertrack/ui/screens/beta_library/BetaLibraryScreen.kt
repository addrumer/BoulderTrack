package com.example.bouldertrack.ui.screens.beta_library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.bouldertrack.R
import com.example.bouldertrack.presentation.betalibrary.BetaLibraryViewModel
import com.example.bouldertrack.ui.screens.session_detail.FullScreenMediaViewer
import org.koin.androidx.compose.koinViewModel

/**
 * Galeria multimediów agregująca wszystkie zapisane zdjęcia i nagrania wideo patentów ze wszystkich sesji wspinaczkowych.
 * Umożliwia przeglądanie materiałów w siatce oraz otwieranie pełnoekranowego odtwarzacza z zoomem i pagerem.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetaLibraryScreen(
    onNavigateBack: () -> Unit,
    viewModel: BetaLibraryViewModel = koinViewModel()
) {
    val mediaItems by viewModel.mediaItems.collectAsStateWithLifecycle()
    var viewerPhotos by remember { mutableStateOf<List<String>?>(null) }
    var initialPage by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patenty") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.wroc))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (mediaItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.brak_patentow), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 120.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(mediaItems.size) { index ->
                    val item = mediaItems[index]
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewerPhotos = mediaItems.map { it.uri }
                                initialPage = index
                            }
                    ) {
                        AsyncImage(
                            model = item.uri,
                            contentDescription = "Patent",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topEnd = 8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.boulder.grade.value.uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        viewerPhotos?.let { uris ->
            FullScreenMediaViewer(mediaUris = uris, onDismiss = { viewerPhotos = null })
        }
    }
}
