package com.mobicom.s18.domanais.joshua.beybladextournamentmanager

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme.BeybladeXTournamentManagerTheme
import kotlin.div
import kotlin.math.min
import kotlin.text.toDouble
import kotlin.text.toFloat
import kotlin.text.toInt
import kotlin.times

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsTrackingScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tournament Metrics") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
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

            StatisticsCardRow()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Match Analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            BarChartCard()

            PieChartCard()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Blader Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            PerformanceMetricsTable()
        }
    }
}

@Composable
private fun StatisticsCardRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatisticCard("Total Matches", "24", Modifier.weight(1f))
        StatisticCard("Burst Finishes", "14", Modifier.weight(1f))
        StatisticCard("Avg. Match Time", "2:45", Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
private fun BarChartCard() {
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
                text = "Match Outcomes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val chartEntryModel = entryModelOf(0.1f, 0.4f, 0.9f, 0.5f, 0.3f)
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
                            shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(40)
                        )
                    )
                ),
                model = chartEntryModel,
                startAxis = startAxis(
                    valueFormatter = { value, _ -> "${(value * 100).toInt()}%" }
                ),
                bottomAxis = bottomAxis(
                    valueFormatter = { value, _ ->
                        listOf("Burst", "Extreme", "Spin", "Time", "Other")
                            .getOrNull(value.toInt()) ?: ""
                    }
                )
            )

            Text(
                text = "Match Finish Types",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
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
    val totalValue = pieChartData.sumOf { it.value.toDouble() }.toFloat()
    var startAngle = 0f

    Canvas(modifier = modifier) {
        val radius = min(size.width, size.height) / 2
        val center = Offset(size.width / 2, size.height / 2)

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

@Composable
private fun PieChartCard() {
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
                text = "Win Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val slices = listOf(
                PieSlice("Team A", 98f, Color(0xFF6200EA)),
                PieSlice("Team B", 1f, Color(0xFF03DAC5)),
                PieSlice("Team C", 1f, Color(0xFFFF6D00))
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                PieChart(pieChartData = slices, modifier = Modifier.fillMaxSize())
            }

            // Dynamic legend generation based on actual data values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val total = slices.sumOf { it.value.toDouble() }.toFloat()
                slices.forEach { slice ->
                    val percentage = (slice.value / total * 100).toInt()
                    PieLegendItem(
                        color = slice.color,
                        label = "${slice.label} (${percentage}%)"
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetricsTable() {
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
                listOf("Blader" to 1.5f, "Wins" to 1f, "Bursts" to 1f, "Win %" to 1f).forEach {
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

            val rows = listOf(
                Triple("Blader_ACE", "6", "75%"),
                Triple("SpinMaster", "5", "62%"),
                Triple("BurstKing", "4", "50%"),
                Triple("StadiumPro", "3", "38%"),
                Triple("LaunchMaster", "2", "25%")
            )

            rows.forEachIndexed { i, row ->
                val (name, wins, winPct) = row
                PerformanceTableRow(name, wins, (2 + i).toString(), winPct)
            }
        }
    }
}

@Composable
private fun PerformanceTableRow(
    name: String,
    wins: String,
    bursts: String,
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
        Text(bursts, Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(winPercentage, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
    Divider(thickness = 0.5.dp)
}

@Preview(showBackground = true)
@Composable
fun MetricsTrackingScreenPreview() {
    BeybladeXTournamentManagerTheme {
        MetricsTrackingScreen()
    }
}
