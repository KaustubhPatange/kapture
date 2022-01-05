package com.kpstv.compose.kapture

import android.graphics.Bitmap
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * Attach the controller to the composable for which you want to capture the image.
 *
 * Note: The call to `rememberScreenshotController()` and `attachController(...)`
 * should belong in the same hierarchy i.e they should belong to the same root
 * composable otherwise the result image will not produce correct results.
 *
 * @throws IllegalStateException If tried to attach the controller more than once
 *  to a different [Composable].
 */
public fun Modifier.attachController(
    screenshotController: ScreenshotController
): Modifier = composed {
    screenshotController.key = currentCompositeKeyHash
    this.then(onGloballyPositioned { screenshotController.coordinates = it })
}

/**
 * Create & remember the instance of [ScreenshotController].
 */
@Composable
public fun rememberScreenshotController(): ScreenshotController {
    val localView = LocalView.current
    return remember { ScreenshotController(localView) }
}

/**
 * Controller for capturing [Composable] content.
 */
public class ScreenshotController internal constructor(private val localView: View) {
    internal var coordinates: LayoutCoordinates? = null
    internal var key: Int = -1
        set(value) {
            if (field == -1) {
                field = value
                return
            }
            if (field == value) return

            throw IllegalStateException("Cannot attach the controller twice.")
        }

    /**
     * Capture the bitmap with the specified [config].
     *
     * The method returns [kotlin.Result] containing the [Bitmap] if the capture was successful
     * or [Exception] if there were any failures.
     *
     * @param config Bitmap config of the desired bitmap. Defaults to [Bitmap.Config.ARGB_8888]
     */
    public suspend fun captureToBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Result<Bitmap> = withContext(coroutineContext) cap@{
        val coordinates = coordinates ?: return@cap Result.failure(IllegalStateException("Layout coordinates are null. Did you forgot to Modifier.attachController(...) ?"))
        val whole = try {
            localView.drawToBitmap(config)
        } catch (e: IllegalStateException) {
            return@cap Result.failure(e)
        }

        val bounds = coordinates.boundsInRoot()
        val rowPixels = whole.width - bounds.left.toInt()
        val columnPixels = whole.height - bounds.top.toInt()

        val finalWidth = minOf(bounds.width.toInt(), rowPixels)
        val finalHeight = minOf(bounds.height.toInt(), columnPixels)

        val final = Bitmap.createBitmap(whole, bounds.left.toInt(), bounds.top.toInt(), finalWidth, finalHeight)
        whole.recycle() // manually recycle
        return@cap Result.success(final)
    }
}