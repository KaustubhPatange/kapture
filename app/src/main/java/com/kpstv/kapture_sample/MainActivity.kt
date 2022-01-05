package com.kpstv.kapture_sample

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kpstv.compose.kapture.ScreenshotController
import com.kpstv.compose.kapture.attachController
import com.kpstv.compose.kapture.rememberScreenshotController
import com.kpstv.kapture_sample.ui.theme.Purple700
import com.kpstv.kapture_sample.ui.theme.SampleComposeTestTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleComposeTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
private fun MainContent(onBackClick: () -> Unit = {}) {
    // Don't do this in production, it is there only for sample purpose.
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val appBarScreenshotController = rememberScreenshotController()
    val coroutineScope = rememberCoroutineScope()
    Column {
        TopAppBar(
            modifier = Modifier.attachController(appBarScreenshotController),
            title = { Text(text = "AppBar") },
            backgroundColor = Purple700,
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "")
                }
            }
        )
        Column(Modifier.padding(10.dp)) {
            Button(onClick = {
                coroutineScope.launch {
                    bitmap = appBarScreenshotController.captureToBitmap().getOrNull()
                }
            }) {
                Text(text = "Capture")
            }
            Spacer(modifier = Modifier.height(30.dp))

            // Test on the local
            AndroidView(factory = { context ->
                ComposeView(context).apply {
                    setContent {
                        val localController = rememberScreenshotController()
                        DemoContent(controller = localController) { bitmap = it }
                    }
                }
            })

            // Show bitmap
            Spacer(modifier = Modifier.height(50.dp))
            Text(text = "Results")
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .border(1.dp, Color.Red, RoundedCornerShape(7.dp))
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                val btmp = bitmap
                if (btmp != null) {
                    Image(bitmap = btmp.asImageBitmap(), contentDescription = "capture")
                }
            }
        }
    }
}

@Composable
private fun DemoContent(
    modifier: Modifier = Modifier,
    controller: ScreenshotController,
    onCapture: (Bitmap?) -> Unit
) {
    Box(modifier = modifier) {

        val coroutineScope = rememberCoroutineScope()

        Column {
            Row(Modifier.attachController(controller)) {
                for (i in 1..5) {
                    Icon(Icons.Filled.AccountBox, contentDescription = "account")
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Button(onClick = {
                coroutineScope.launch {
                    onCapture(controller.captureToBitmap().getOrNull())
                }
            }) {
                Text(text = "Capture")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SampleComposeTestTheme {
        MainContent()
    }
}

