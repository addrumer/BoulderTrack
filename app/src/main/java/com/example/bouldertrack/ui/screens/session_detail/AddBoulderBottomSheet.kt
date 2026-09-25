package com.example.bouldertrack.ui.screens.session_detail

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bouldertrack.R
import com.example.bouldertrack.domain.model.BoulderProblem
import com.example.bouldertrack.domain.model.ClimbStyle
import com.example.bouldertrack.domain.model.GradeScale
import com.example.bouldertrack.ui.theme.getClimbingGradeColor

/**
 * Dolny arkusz modalny (bottom sheet) do rejestrowania nowego bouldera lub edycji istniejącego.
 * Umożliwia wybór trudności w różnych skalach (Fontainebleau, Skala V, Kolory ściankowe),
 * wybór stylu przejścia (Flash, Top, Projekt), licznik wstawek, tagi chwytów oraz załączanie multimediów.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBoulderBottomSheet(
    initialBoulder: BoulderProblem? = null,
    onDismiss: () -> Unit,
    onSave: (String, ClimbStyle, Int, List<String>, String?, List<String>) -> Unit,
    defaultGradeScale: GradeScale = GradeScale.FONT,
    onGradeScaleChanged: (GradeScale) -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Określenie początkowej skali wycen na podstawie edytowanego bouldera lub domyślnej skali użytkownika
    val fontGrades = listOf("4", "5a", "5a+", "5b", "5b+", "5c", "5c+", "6a", "6a+", "6b", "6b+", "6c", "6c+", "7a", "7a+", "7b", "7b+", "7c", "7c+", "8a", "8a+", "8b", "8b+", "8c")
    val vScaleGrades = listOf("VB", "V0", "V1", "V2", "V3", "V4", "V5", "V6", "V7", "V8", "V9", "V10", "V11", "V12", "V13", "V14", "V15")
    val colorGrades = listOf(
        stringResource(R.string.kolor_zielony),
        stringResource(R.string.kolor_rozowy),
        stringResource(R.string.kolor_bialy),
        stringResource(R.string.kolor_zolty),
        stringResource(R.string.kolor_pomaranczowy),
        stringResource(R.string.kolor_czerwony),
        stringResource(R.string.kolor_fioletowy),
        stringResource(R.string.kolor_niebieski),
        stringResource(R.string.kolor_czarny)
    )
    val numericGrades = (1..10).map { it.toString() }
    
    var selectedScale by remember { 
        mutableStateOf(
            if (initialBoulder != null) {
                if (vScaleGrades.contains(initialBoulder.grade.value.uppercase()) || initialBoulder.grade.value.startsWith("V", ignoreCase = true)) GradeScale.V_SCALE 
                else if (colorGrades.any { it.equals(initialBoulder.grade.value, ignoreCase = true) }) GradeScale.COLORS
                else if (numericGrades.contains(initialBoulder.grade.value)) GradeScale.SCALE_1_10
                else GradeScale.FONT
            } else {
                defaultGradeScale
            }
        ) 
    }
    
    LaunchedEffect(selectedScale) {
        onGradeScaleChanged(selectedScale)
    }

    var selectedFontGrade by remember { mutableStateOf(if (selectedScale == GradeScale.FONT && initialBoulder != null) initialBoulder.grade.value else "6a") }
    var selectedVScaleGrade by remember { mutableStateOf(if (selectedScale == GradeScale.V_SCALE && initialBoulder != null) initialBoulder.grade.value else "V3") }
    var selectedColorGrade by remember {
        mutableStateOf(
            if (selectedScale == GradeScale.COLORS && initialBoulder != null) {
                colorGrades.firstOrNull { it.equals(initialBoulder.grade.value, ignoreCase = true) } ?: initialBoulder.grade.value
            } else {
                colorGrades.first()
            }
        )
    }
    var selectedNumericGrade by remember { mutableStateOf(if (selectedScale == GradeScale.SCALE_1_10 && initialBoulder != null) initialBoulder.grade.value else "1") }
    
    val currentGradeList = when (selectedScale) {
        GradeScale.FONT -> fontGrades
        GradeScale.V_SCALE -> vScaleGrades
        GradeScale.COLORS -> colorGrades
        GradeScale.SCALE_1_10 -> numericGrades
    }
    val currentSelectedGrade = when (selectedScale) {
        GradeScale.FONT -> selectedFontGrade
        GradeScale.V_SCALE -> selectedVScaleGrade
        GradeScale.COLORS -> selectedColorGrade
        GradeScale.SCALE_1_10 -> selectedNumericGrade
    }
    
    val styles = ClimbStyle.entries.toList()
    var selectedStyle by remember { mutableStateOf(initialBoulder?.style ?: ClimbStyle.FLASH) }
    
    var attempts by remember { mutableIntStateOf(initialBoulder?.attempts ?: 1) }
    
    val tagKrawadki = stringResource(R.string.tag_krawadki)
    val tagOblaki = stringResource(R.string.tag_oblaki)
    val tagDach = stringResource(R.string.tag_dach)
    val tagPolog = stringResource(R.string.tag_polog)
    val tagRysa = stringResource(R.string.tag_rysa)
    val tagSkok = stringResource(R.string.tag_skok)
    
    val availableTags = listOf(tagKrawadki, tagOblaki, tagDach, tagPolog, tagRysa, tagSkok)
    val selectedTags = remember { mutableStateListOf<String>().apply { initialBoulder?.tags?.let { addAll(it) } } }
    
    var note by remember { mutableStateOf(initialBoulder?.note ?: "") }
    val photoUris = remember { mutableStateListOf<Uri>().apply { initialBoulder?.photoUris?.let { addAll(it.map { uri -> Uri.parse(uri) }) } } }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3),
        onResult = { uris -> 
            val spaceLeft = 3 - photoUris.size
            if (spaceLeft > 0) {
                photoUris.addAll(uris.take(spaceLeft))
            }
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.wycena), style = MaterialTheme.typography.titleMedium)
                
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = selectedScale == GradeScale.FONT,
                        onClick = { selectedScale = GradeScale.FONT },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
                    ) {
                        Text("Font")
                    }
                    SegmentedButton(
                        selected = selectedScale == GradeScale.V_SCALE,
                        onClick = { selectedScale = GradeScale.V_SCALE },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
                    ) {
                        Text("V-Scale")
                    }
                    SegmentedButton(
                        selected = selectedScale == GradeScale.COLORS,
                        onClick = { selectedScale = GradeScale.COLORS },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4)
                    ) {
                        Text("Kolory")
                    }
                    SegmentedButton(
                        selected = selectedScale == GradeScale.SCALE_1_10,
                        onClick = { selectedScale = GradeScale.SCALE_1_10 },
                        shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4)
                    ) {
                        Text("1–10")
                    }
                }
            }
            
            // Expanded Grade List UI
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(currentGradeList) { grade ->
                    val isSelected = grade == currentSelectedGrade
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    if (selectedScale == GradeScale.COLORS) {
                        val visualColor = getClimbingGradeColor(grade)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .wrapContentWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .clickable { selectedColorGrade = grade }
                                .padding(horizontal = 14.dp)
                        ) {
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
                                text = grade,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor
                            )
                        }
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .clickable {
                                    when (selectedScale) {
                                        GradeScale.FONT -> selectedFontGrade = grade
                                        GradeScale.V_SCALE -> selectedVScaleGrade = grade
                                        GradeScale.SCALE_1_10 -> selectedNumericGrade = grade
                                        else -> {}
                                    }
                                }
                        ) {
                            Text(
                                text = grade.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = contentColor
                            )
                        }
                    }
                }
            }
            
            Text(stringResource(R.string.styl_przejscia), style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                styles.forEachIndexed { index, style ->
                    SegmentedButton(
                        selected = style == selectedStyle,
                        onClick = {
                            selectedStyle = style
                            when (style) {
                                ClimbStyle.FLASH -> attempts = 1
                                ClimbStyle.TOP -> if (attempts < 2) attempts = 2
                                ClimbStyle.PROJECT -> if (attempts < 1) attempts = 1
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = styles.size)
                    ) {
                        val styleName = when(style) {
                            ClimbStyle.FLASH -> stringResource(R.string.flash)
                            ClimbStyle.TOP -> stringResource(R.string.top)
                            ClimbStyle.PROJECT -> stringResource(R.string.projekt)
                        }
                        Text(styleName)
                    }
                }
            }
            
            val minAttempts = when (selectedStyle) {
                ClimbStyle.FLASH -> 1
                ClimbStyle.TOP -> 2
                ClimbStyle.PROJECT -> 1
            }
            val isAttemptsFixed = selectedStyle == ClimbStyle.FLASH

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.liczba_prob), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { if (attempts > minAttempts) attempts-- },
                        enabled = !isAttemptsFixed && attempts > minAttempts
                    ) { Text("-") }
                    Text(text = attempts.toString(), style = MaterialTheme.typography.titleLarge)
                    Button(
                        onClick = { attempts++ },
                        enabled = !isAttemptsFixed
                    ) { Text("+") }
                }
            }
            
            Text(stringResource(R.string.tagi), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableTags) { tag ->
                    FilterChip(
                        selected = selectedTags.contains(tag),
                        onClick = {
                            if (selectedTags.contains(tag)) selectedTags.remove(tag) else selectedTags.add(tag)
                        },
                        label = { Text(tag) }
                    )
                }
            }
            
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.notatka_opcjonalnie)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(stringResource(R.string.zdjecia), style = MaterialTheme.typography.titleMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(photoUris) { uri ->
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = stringResource(R.string.wybrane_zdjecie),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { photoUris.remove(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .padding(2.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.usun), tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                if (photoUris.size < 3) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { 
                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, contentDescription = "Dodaj", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Dodaj", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    val uriStrings = photoUris.map { uri ->
                        try {
                            context.contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (e: SecurityException) {
                            // Ignored
                        }
                        uri.toString()
                    }
                    onSave(currentSelectedGrade, selectedStyle, attempts, selectedTags.toList(), note, uriStrings)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (initialBoulder != null) stringResource(R.string.zapisz_zmiany) else stringResource(R.string.dodaj_bald))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
