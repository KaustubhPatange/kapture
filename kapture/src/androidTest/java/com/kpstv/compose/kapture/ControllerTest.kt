@file:Suppress("invisible_reference", "invisible_member")

package com.kpstv.compose.kapture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
public class ControllerTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule()

    @Test
    public fun bitmapTest() : Unit = runBlockingTest {
        var screenshotController: ScreenshotController? = null
        composeTestRule.setContent {
            val controller = rememberScreenshotController().also { screenshotController = it }
            Row(modifier = Modifier
                .semantics { contentDescription = "image-row" }
                .attachController(controller)) {
                for (i in 1..5) {
                    Icon(imageVector = Icons.Filled.AccountBox, contentDescription = "icon")
                }
            }
        }

        val result = screenshotController!!.captureToBitmap()

        // Check if image is captured
        assert(result.isSuccess)

        val bitmap = result.getOrNull()!!

        assert(bitmap.height > 0 && bitmap.width > 0)

        val imageBitmap = composeTestRule.onNodeWithContentDescription("image-row").captureToImage().asAndroidBitmap()

        // We cannot accurately compare the pixels of both image so instead
        // we will compare basic image properties

        assert(bitmap.width == imageBitmap.width)
        assert(bitmap.height == imageBitmap.height)

        assert(bitmap.density == imageBitmap.density)
        assert(bitmap.colorSpace == imageBitmap.colorSpace)
    }


    @Test
    public fun notAttachedControllerTest() : Unit = runBlockingTest {
        var screenshotController: ScreenshotController? = null
        composeTestRule.setContent {
            screenshotController = rememberScreenshotController()
        }

        val result = screenshotController!!.captureToBitmap()

        // Should fail as the controller is not attached to any modifier
        assert(result.isFailure)

        assert(result.exceptionOrNull()!!.message == "Layout coordinates are null. Did you forgot to Modifier.attachController(...) ?")
    }

    @Test
    public fun cannotAttachControllerTwiceTest() : Unit = runBlockingTest {
        var screenshotController: ScreenshotController? = null
        composeTestRule.setContent {
            val controller = rememberScreenshotController().also { screenshotController = it }
            Box(modifier = Modifier.attachController(controller))
        }

        assert(screenshotController!!.key != -1)
    }
}