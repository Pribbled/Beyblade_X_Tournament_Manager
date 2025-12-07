package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.data.Match
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlin.math.min
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsTab(
    matches: List<Match> = emptyList()
) {
    val completedMatches = matches.filter { it.status == "completed" }
    val totalMatches = completedMatches.size
    val totalBursts = completedMatches.sumOf { it.burstFinishes.toDouble() }.toFloat()

    // Calculate Average Time
    val totalSeconds = completedMatches.sumOf { it.elapsedSeconds }
    val avgSeconds = if (totalMatches > 0) totalSeconds / totalMatches else 0
    val avgTimeStr = String.format("%d:%02d",
        TimeUnit.SECONDS.toMinutes(avgSeconds),
        avgSeconds % 60
    )

    val countExtreme = completedMatches.sumOf { it.extremeFinishes.toDouble() }.toFloat()
    val countBurst = completedMatches.sumOf { it.burstFinishes.toDouble() }.toFloat()
    val countOver = completedMatches.sumOf { it.overFinishes.toDouble() }.toFloat()
    val countSpin = completedMatches.sumOf { it.spinFinishes.toDouble() }.toFloat()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tournament Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            StatisticsCardRow(
                totalMatches = totalMatches.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Match Analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            if (totalMatches > 0) {
                PieChartCard(completedMatches)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Blader Performance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                PerformanceMetricsTable(completedMatches)
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Finish matches to see analytics", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsCardRow(
    totalMatches: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatisticCard("Total Matches", totalMatches, Modifier.weight(1f))
    }
}

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BarChartCard(
    burst: Float,
    extreme: Float,
    spin: Float,
    over: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Finish Types",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val chartEntryModel = entryModelOf(
                burst,
                extreme,
                spin,
                over
            )
            val primaryColor = MaterialTheme.colorScheme.primary

            Chart(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                chart = columnChart(
                    columns = listOf(
                        LineComponent(
                            color = primaryColor.toArgb(),
                            thicknessDp = 12f,
                            shape = Shapes.roundedCornerShape(40)
                        )
                    )
                ),
                model = chartEntryModel,
                startAxis = startAxis(
                    valueFormatter = { value, _ -> value.toInt().toString() }
                ),
                bottomAxis = bottomAxis(
                    valueFormatter = { value, _ ->
                        listOf("Burst", "Extreme", "Spin", "Over")
                            .getOrNull(value.toInt()) ?: ""
                    }
                )
            )
        }
    }
}

@Composable
private fun PieLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

data class PieSlice(val label: String, val value: Float, val color: Color)

@Composable
fun PieChart(
    pieChartData: List<PieSlice>,
    modifier: Modifier = Modifier
) {
    val totalValue = pieChartData.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    var startAngle = 0f

    Canvas(modifier = modifier) {
        val radius = min(size.width, size.height) / 2
        val center = Offset(size.width / 2, size.height / 2)

        if (pieChartData.isEmpty() || totalValue <= 0f) {
            drawCircle(Color.LightGray, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
        } else {
            pieChartData.forEach { slice ->
                val sweepAngle = 360 * (slice.value / totalValue)
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = center - Offset(radius, radius),
                    size = Size(radius * 2, radius * 2)
                )
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
private fun PieChartCard(matches: List<Match>) {
    // Calculate Wins per Player
    val winsMap = mutableMapOf<String, Int>()
    matches.forEach { match ->
        if (!match.winnerName.isNullOrBlank()) {
            winsMap[match.winnerName] = winsMap.getOrDefault(match.winnerName, 0) + 1
        }
    }

    // Take top 3 winners
    val topWinners = winsMap.toList()
        .sortedByDescending { it.second }
        .take(3)

    val colors = listOf(Color(0xFF6200EA), Color(0xFF03DAC5), Color(0xFFFF6D00))

    val slices = topWinners.mapIndexed { index, (name, wins) ->
        PieSlice(name, wins.toFloat(), colors.getOrElse(index) { Color.Gray })
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Top Winners",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (slices.isEmpty()) {
                Text("No winners yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            } else {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PieChart(pieChartData = slices, modifier = Modifier.fillMaxSize())
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val total = slices.sumOf { it.value.toDouble() }.toFloat()
                    slices.forEach { slice ->
                        val percentage = if(total > 0) (slice.value / total * 100).toInt() else 0
                        PieLegendItem(
                            color = slice.color,
                            label = "${slice.label} ($percentage%)"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetricsTable(matches: List<Match>) {
    // Aggregate stats by Player Name
    val playerStats = mutableMapOf<String, Triple<Int, Int, Int>>() // Wins, Bursts, Played

    matches.forEach { match ->
        // Only count valid player names
        if (match.player1Name.isNotBlank()) {
            val p1Stats = playerStats.getOrDefault(match.player1Name, Triple(0, 0, 0))
            val isWin = match.winnerId == match.player1Id
            playerStats[match.player1Name] = Triple(
                p1Stats.first + (if (isWin) 1 else 0),
                0,
                p1Stats.third + 1
            )
        }
        if (match.player2Name.isNotBlank()) {
            val p2Stats = playerStats.getOrDefault(match.player2Name, Triple(0, 0, 0))
            val isWin = match.winnerId == match.player2Id
            playerStats[match.player2Name] = Triple(
                p2Stats.first + (if (isWin) 1 else 0),
                0,
                p2Stats.third + 1
            )
        }
    }

    val sortedStats = playerStats.entries.sortedByDescending { it.value.first } // Sort by wins

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Blader" to 1.5f, "Wins" to 1f, "Matches" to 1f, "Win %" to 1f).forEach {
                    Text(
                        text = it.first,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(it.second),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Divider()

            if (sortedStats.isEmpty()) {
                Text("No data available.", modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center)
            } else {
                sortedStats.take(5).forEach { (name, stats) ->
                    val (wins, _, played) = stats
                    val winPct = if (played > 0) (wins.toFloat() / played * 100).toInt() else 0
                    PerformanceTableRow(name, wins.toString(), played.toString(), "$winPct%")
                }
            }
        }
    }
}

@Composable
private fun PerformanceTableRow(
    name: String,
    wins: String,
    matches: String,
    winPercentage: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, Modifier.weight(1.5f))
        Text(wins, Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(matches, Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(winPercentage, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
    Divider(thickness = 0.5.dp)
}

@Preview(showBackground = true)
@Composable
fun MetricsTrackingScreenPreview() {
    BeybladeXTournamentManagerTheme {
        MetricsTab()
    }
}