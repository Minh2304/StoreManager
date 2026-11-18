package com.storemanager.afada.ui.report

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.ui.PrinterSettingsState
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import java.io.File
import android.graphics.BitmapFactory
import android.net.Uri

/**
 * Module Hiển thị Xem trước bản in (Live Receipt Preview) & Bảng kẻ ô hóa đơn
 * Được tách riêng khỏi PrinterSettingsSection.kt để dễ bảo trì và tái sử dụng.
 */
@Composable
fun LiveReceiptPreviewCard(
    storeName: String,
    printerState: PrinterSettingsState
) {
    val isK80 = printerState.paperSize.startsWith("K80")
    val basePt = printerState.fontScale.coerceIn(9, 16)

    val titleSize = (basePt + 4).sp
    val bodySize = basePt.sp
    val totalLabelSize = (basePt + 2).sp
    val totalAmountSize = (basePt + 6).sp

    val wName = printerState.colNameWeight.coerceAtLeast(10).toFloat()
    val wQty = printerState.colQtyWeight.coerceAtLeast(8).toFloat()
    val wPrice = printerState.colPriceWeight.coerceAtLeast(10).toFloat()
    val wTotal = printerState.colTotalWeight.coerceAtLeast(10).toFloat()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF3F8)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "XEM TRƯỚC BẢN IN (${printerState.paperSize})",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AfadaBluePrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(6.dp),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth(if (isK80) 1f else 0.82f)
                    .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val headerLines = printerState.headerText
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .ifEmpty { listOf(storeName.uppercase()) }

                    headerLines.forEach { line ->
                        val isAllCapsNoDigits = isAllCapsLineWithoutDigits(line)
                        val lineFontSize = if (isAllCapsNoDigits) titleSize else bodySize
                        Text(
                            text = if (isAllCapsNoDigits) line.uppercase() else line,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isAllCapsNoDigits) FontWeight.ExtraBold else FontWeight.Normal,
                            fontSize = lineFontSize,
                            letterSpacing = (-0.6).sp,
                            lineHeight = (lineFontSize.value * 1.35f).sp,
                            textAlign = TextAlign.Center,
                            softWrap = true,
                            color = Color.Black,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // BẢNG KẺ Ô 4 CỘT (Tất cả viền và chữ đều màu ĐEN trên nền TRẮNG)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Black)
                    ) {
                        BorderedReceiptTableRow(
                            col1 = "Tên hàng",
                            col2 = "SL",
                            col3 = "Đ.Giá",
                            col4 = "T.Tiền",
                            wName = wName,
                            wQty = wQty,
                            wPrice = wPrice,
                            wTotal = wTotal,
                            fontSize = bodySize,
                            isHeader = true
                        )

                        Divider(color = Color.Black, thickness = 1.dp)

                        BorderedReceiptTableRow(
                            col1 = "Bia Hà Nội lon",
                            col2 = "1 thùng",
                            col3 = "250.000",
                            col4 = "250.000",
                            wName = wName,
                            wQty = wQty,
                            wPrice = wPrice,
                            wTotal = wTotal,
                            fontSize = bodySize,
                            isHeader = false
                        )

                        Divider(color = Color.Black, thickness = 1.dp)

                        BorderedReceiptTableRow(
                            col1 = "Bia Hà Nội lon",
                            col2 = "2 lốc",
                            col3 = "65.000",
                            col4 = "130.000",
                            wName = wName,
                            wQty = wQty,
                            wPrice = wPrice,
                            wTotal = wTotal,
                            fontSize = bodySize,
                            isHeader = false
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // TỔNG TIỀN: Bỏ chữ "đ", chỉ để lại số tiền in đậm & to hơn
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TỔNG TIỀN:",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = totalLabelSize,
                            letterSpacing = (-0.5).sp,
                            softWrap = true,
                            color = Color.Black
                        )
                        Text(
                            text = "380.000",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = totalAmountSize,
                            letterSpacing = (-0.5).sp,
                            softWrap = true,
                            color = Color.Black
                        )
                    }

                    Divider(
                        color = Color.Black,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 9.dp)
                    )

                    // CUỐI HÓA ĐƠN: Ngân hàng + STK + Chủ TK bên trái | Mã QR TO bên phải (Không bao viền)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = printerState.bankName.ifBlank { "Vietcombank" }.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = (basePt + 1).sp,
                                letterSpacing = (-0.6).sp,
                                lineHeight = ((basePt + 1) * 1.35f).sp,
                                softWrap = true,
                                color = Color.Black
                            )
                            Text(
                                text = "STK: ${printerState.bankAccount.ifBlank { "0123456789" }}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = (basePt + 1).sp,
                                letterSpacing = (-0.6).sp,
                                lineHeight = ((basePt + 1) * 1.35f).sp,
                                softWrap = true,
                                color = Color.Black
                            )
                            if (printerState.bankOwner.isNotBlank()) {
                                Text(
                                    text = printerState.bankOwner.uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = bodySize,
                                    letterSpacing = (-0.6).sp,
                                    lineHeight = (bodySize.value * 1.35f).sp,
                                    softWrap = true,
                                    color = Color.Black
                                )
                            }
                        }

                        ScannableVietQrBox(
                            bankBin = printerState.bankBin,
                            bankAccount = printerState.bankAccount,
                            amount = if (printerState.qrIncludeAmount) 380000L else null,
                            customQrUri = printerState.customQrImageUri,
                            size = if (isK80) 126.dp else 106.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Một dòng trong bảng hóa đơn kẻ ô:
 * - Tối ưu khoảng cách theo CHIỀU NGANG (`letterSpacing = (-0.6).sp`, `padding(horizontal = 3.dp)`)
 *   để hiển thị được nhiều ký tự và chữ số nhất trên một hàng.
 * - Giữ CHIỀU DỌC thoáng đãng (`lineHeight = (fontSize.value * 1.38f).sp`, `padding(vertical = 7.dp)`)
 *   để người dùng nhìn rõ ràng, không bị rối mắt.
 * - Giữ đủ 100% chữ và số (`softWrap = true`): Nếu hết chỗ ngang sẽ tự động xuống dòng.
 */
@Composable
fun BorderedReceiptTableRow(
    col1: String,
    col2: String,
    col3: String,
    col4: String,
    wName: Float,
    wQty: Float,
    wPrice: Float,
    wTotal: Float,
    fontSize: androidx.compose.ui.unit.TextUnit,
    isHeader: Boolean
) {
    val comfortableLineHeight = (fontSize.value * 1.38f).sp
    val tightHorizontalSpacing = (-0.6).sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = col1,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isHeader) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = fontSize,
            letterSpacing = tightHorizontalSpacing,
            lineHeight = comfortableLineHeight,
            softWrap = true,
            color = Color.Black,
            modifier = Modifier
                .weight(wName)
                .padding(horizontal = 3.dp, vertical = 7.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.Black)
        )

        Text(
            text = col2,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isHeader) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = fontSize,
            letterSpacing = tightHorizontalSpacing,
            lineHeight = comfortableLineHeight,
            textAlign = TextAlign.Center,
            softWrap = true,
            color = Color.Black,
            modifier = Modifier
                .weight(wQty)
                .padding(horizontal = 2.dp, vertical = 7.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.Black)
        )

        Text(
            text = col3,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isHeader) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = fontSize,
            letterSpacing = tightHorizontalSpacing,
            lineHeight = comfortableLineHeight,
            textAlign = TextAlign.End,
            softWrap = true,
            color = Color.Black,
            modifier = Modifier
                .weight(wPrice)
                .padding(horizontal = 3.dp, vertical = 7.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.Black)
        )

        Text(
            text = col4,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isHeader) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = fontSize,
            letterSpacing = tightHorizontalSpacing,
            lineHeight = comfortableLineHeight,
            textAlign = TextAlign.End,
            softWrap = true,
            color = Color.Black,
            modifier = Modifier
                .weight(wTotal)
                .padding(horizontal = 3.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun ScannableVietQrBox(
    bankBin: String,
    bankAccount: String,
    amount: Long?,
    customQrUri: String,
    size: Dp
) {
    val context = LocalContext.current

    val customBitmap = remember(customQrUri) {
        if (customQrUri.isNotBlank()) {
            try {
                when {
                    customQrUri.startsWith("content://") || customQrUri.startsWith("file://") -> {
                        context.contentResolver.openInputStream(Uri.parse(customQrUri))?.use {
                            BitmapFactory.decodeStream(it)?.asImageBitmap()
                        }
                    }
                    customQrUri.startsWith("/") -> {
                        val file = File(customQrUri)
                        if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
                    }
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val emvPayload = remember(bankBin, bankAccount, amount) {
        VietQrGenerator.buildEmvCoVietQrPayload(
            bankBin = bankBin,
            accountNumber = bankAccount,
            amount = amount
        )
    }

    val generatedQrBitmap = remember(emvPayload) {
        VietQrGenerator.generateQrBitmap(emvPayload, sizePx = 512)?.asImageBitmap()
    }

    Box(
        modifier = Modifier
            .size(size)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val bitmapToDraw = customBitmap ?: generatedQrBitmap
        if (bitmapToDraw != null) {
            Image(
                bitmap = bitmapToDraw,
                contentDescription = "Mã QR Ngân hàng",
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

fun isAllCapsLineWithoutDigits(line: String): Boolean {
    val hasDigit = line.any { it.isDigit() }
    if (hasDigit) return false
    val letters = line.filter { it.isLetter() }
    if (letters.isEmpty()) return false
    return letters.all { it.isUpperCase() }
}
