package com.example.bouldertrack.presentation.betalibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.repository.BoulderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Obiekt łączący trwały identyfikator URI multimediów z powiązanym obiektem bouldera [BoulderProblem].
 */
data class BetaMediaItem(
    val uri: String,
    val boulder: BoulderProblem
)

/**
 * ViewModel zarządzający galerią zdjęć i filmów instruktażowych (patentów) ze wszystkich treningów.
 */
class BetaLibraryViewModel(
    boulderRepository: BoulderRepository
) : ViewModel() {

    val mediaItems: StateFlow<List<BetaMediaItem>> = boulderRepository.getAllBoulders()
        .map { boulders ->
            boulders.flatMap { boulder ->
                boulder.photoUris.map { uri ->
                    BetaMediaItem(uri, boulder)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
