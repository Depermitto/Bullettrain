package io.github.depermitto.bullettrain.exercises

import android.text.Layout
import android.text.SpannableStringBuilder
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.fixed
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shadow
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.compose.common.shape.markerCorneredShape
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import io.github.depermitto.bullettrain.db.HistoryDao
import io.github.depermitto.bullettrain.protos.ExercisesProto.Exercise
import io.github.depermitto.bullettrain.protos.SettingsProto.Settings
import io.github.depermitto.bullettrain.theme.Large
import io.github.depermitto.bullettrain.theme.Medium
import io.github.depermitto.bullettrain.util.DateFormatters
import io.github.depermitto.bullettrain.util.lastCompletedSet
import io.github.depermitto.bullettrain.util.splitOnUppercase
import io.github.depermitto.bullettrain.util.toInstant
import io.github.depermitto.bullettrain.util.toLocalDate
import java.time.ZoneId
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun ExerciseScreen(
  modifier: Modifier = Modifier,
  historyDao: HistoryDao,
  exerciseDescriptor: Exercise.Descriptor,
  settings: Settings,
) {
  val exercises by
    historyDao
      .map { records ->
        records
          .sortedBy { r -> r.workoutStartTs.seconds }
          .flatMap { r ->
            r.workout.exercisesList.filter { e -> e.descriptorId == exerciseDescriptor.id }
          }
          .filter { e -> e.setsList.any { s -> s.hasDoneTs() } }
      }
      .collectAsStateWithLifecycle(initialValue = emptyList())

  Column(modifier = modifier) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    // TODO add gestures to swipe between tabs
    TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.Transparent) {
      Tab(
        selected = selectedTabIndex == 0,
        onClick = { selectedTabIndex = 0 },
        modifier = Modifier.height(40.dp),
      ) {
        Text("History")
      }
      Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) { Text("Graph") }
    }

    Spacer(Modifier.height(Dp.Medium))

    val lazyListState = rememberLazyListState()
    if (selectedTabIndex == 0) {
      ExercisesSetsListings(
        exercises = exercises.asReversed(),
        exerciseHeadline = { exercise ->
          val start = exercise.setsList.first().doneTs.toInstant().atZone(ZoneId.systemDefault())
          val startDateText = DateFormatters.EEEE_MMMM_d_yyyy.format(start.toLocalDate())
          val startTimeText = DateFormatters.kk_mm.format(start.toLocalTime())
          Text("$startDateText ($startTimeText)", maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        settings = settings,
        lazyListState = lazyListState,
      )
    }

    var selectedPeriod by remember { mutableStateOf(Period.Yearly) }
    var selectedMetric by remember { mutableStateOf(Metric.OneRepMax) }

    if (selectedTabIndex == 1 && exercises.isNotEmpty()) {
      Column(
        modifier = Modifier.padding(horizontal = Dp.Medium),
        verticalArrangement = Arrangement.spacedBy(Dp.Large),
      ) {
        val (horizontalAxisData, markerData, zoomState) =
          remember(selectedPeriod, key2 = exercises) {
            val dates = exercises.map { e -> e.lastCompletedSet!!.doneTs.toLocalDate() }
            Triple(
              when (selectedPeriod) {
                Period.Weekly -> dates.map { DateFormatters.MMM_d_yyyy.format(it) }
                Period.Monthly -> dates.map { DateFormatters.MMM_yyyy.format(it) }
                Period.Yearly -> dates.map { DateFormatters.yyyy.format(it) }
              },
              dates.map { DateFormatters.MMMM_d_yyyy.format(it) },
              when (selectedPeriod) {
                Period.Weekly -> Zoom.max(Zoom.fixed(2F), Zoom.Content)
                Period.Monthly -> Zoom.max(Zoom.fixed(1F), Zoom.Content)
                Period.Yearly -> Zoom.Content
              },
            )
          }

        val values: List<Double> =
          remember(selectedMetric) {
            when (selectedMetric) {
              Metric.OneRepMax -> exercises.map { e -> e.setsList.maxOf { s -> oneRepMax(s) } }
              Metric.Volume -> exercises.map { e -> volume(e.setsList) }
              Metric.BestWeight -> exercises.map { e -> bestWeight(e.setsList) }
              Metric.BestReps -> exercises.map { e -> bestReps(e.setsList) }
            }
          }
        val modelProducer = remember { CartesianChartModelProducer() }
        LaunchedEffect(values) { modelProducer.runTransaction { lineSeries { series(values) } } }

        val lineColor = MaterialTheme.colorScheme.primary
        CartesianChartHost(
          modifier = Modifier.height(250.dp),
          modelProducer = modelProducer,
          zoomState = rememberVicoZoomState(initialZoom = zoomState),
          chart =
            rememberCartesianChart(
              rememberLineCartesianLayer(
                lineProvider =
                  LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                      fill = LineCartesianLayer.LineFill.single(fill(lineColor)),
                      areaFill =
                        LineCartesianLayer.AreaFill.single(
                          fill(
                            ShaderProvider.verticalGradient(
                              arrayOf(lineColor.copy(alpha = 0.3f), Color.Transparent)
                            )
                          )
                        ),
                    )
                  ),
                rangeProvider =
                  CartesianLayerRangeProvider.fixed(
                    minY = floor(values.min()),
                    maxY = ceil(values.max()),
                  ),
              ),
              startAxis = VerticalAxis.rememberStart(),
              bottomAxis =
                HorizontalAxis.rememberBottom(
                  guideline = null,
                  itemPlacer = rememberCustomItemPlacer(horizontalAxisData),
                  valueFormatter = { _, pos, _ -> horizontalAxisData[pos.toInt()] },
                ),
              marker = rememberCustomCartesianMarker(markerData),
            ),
        )

        var showPeriodDropdown by remember { mutableStateOf(false) }
        Column(modifier = Modifier.clickable { showPeriodDropdown = true }) {
          Text(
            "Period",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6F),
          )

          Text(selectedPeriod.name)

          DropdownMenu(
            expanded = showPeriodDropdown,
            onDismissRequest = { showPeriodDropdown = false },
          ) {
            for (period in Period.entries) {
              DropdownMenuItem(
                text = { Text(period.name) },
                onClick = {
                  showPeriodDropdown = false
                  selectedPeriod = period
                },
              )
            }
          }
          HorizontalDivider()
        }

        var showMetricDropdown by remember { mutableStateOf(false) }
        Column(modifier = Modifier.clickable { showMetricDropdown = true }) {
          Text(
            "Metric",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6F),
          )

          Text(selectedMetric.name.splitOnUppercase())

          DropdownMenu(
            expanded = showMetricDropdown,
            onDismissRequest = { showMetricDropdown = false },
          ) {
            for (metric in Metric.entries) {
              DropdownMenuItem(
                text = { Text(metric.name.splitOnUppercase()) },
                onClick = {
                  showMetricDropdown = false
                  selectedMetric = metric
                },
              )
            }
          }
          HorizontalDivider()
        }
      }
    }
  }
}

private enum class Period {
  Weekly,
  Monthly,
  Yearly,
}

private enum class Metric {
  OneRepMax,
  Volume,
  BestWeight,
  BestReps,
}

fun oneRepMax(set: Exercise.Set): Double = set.weight * (1 + set.actual / 30.0)

fun volume(sets: Iterable<Exercise.Set>): Double =
  sets.sumOf { s -> (s.actual * s.weight).toDouble() }

fun bestWeight(sets: Iterable<Exercise.Set>): Double = sets.maxOf { s -> s.weight.toDouble() }

fun bestReps(sets: Iterable<Exercise.Set>): Double = sets.maxOf { s -> s.actual.toDouble() }

@Composable
private fun rememberCustomItemPlacer(data: List<CharSequence>): HorizontalAxis.ItemPlacer {
  val itemPlacer = remember { HorizontalAxis.ItemPlacer.aligned() }
  return remember(data, itemPlacer) {
    object : HorizontalAxis.ItemPlacer {
      override fun getLabelValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
      ): List<Double> {
        val indices = itemPlacer.getLabelValues(context, visibleXRange, fullXRange, maxLabelWidth)
        return indices.map { data[it.toInt()] }.distinct().map { data.indexOf(it).toDouble() }
      }

      override fun getStartLayerMargin(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        tickThickness: Float,
        maxLabelWidth: Float,
      ): Float =
        itemPlacer.getStartLayerMargin(context, layerDimensions, tickThickness, maxLabelWidth)

      override fun getEndLayerMargin(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        tickThickness: Float,
        maxLabelWidth: Float,
      ): Float =
        itemPlacer.getEndLayerMargin(context, layerDimensions, tickThickness, maxLabelWidth)

      override fun getHeightMeasurementLabelValues(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
      ): List<Double> =
        itemPlacer.getHeightMeasurementLabelValues(
          context,
          layerDimensions,
          fullXRange,
          maxLabelWidth,
        )

      override fun getWidthMeasurementLabelValues(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        fullXRange: ClosedFloatingPointRange<Double>,
      ): List<Double> =
        itemPlacer.getWidthMeasurementLabelValues(context, layerDimensions, fullXRange)

      override fun getFirstLabelValue(
        context: CartesianMeasuringContext,
        maxLabelWidth: Float,
      ): Double? = itemPlacer.getFirstLabelValue(context, maxLabelWidth)

      override fun getLastLabelValue(
        context: CartesianMeasuringContext,
        maxLabelWidth: Float,
      ): Double? = itemPlacer.getFirstLabelValue(context, maxLabelWidth)
    }
  }
}

@Composable
private fun rememberCustomCartesianMarker(data: List<CharSequence>): CartesianMarker {
  val labelBackgroundShape = markerCorneredShape(CorneredShape.Corner.Rounded)
  val labelBackground =
    rememberShapeComponent(
      fill = fill(MaterialTheme.colorScheme.surfaceContainer),
      shape = labelBackgroundShape,
      shadow = shadow(radius = 4.dp, y = 2.dp),
    )
  val label =
    rememberTextComponent(
      color = MaterialTheme.colorScheme.onSurface,
      textAlignment = Layout.Alignment.ALIGN_CENTER,
      padding = insets(8.dp, 4.dp),
      background = labelBackground,
      minWidth = TextComponent.MinWidth.fixed(40.dp),
    )
  val indicatorFrontComponent =
    rememberShapeComponent(fill(MaterialTheme.colorScheme.surface), CorneredShape.Pill)
  val indicatorRearComponent = rememberShapeComponent(shape = CorneredShape.Pill)
  val indicator =
    LayeredComponent(
      back = indicatorRearComponent,
      front = indicatorFrontComponent,
      padding = insets(5.dp),
    )
  val guideline = rememberAxisGuidelineComponent()
  val formatter = remember { DefaultCartesianMarker.ValueFormatter.default() }
  return remember(label, indicator, guideline, formatter) {
    object :
      DefaultCartesianMarker(
        label = label,
        valueFormatter =
          ValueFormatter { context, targets ->
            SpannableStringBuilder().apply {
              append(formatter.format(context, targets))
              if (targets.isNotEmpty()) {
                append(", ")
                append(data[targets[0].x.toInt()])
              }
            }
          },
        indicator = { color ->
          LayeredComponent(
            back =
              ShapeComponent(
                fill = Fill(color),
                shape = CorneredShape.Pill,
                shadow = Shadow(radiusDp = 6F, color = color),
              ),
            front = indicatorFrontComponent,
            padding = insets(5.dp),
          )
        },
        indicatorSizeDp = 14F,
        guideline = guideline,
      ) {
      override fun updateLayerMargins(
        context: CartesianMeasuringContext,
        layerMargins: CartesianLayerMargins,
        layerDimensions: CartesianLayerDimensions,
        model: CartesianChartModel,
      ) {
        with(context) {
          val baseShadowMarginDp = 1.4F * 4
          var topMargin = (baseShadowMarginDp - 2).pixels
          var bottomMargin = (baseShadowMarginDp + 2).pixels
          when (labelPosition) {
            LabelPosition.Top,
            LabelPosition.AbovePoint -> topMargin += label.getHeight(context) + tickSizeDp.pixels
            LabelPosition.Bottom -> bottomMargin += label.getHeight(context) + tickSizeDp.pixels
            LabelPosition.AroundPoint -> {}
          }
          layerMargins.ensureValuesAtLeast(top = topMargin, bottom = bottomMargin)
        }
      }
    }
  }
}
