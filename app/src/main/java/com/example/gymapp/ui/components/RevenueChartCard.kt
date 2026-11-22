package com.example.gymapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapp.util.DateUtils
import com.example.gymapp.viewmodel.RevenueUiState
import com.example.ui.theme.CoralFlame
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidContainer
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape

@Composable
fun RevenueChartCard(
    revenueUiState: RevenueUiState,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    val modelProducer = remember { CartesianChartModelProducer() }
    val monthLabels = remember(revenueUiState.months) {
        revenueUiState.months.map { it.monthLabel }
    }

    LaunchedEffect(revenueUiState.months) {
        if (revenueUiState.months.isNotEmpty()) {
            val values = revenueUiState.months.map { it.totalAmount }
            modelProducer.runTransaction {
                columnSeries { series(values) }
                lineSeries { series(values) }
            }
        }
    }

    val bottomAxisValueFormatter = remember(monthLabels) {
        CartesianValueFormatter { _, x, _ ->
            monthLabels.getOrElse(x.toInt()) { "" }
        }
    }

    val startAxisValueFormatter = remember {
        CartesianValueFormatter { _, y, _ ->
            DateUtils.formatCurrency(y)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("revenue_chart_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded }
                    .padding(vertical = 2.dp)
                    .testTag("revenue_card_toggle"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricTeal.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = ElectricTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Monthly Collections (6M)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total: ${DateUtils.formatCurrency(revenueUiState.sixMonthTotal)} • This month: ${DateUtils.formatCurrency(revenueUiState.currentMonthTotal)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse chart" else "Expand chart",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val diff = revenueUiState.currentMonthTotal - revenueUiState.previousMonthTotal
                    val trendText = when {
                        revenueUiState.sixMonthTotal == 0.0 -> "No collections logged yet"
                        diff > 0 -> "+${DateUtils.formatCurrency(diff)} vs last month"
                        diff < 0 -> "-${DateUtils.formatCurrency(-diff)} vs last month"
                        else -> "Same as last month"
                    }
                    Surface(
                        color = if (diff >= 0 && revenueUiState.sixMonthTotal > 0.0) {
                            StatusPaidContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = trendText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (diff >= 0 && revenueUiState.sixMonthTotal > 0.0) {
                                StatusPaid
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (revenueUiState.months.isNotEmpty()) {
                        ProvideVicoTheme(rememberM3VicoTheme()) {
                            CartesianChartHost(
                                chart = rememberCartesianChart(
                                    rememberColumnCartesianLayer(
                                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                            rememberLineComponent(
                                                fill = fill(CoralFlame.copy(alpha = 0.85f)),
                                                thickness = 18.dp,
                                                shape = CorneredShape.rounded(
                                                    topLeftPercent = 30,
                                                    topRightPercent = 30
                                                )
                                            )
                                        )
                                    ),
                                    rememberLineCartesianLayer(
                                        lineProvider = LineCartesianLayer.LineProvider.series(
                                            LineCartesianLayer.rememberLine(
                                                fill = LineCartesianLayer.LineFill.single(
                                                    fill(ElectricTeal)
                                                )
                                            )
                                        )
                                    ),
                                    startAxis = VerticalAxis.rememberStart(
                                        valueFormatter = startAxisValueFormatter
                                    ),
                                    bottomAxis = HorizontalAxis.rememberBottom(
                                        valueFormatter = bottomAxisValueFormatter
                                    )
                                ),
                                modelProducer = modelProducer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp)
                                    .testTag("revenue_vico_chart")
                            )
                        }
                    }
                }
            }
        }
    }
}
