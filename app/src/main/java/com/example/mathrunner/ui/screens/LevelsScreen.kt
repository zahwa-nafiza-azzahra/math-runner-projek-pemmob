package com.example.mathrunner.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.mathrunner.R

private enum class LevelsDialog {
    EasySelected,
    MediumLocked,
    HardLocked
}

@Composable
fun LevelsScreen(
    onBackToHome: () -> Unit,
    onEasySelected: () -> Unit = {}
) {
    var activeDialog by remember { mutableStateOf<LevelsDialog?>(null) }

    BackHandler { onBackToHome() }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.easy_terbuka),
            contentDescription = stringResource(R.string.levels_screen_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val backSize = maxWidth * 0.12f
            val cardWidthFraction = 0.72f
            val cardHeight = maxHeight * 0.175f
            val cardGap = maxHeight * 0.018f
            val cardsTopPadding = maxHeight * 0.175f
            val backDescription = stringResource(R.string.levels_back_description)

            // Back button hotspot (top-left blue arrow on the mockup)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = maxWidth * 0.035f,
                        top = maxHeight * 0.028f
                    )
                    .size(backSize)
                    .semantics { contentDescription = backDescription }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onBackToHome() }
            )

            // Easy / Medium / Hard cards stacked under the LEVELS sign
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = cardsTopPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TransparentHotspot(
                    modifier = Modifier
                        .fillMaxWidth(cardWidthFraction)
                        .height(cardHeight),
                    contentDescription = stringResource(R.string.levels_easy_description)
                ) {
                    activeDialog = LevelsDialog.EasySelected
                }

                Spacer(modifier = Modifier.height(cardGap))

                TransparentHotspot(
                    modifier = Modifier
                        .fillMaxWidth(cardWidthFraction)
                        .height(cardHeight),
                    contentDescription = stringResource(R.string.levels_medium_description)
                ) {
                    activeDialog = LevelsDialog.MediumLocked
                }

                Spacer(modifier = Modifier.height(cardGap))

                TransparentHotspot(
                    modifier = Modifier
                        .fillMaxWidth(cardWidthFraction)
                        .height(cardHeight),
                    contentDescription = stringResource(R.string.levels_hard_description)
                ) {
                    activeDialog = LevelsDialog.HardLocked
                }
            }
        }

        activeDialog?.let { dialog ->
            LevelsInfoDialog(
                dialog = dialog,
                onDismiss = { activeDialog = null },
                onConfirmEasy = {
                    activeDialog = null
                    onEasySelected()
                }
            )
        }
    }
}

@Composable
private fun TransparentHotspot(
    modifier: Modifier,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    )
}

@Composable
private fun LevelsInfoDialog(
    dialog: LevelsDialog,
    onDismiss: () -> Unit,
    onConfirmEasy: () -> Unit
) {
    val title = when (dialog) {
        LevelsDialog.EasySelected -> stringResource(R.string.levels_easy_title)
        LevelsDialog.MediumLocked -> stringResource(R.string.levels_medium_title)
        LevelsDialog.HardLocked -> stringResource(R.string.levels_hard_title)
    }
    val message = when (dialog) {
        LevelsDialog.EasySelected -> stringResource(R.string.levels_easy_message)
        LevelsDialog.MediumLocked -> stringResource(R.string.levels_medium_locked_message)
        LevelsDialog.HardLocked -> stringResource(R.string.levels_hard_locked_message)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF0F172A),
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = message,
                color = Color(0xFFBAE6FD),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            if (dialog == LevelsDialog.EasySelected) {
                Button(
                    onClick = onConfirmEasy,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Text(
                        text = stringResource(R.string.levels_easy_confirm),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.close),
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            if (dialog == LevelsDialog.EasySelected) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.close),
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}
