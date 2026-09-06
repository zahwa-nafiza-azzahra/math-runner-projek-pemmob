package com.example.mathrunner.ui.components

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun LoopingVideoBackground(
    @RawRes videoResId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(videoResId) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            val textureView = TextureView(ctx)
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                    try {
                        val player = MediaPlayer().apply {
                            val afd = ctx.resources.openRawResourceFd(videoResId)
                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                            setSurface(Surface(surfaceTexture))
                            isLooping = true
                            setVolume(0f, 0f) // muted for smooth background ambiance
                            prepareAsync()
                            setOnPreparedListener { mp ->
                                mp.start()
                                adjustAspectRatio(textureView, mp.videoWidth, mp.videoHeight, width, height)
                            }
                        }
                        mediaPlayer = player
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                    mediaPlayer?.let { mp ->
                        adjustAspectRatio(textureView, mp.videoWidth, mp.videoHeight, width, height)
                    }
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    try {
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return true
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }
            textureView
        },
        modifier = modifier
    )
}

private fun adjustAspectRatio(textureView: TextureView, videoWidth: Int, videoHeight: Int, viewWidth: Int, viewHeight: Int) {
    if (videoWidth == 0 || videoHeight == 0 || viewWidth == 0 || viewHeight == 0) return
    val viewRatio = viewWidth.toFloat() / viewHeight
    val videoRatio = videoWidth.toFloat() / videoHeight
    val scaleX: Float
    val scaleY: Float

    if (viewRatio > videoRatio) {
        scaleX = 1f
        scaleY = (viewWidth.toFloat() / videoWidth * videoHeight) / viewHeight
    } else {
        scaleX = (viewHeight.toFloat() / videoHeight * videoWidth) / viewWidth
        scaleY = 1f
    }

    val matrix = Matrix().apply {
        setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
    }
    textureView.setTransform(matrix)
}
