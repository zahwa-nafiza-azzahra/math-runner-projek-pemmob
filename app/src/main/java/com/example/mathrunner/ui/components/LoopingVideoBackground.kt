package com.example.mathrunner.ui.components

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.view.Surface
import android.view.TextureView
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
    isPlaying: Boolean = true,
    speed: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var textureViewRef by remember { mutableStateOf<TextureView?>(null) }

    LaunchedEffect(isPlaying) {
        try {
            mediaPlayer?.let { player ->
                if (isPlaying) {
                    if (!player.isPlaying) player.start()
                } else {
                    if (player.isPlaying) player.pause()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(speed) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer?.let { player ->
                    val params = player.playbackParams
                    params.speed = speed.coerceIn(0.5f, 2.0f)
                    player.playbackParams = params
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    DisposableEffect(videoResId) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                textureViewRef = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            val textureView = TextureView(ctx)
            textureViewRef = textureView
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                    try {
                        val player = MediaPlayer().apply {
                            val afd = ctx.resources.openRawResourceFd(videoResId)
                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                            afd.close()
                            setSurface(Surface(surfaceTexture))
                            isLooping = true
                            setVolume(0f, 0f) // muted background
                            prepareAsync()
                            setOnPreparedListener { mp ->
                                if (isPlaying) mp.start()
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
