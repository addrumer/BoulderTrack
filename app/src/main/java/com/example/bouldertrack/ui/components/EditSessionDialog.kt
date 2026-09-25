package com.example.bouldertrack.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bouldertrack.R
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/**
 * Dialog umożliwiający edycję parametrów istniejącej sesji wspinaczkowej:
 * - Nazwy ściany / lokalizacji (wraz z podpowiedziami poprzednich ścianek),
 * - Daty treningu (poprzez natywny kalendarz DatePickerDialog lub szybkie przyciski Dzisiaj / Wczoraj),
 * - Opcjonalnych notatek treningowych.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditSessionDialog(
    initialLocation: String,
    initialDate: LocalDate,
    initialNotes: String?,
    gymSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (location: String, date: LocalDate, notes: String?) -> Unit
) {
    val context = LocalContext.current
    var location by remember { mutableStateOf(initialLocation) }
    var date by remember { mutableStateOf(initialDate) }
    var notes by remember { mutableStateOf(initialNotes ?: "") }

    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val yesterday = remember(today) { today.minus(1, DateTimeUnit.DAY) }

    val filteredSuggestions = remember(location, gymSuggestions) {
        if (location.isBlank()) gymSuggestions
        else gymSuggestions.filter { it.contains(location, ignoreCase = true) && !it.equals(location, ignoreCase = true) }
    }

    val datePickerDialog = remember(date) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                date = LocalDate(year, month + 1, dayOfMonth)
            },
            date.year,
            date.monthNumber - 1,
            date.dayOfMonth
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(stringResource(R.string.edytuj_sesje))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pole nazwy lokalizacji
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.nazwa_lokalizacji)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Podpowiedzi ścianek
                if (filteredSuggestions.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        filteredSuggestions.take(4).forEach { gym ->
                            SuggestionChip(
                                onClick = { location = gym },
                                label = { Text(gym) }
                            )
                        }
                    }
                }

                // Wybór daty sesji
                Text(
                    text = stringResource(R.string.data_sesji),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = date.toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Text(
                            text = stringResource(R.string.zmien_date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Szybkie chipy daty
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = { date = today },
                        label = { Text("Dzisiaj") }
                    )
                    SuggestionChip(
                        onClick = { date = yesterday },
                        label = { Text("Wczoraj") }
                    )
                }

                // Notatki opcjonalne
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notatka_opcjonalnie)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(location, date, notes)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.zapisz))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.anuluj))
            }
        }
    )
}
