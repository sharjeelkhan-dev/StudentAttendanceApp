package com.attendance.app.presentation.components
import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.attendance.app.presentation.theme.LocalIsDarkMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun VerticalScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    thickness: Dp = 3.dp,
    color: Color? = null
) {
    val isDark = LocalIsDarkMode.current
    val scrollbarColor = color ?: if (isDark) {
        Color.White.copy(alpha = 0.5f)
    } else {
        Color.Black.copy(alpha = 0.3f)
    }

    // Scrollbar visibility state
    var isVisible by remember { mutableStateOf(false) }

    // Use derivedStateOf to efficiently compute scroll metrics only when layout changes.
    // This minimizes recompositions during scroll if the results don't change.
    val scrollbarMetrics by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf null

            val totalItemsCount = layoutInfo.totalItemsCount
            val viewportHeight = layoutInfo.viewportSize.height.toFloat()
            if (viewportHeight <= 0f) return@derivedStateOf null

            // Estimate total height using the average size of visible items.
            // This is more robust than using a fixed estimate for lists with variable item heights.
            val firstItem = visibleItems.first()
            val lastItem = visibleItems.last()
            val visibleHeight = lastItem.offset + lastItem.size - firstItem.offset
            val averageItemHeight = visibleHeight.toFloat() / visibleItems.size
            
            val estimatedTotalHeight = (averageItemHeight * totalItemsCount) +
                    layoutInfo.beforeContentPadding + layoutInfo.afterContentPadding

            // Hide scrollbar if content is smaller than viewport (with a small epsilon)
            if (estimatedTotalHeight <= viewportHeight + 1) return@derivedStateOf null

            // Calculate pixel-based scroll progress
            val scrolledPixels = (firstItem.index * averageItemHeight) + 
                    lazyListState.firstVisibleItemScrollOffset
            
            val scrollRange = (estimatedTotalHeight - viewportHeight).coerceAtLeast(1f)
            
            // Determine scroll percentage (0 to 1), snapping to boundaries for accuracy
            val scrollPercent = when {
                !lazyListState.canScrollBackward -> 0f
                !lazyListState.canScrollForward -> 1f
                else -> (scrolledPixels / scrollRange).coerceIn(0f, 1f)
            }
            
            // Determine handle size as a fraction of viewport (bounded for visibility)
            val visiblePercent = (viewportHeight / estimatedTotalHeight).coerceIn(0.1f, 0.9f)

            scrollPercent to visiblePercent
        }
    }

    // Handle auto-hide logic: Show on scroll/movement, hide after 1.5s of inactivity.
    // snapshotFlow is used to monitor high-frequency state changes efficiently.
    LaunchedEffect(lazyListState) {
        snapshotFlow {
            Triple(
                lazyListState.firstVisibleItemIndex,
                lazyListState.firstVisibleItemScrollOffset,
                lazyListState.isScrollInProgress
            )
        }.collectLatest {
            isVisible = true
            if (!lazyListState.isScrollInProgress) {
                delay(1500)
                isVisible = false
            }
        }
    }

    // Smooth visibility transition
    val alpha by animateFloatAsState(
        targetValue = if (isVisible && scrollbarMetrics != null) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "ScrollbarAlpha"
    )

    // Keep track of last metrics to avoid jumps or disappearing handles during fade-out
    val lastMetrics = remember { mutableStateOf<Pair<Float, Float>?>(null) }
    SideEffect {
        scrollbarMetrics?.let { lastMetrics.value = it }
    }

    val metrics = lastMetrics.value
    if (alpha > 0f && metrics != null) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxHeight()
                .width(thickness + 8.dp) // Hit area for potential interaction
                .graphicsLayer { this.alpha = alpha }, // Use graphicsLayer for efficient alpha
            contentAlignment = Alignment.TopCenter
        ) {
            val trackHeight = maxHeight
            val handleHeight = (trackHeight * metrics.second).coerceAtLeast(32.dp)
            val scrollPosition = metrics.first

            Box(
                modifier = Modifier
                    .width(thickness)
                    .height(handleHeight)
                    .graphicsLayer {
                        // Use translationY in graphicsLayer to skip layout passes during scroll
                        val maxTravel = (trackHeight - handleHeight).toPx()
                        translationY = scrollPosition * maxTravel
                    }
                    .clip(CircleShape)
                    .background(scrollbarColor)
            )
        }
    }
}

@Composable
fun VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    thickness: Dp = 3.dp,
    color: Color? = null
) {
    val isDark = LocalIsDarkMode.current
    val scrollbarColor = color ?: if (isDark) {
        Color.White.copy(alpha = 0.5f)
    } else {
        Color.Black.copy(alpha = 0.3f)
    }

    // Scrollbar visibility state
    var isVisible by remember { mutableStateOf(false) }

    // Handle auto-hide logic for ScrollState
    LaunchedEffect(scrollState) {
        snapshotFlow {
            Triple(scrollState.value, scrollState.maxValue, scrollState.isScrollInProgress)
        }.collectLatest {
            isVisible = true
            if (!scrollState.isScrollInProgress) {
                delay(1500)
                isVisible = false
            }
        }
    }

    // Smooth visibility transition
    val alpha by animateFloatAsState(
        targetValue = if (isVisible && scrollState.maxValue > 0) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "ScrollbarAlpha"
    )

    if (alpha > 0f) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxHeight()
                .width(thickness + 8.dp) // Hit area for potential interaction
                .graphicsLayer { this.alpha = alpha },
            contentAlignment = Alignment.TopCenter
        ) {
            val density = LocalDensity.current
            val scrollbarMetrics by remember(scrollState, maxHeight, density) {
                derivedStateOf {
                    val maxValue = scrollState.maxValue.toFloat()
                    if (maxValue <= 0f) return@derivedStateOf null

                    val viewportHeight = with(density) { maxHeight.toPx() }
                    if (viewportHeight <= 0f) return@derivedStateOf null

                    val totalHeight = maxValue + viewportHeight
                    val scrollPercent = (scrollState.value.toFloat() / maxValue).coerceIn(0f, 1f)
                    val visiblePercent = (viewportHeight / totalHeight).coerceIn(0.1f, 0.9f)

                    scrollPercent to visiblePercent
                }
            }

            scrollbarMetrics?.let { (scrollPercent, visiblePercent) ->
                val handleHeight = maxHeight * visiblePercent
                Box(
                    modifier = Modifier
                        .width(thickness)
                        .height(handleHeight.coerceAtLeast(32.dp))
                        .graphicsLayer {
                            val trackHeightPx = maxHeight.toPx()
                            val handleHeightPx = size.height
                            val maxTravel = trackHeightPx - handleHeightPx
                            translationY = scrollPercent * maxTravel
                        }
                        .clip(CircleShape)
                        .background(scrollbarColor)
                )
            }
        }
    }
}
