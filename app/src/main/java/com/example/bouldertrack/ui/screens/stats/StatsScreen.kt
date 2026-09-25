package com.example.bouldertrack.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bouldertrack.R
import com.example.bouldertrack.domain.model.StatsTimeRange
import com.example.bouldertrack.presentation.stats.StatsViewModel
import com.example.bouldertrack.ui.theme.PrimaryOrange
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import kotlin.math.cos
import kotlin.math.sin

/**
 * Panel analityczny prezentujący statystyki treningowe: liczbę ukończonych boulderów,
 * średnią liczbę wstawek na sukces, odznaki osiągnięć, piramidę wycen,
 * radar stylu wspinania oraz oś czasu z progresem trudności.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statystyki), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.wroc))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Filtry zakresu czasowego
                val ranges = listOf(
                    StatsTimeRange.ALL_TIME,
                    StatsTimeRange.LAST_30_DAYS,
                    StatsTimeRange.LAST_90_DAYS,
                    StatsTimeRange.THIS_YEAR
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(ranges) { range ->
                        val isSelected = uiState.selectedTimeRange == range
                        val label = when (range) {
                            StatsTimeRange.ALL_TIME -> stringResource(R.string.zakres_wszystko)
                            StatsTimeRange.LAST_30_DAYS -> stringResource(R.string.zakres_30_dni)
                            StatsTimeRange.LAST_90_DAYS -> stringResource(R.string.zakres_90_dni)
                            StatsTimeRange.THIS_YEAR -> stringResource(R.string.zakres_ten_rok)
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onTimeRangeSelected(range) },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Główne wskaźniki podsumowujące
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f).height(120.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${uiState.topCount + uiState.flashCount}",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.ukonczone_baldy),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    val sentCount = uiState.topCount + uiState.flashCount
                    val ratio = if (sentCount > 0) String.format("%.1f", uiState.totalAttempts.toFloat() / sentCount) else "-"

                    Card(
                        modifier = Modifier.weight(1f).height(120.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = ratio,
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFC107)
                                )
                                Text(
                                    text = stringResource(R.string.sr_prob_na_sukces),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Kalendarz aktywności treningowej w stylu GitHub (Heatmapa)
                Text(
                    text = stringResource(R.string.kalendarz_aktywnosci),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                ActivityHeatmapCard(
                    activityMap = uiState.activityMap,
                    totalTrainingDays = uiState.totalTrainingDays,
                    currentStreakDays = uiState.currentStreakDays
                )

                // Karuzela odznak i osiągnięć
                if (uiState.badges.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.osiagniecia),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.badges) { badge ->
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(badge, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Wykres piramidy wycen
                if (uiState.bouldersByGrade.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.piramida_przejsc),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    GradeBarChart(gradesMap = uiState.bouldersByGrade)
                }

                // Wykres radarowy stylu wspinania i chwytów
                if (uiState.tagCounts.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.radar_stylu),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    RadarChart(tagCounts = uiState.tagCounts)
                }

                // Oś czasu progresu maksymalnej wyceny
                if (uiState.progressionData.size >= 2) {
                    Text(
                        text = stringResource(R.string.progresja_wycen),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    ProgressionLineChart(progressionData = uiState.progressionData)
                }
            }
        }
    }
}

/**
 * Niestandardowy wykres słupkowy Canvas prezentujący piramidę wspinaczkową użytkownika według wycen.
 */
@Composable
fun GradeBarChart(gradesMap: Map<String, Long>) {
    val sortedGrades = gradesMap.keys.sortedWith(compareBy { it.lowercase() })
    val maxCount = gradesMap.values.maxOrNull()?.toFloat() ?: 1f

    val primaryColor = MaterialTheme.colorScheme.primary
    val gradientBrush = Brush.verticalGradient(colors = listOf(Color(0xFFFF8235), Color(0xFFFF5E3A)))
    val unselectedBarColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val barSpacing = 16.dp.toPx()
            val totalBars = sortedGrades.size
            val availableWidth = size.width - (barSpacing * (totalBars - 1))
            val barWidth = (availableWidth / totalBars).coerceAtMost(48.dp.toPx())

            val maxBarHeight = size.height - 48.dp.toPx()
            val totalChartWidth = (barWidth * totalBars) + (barSpacing * (totalBars - 1))
            var startX = (size.width - totalChartWidth) / 2f

            sortedGrades.forEach { grade ->
                val count = gradesMap[grade] ?: 0L
                val barHeight = (count / maxCount) * maxBarHeight

                val left = startX
                val top = size.height - 24.dp.toPx() - barHeight

                val isMax = count.toFloat() == maxCount

                if (isMax) {
                    drawRoundRect(
                        brush = gradientBrush,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                } else {
                    drawRoundRect(
                        color = unselectedBarColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }

                val valueText = count.toString()
                val valueLayoutResult = textMeasurer.measure(
                    text = valueText,
                    style = TextStyle(color = if (isMax) primaryColor else onSurfaceColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                )
                drawText(
                    textLayoutResult = valueLayoutResult,
                    topLeft = Offset(left + (barWidth - valueLayoutResult.size.width) / 2f, top - valueLayoutResult.size.height - 4.dp.toPx())
                )

                val labelText = grade.uppercase()
                val labelLayoutResult = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(color = onSurfaceColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = labelLayoutResult,
                    topLeft = Offset(left + (barWidth - labelLayoutResult.size.width) / 2f, size.height - 20.dp.toPx())
                )

                startX += barWidth + barSpacing
            }
        }
    }
}

/**
 * Niestandardowy liniowy wykres Canvas obrazujący progres maksymalnej pokonanej wyceny w kolejnych sesjach.
 */
@Composable
fun ProgressionLineChart(progressionData: List<Pair<LocalDate, String>>) {
    val sortedData = progressionData.sortedBy { it.first }
    val uniqueGrades = sortedData.map { it.second }.distinct().sortedWith { a, b ->
        val aNum = a.filter { it.isDigit() }.toIntOrNull() ?: 0
        val bNum = b.filter { it.isDigit() }.toIntOrNull() ?: 0
        if (aNum != bNum) aNum.compareTo(bNum) else a.compareTo(b)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val gradientBrush = Brush.horizontalGradient(colors = listOf(Color(0xFFFF8235), Color(0xFFFF5E3A)))
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val maxGradeIndex = (uniqueGrades.size - 1).coerceAtLeast(1)
            val points = sortedData.mapIndexed { index, pair ->
                val x = (index.toFloat() / (sortedData.size - 1)) * size.width
                val yIndex = uniqueGrades.indexOf(pair.second)
                val y = size.height - ((yIndex.toFloat() / maxGradeIndex) * size.height)
                Offset(x, y)
            }

            val path = Path().apply {
                points.forEachIndexed { index, point ->
                    if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                }
            }
            drawPath(
                path = path,
                brush = gradientBrush,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            points.forEachIndexed { index, point ->
                drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = point)
                drawCircle(color = onSurfaceColor, radius = 3.dp.toPx(), center = point)

                val gradeLabel = sortedData[index].second.uppercase()
                val labelLayout = textMeasurer.measure(
                    text = gradeLabel,
                    style = TextStyle(color = onSurfaceColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                )
                drawText(textLayoutResult = labelLayout, topLeft = Offset(point.x - (labelLayout.size.width / 2f), point.y - labelLayout.size.height - 8.dp.toPx()))

                val dateLabel = sortedData[index].first.toString().takeLast(5)
                val dateLayout = textMeasurer.measure(
                    text = dateLabel,
                    style = TextStyle(color = onSurfaceVariantColor, fontSize = 10.sp)
                )
                drawText(textLayoutResult = dateLayout, topLeft = Offset(point.x - (dateLayout.size.width / 2f), point.y + 8.dp.toPx()))
            }
        }
    }
}

/**
 * Niestandardowy wykres radarowy (pajęczynowy) Canvas prezentujący rozkład stylów i typów chwytów.
 */
@Composable
fun RadarChart(tagCounts: Map<String, Int>) {
    if (tagCounts.isEmpty()) return
    val tags = tagCounts.keys.toList()
    val maxCount = tagCounts.values.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelMedium.copy(color = onSurfaceColor, fontWeight = FontWeight.Bold)

    Card(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 * 0.65f
                val angleStep = (2 * Math.PI) / tags.size

                // Draw web circles
                for (i in 1..4) {
                    val r = radius * (i / 4f)
                    val path = Path()
                    for (j in tags.indices) {
                        val angle = j * angleStep - Math.PI / 2
                        val x = center.x + r * cos(angle).toFloat()
                        val y = center.y + r * sin(angle).toFloat()
                        if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path, color = surfaceColor, style = Stroke(width = 2f))
                }

                // Draw axes and labels
                val dataPath = Path()
                tags.forEachIndexed { index, tag ->
                    val angle = index * angleStep - Math.PI / 2
                    val x = center.x + radius * cos(angle).toFloat()
                    val y = center.y + radius * sin(angle).toFloat()
                    drawLine(color = surfaceColor, start = center, end = Offset(x, y), strokeWidth = 2f)

                    val value = tagCounts.getOrDefault(tag, 0)
                    val valueRadius = radius * (value / maxCount)
                    val vx = center.x + valueRadius * cos(angle).toFloat()
                    val vy = center.y + valueRadius * sin(angle).toFloat()

                    if (index == 0) dataPath.moveTo(vx, vy) else dataPath.lineTo(vx, vy)

                    val textLayoutResult = textMeasurer.measure(tag, textStyle)
                    val tx = center.x + (radius * 1.35f) * cos(angle).toFloat() - textLayoutResult.size.width / 2
                    val ty = center.y + (radius * 1.35f) * sin(angle).toFloat() - textLayoutResult.size.height / 2
                    drawText(textLayoutResult, topLeft = Offset(tx, ty))
                }
                dataPath.close()
                drawPath(dataPath, color = primaryColor.copy(alpha = 0.3f))
                drawPath(dataPath, color = primaryColor, style = Stroke(width = 4f))
            }
        }
    }
}

/**
 * Wykres regularności treningów w stylu GitHub (Contribution Heatmap)
 * prezentujący aktywność wspinaczkową w poszczególnych dniach z ostatnich 16 tygodni.
 */
@Composable
fun ActivityHeatmapCard(
    activityMap: Map<LocalDate, Int>,
    totalTrainingDays: Int,
    currentStreakDays: Int
) {
    val today = androidx.compose.runtime.remember {
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    }
    val todayEpoch = today.toEpochDays()
    val weeksCount = 16

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Nagłówek ze statystykami regularności
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dni_treningowych, totalTrainingDays),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (currentStreakDays > 0) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF5E3A).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.passa_dni, currentStreakDays),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5E3A)
                        )
                    }
                }
            }

            // Przewijana poziomo siatka (16 tygodni po 7 dni)
            val horizontalScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScroll),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (weekIndex in (weeksCount - 1) downTo 0) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (dayOfWeek in 0..6) {
                            val daysBack = (weekIndex * 7) + (6 - dayOfWeek)
                            val dayEpoch = todayEpoch - daysBack
                            val date = LocalDate.fromEpochDays(dayEpoch)
                            val count = activityMap[date] ?: 0

                            val cellColor = when {
                                count >= 8 -> PrimaryOrange
                                count >= 4 -> Color(0xFFFF8235)
                                count >= 1 -> Color(0xFFFF8235).copy(alpha = 0.45f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }

                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cellColor)
                            )
                        }
                    }
                }
            }

            // Legenda intensywności
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.mniej),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                listOf(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    Color(0xFFFF8235).copy(alpha = 0.45f),
                    Color(0xFFFF8235),
                    PrimaryOrange
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.wiecej),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

