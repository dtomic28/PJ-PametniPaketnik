import android.content.Context
import android.util.Log
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun Custom_CameraPreview(
    modifier: Modifier = Modifier,
    imageCapture: MutableState<ImageCapture?>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        val capture = ImageCapture.Builder().build()
        imageCapture.value = capture

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                capture
            )
        } catch (e: Exception) {
            Log.e("CameraPreview", "Camera binding failed", e)
        }

    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )

}

suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            cont.resume(future.get())
        }, ContextCompat.getMainExecutor(this))
    }
