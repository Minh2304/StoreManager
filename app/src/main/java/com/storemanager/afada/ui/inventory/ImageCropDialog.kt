package com.storemanager.afada.ui.inventory

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Hộp thoại Cắt & Thu phóng ảnh (Crop Dialog) đúng tỷ lệ vuông 1:1 hiển thị trên thẻ món hàng
 * Người dùng dùng 1 hoặc 2 ngón tay để kéo di chuyển và phóng to / thu nhỏ ảnh trong khung vuông.
 */
@Composable
fun ImageCropDialog(
    sourceUri: Uri,
    onCropSaved: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val cropBoxSizeDp = 260.dp
    val cropBoxSizePx = with(density) { cropBoxSizeDp.toPx() }

    val sourceBitmap: Bitmap? = remember(sourceUri) {
        try {
            context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    if (sourceBitmap == null) {
        onDismiss()
        return
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Chỉnh sửa ảnh",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1C1E)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Khung cắt ảnh tỷ lệ 1:1 đúng chuẩn hiển thị trên thẻ món hàng
                Box(
                    modifier = Modifier
                        .size(cropBoxSizeDp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF111827))
                        .border(2.dp, AfadaBluePrimary, RoundedCornerShape(14.dp))
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 5f)
                                val maxOffset = (cropBoxSizePx * (newScale - 1f)) / 2f + cropBoxSizePx * 0.35f
                                scale = newScale
                                offsetX = (offsetX + pan.x).coerceIn(-maxOffset, maxOffset)
                                offsetY = (offsetY + pan.y).coerceIn(-maxOffset, maxOffset)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = sourceBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }

                    Button(
                        onClick = {
                            val savedPath = renderAndSaveCroppedSquareBitmap(
                                context = context,
                                bitmap = sourceBitmap,
                                viewSizePx = cropBoxSizePx,
                                userScale = scale,
                                offsetX = offsetX,
                                offsetY = offsetY
                            )
                            onCropSaved(savedPath)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Xong", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

private fun renderAndSaveCroppedSquareBitmap(
    context: Context,
    bitmap: Bitmap,
    viewSizePx: Float,
    userScale: Float,
    offsetX: Float,
    offsetY: Float
): String {
    val outputSize = 512
    val outBitmap = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outBitmap)

    val ratio = outputSize / viewSizePx
    val baseScale = max(
        outputSize.toFloat() / bitmap.width.toFloat(),
        outputSize.toFloat() / bitmap.height.toFloat()
    )

    val matrix = Matrix().apply {
        postTranslate(-bitmap.width / 2f, -bitmap.height / 2f)
        postScale(baseScale * userScale, baseScale * userScale)
        postTranslate(
            outputSize / 2f + offsetX * ratio,
            outputSize / 2f + offsetY * ratio
        )
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    canvas.drawColor(android.graphics.Color.WHITE)
    canvas.drawBitmap(bitmap, matrix, paint)

    val file = File(context.filesDir, "product_img_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        outBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file.absolutePath
}

