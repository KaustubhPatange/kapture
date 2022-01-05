# kapture

[![CI](https://github.com/KaustubhPatange/kapture/actions/workflows/ci.yml/badge.svg)](https://github.com/KaustubhPatange/kapture/actions/workflows/ci.yml)

A small utility library for Jetpack Compose to capture **Composable** content to Android Bitmap.

The way this work is we listen to the coordinates of the Composable through `Modifier.onGloballyPositioned` & crop the bitmap to the coordinates we get from the callback from the nearest root composable attached to `LocalView.current`.

The working is similar to [`SemanticsNodeInteraction.captureToImage()`](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/ui/ui-test/src/androidMain/kotlin/androidx/compose/ui/test/AndroidImageHelpers.android.kt;l=42) which we use for screenshot testing.

## Implementation

Check the sample in [/app](/app) directory which demonstrates the full usage of the library.

### Gradle Setup

![Maven Central](https://img.shields.io/maven-central/v/io.github.kaustubhpatange/kapture)

In your module's `build.gradle`, include the dependency.

```gradle
dependencies {
    implementation "io.github.kaustubhpatange:kapture:$version"
}
```

Check the release notes for latest version [here](CHANGELOG.md).

### Usage

- Capturing to Android Bitmap is managed by `ScreenshotController` class. You can obtain it as follows,

```kotlin
@Composable
fun TestScreen() {
    val screenshotController = rememberScreenshotController()
    ...
}
```

- Attach this controller to the Composable through a `Modifier` extension function `attachController(...)`. Through this we can capture the content for this composable including it's child heirarchy.

```kotlin
@Composable
fun TestScreen() {
    val screenshotController = rememberScreenshotController() // <--

    Column(modifier = Modifier
        .attachController(screenshotController)) {  // <--
        Text(...)
        Icon(...)
    }
}
```

- To capture the content just call `ScreenshotController.captureToBitmap()`. It is a suspending function so make sure to run it in a coroutine scope or in an implicit scope provided by `LaunchedEffect`.

```kotlin
@Composable
fun TestScreen() {
    val screenshotController = rememberScreenshotController() // <--

    Column(modifier = Modifier
        .attachController(screenshotController)) {  // <--
        Text(...)
        Icon(...)
    }

    LaunchedEffect(...) { // implicit coroutine scope
        val bitmap: Result<Bitmap> = screenshotController.captureToBitmap(
            config = Bitmap.Config.ARGB_8888 // optional
        )
        ...
    }
}
```

- The call returns a `kotlin.Result<Bitmap>` which has a bitmap if successful otherwise a throwable.
- I'll advice you to check [Tests](kapture/src/androidTest/java/com/kpstv/compose/kapture/) for some more deeper understanding.

## Contribute

If you want to contribute to this project, you're always welcome!
See [Contributing Guidelines](CONTRIBUTING.md).

## License

- [The Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)

```
Copyright 2022 Kaustubh Patange

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
