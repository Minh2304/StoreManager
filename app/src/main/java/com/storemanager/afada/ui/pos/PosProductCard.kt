package com.storemanager.afada.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.core.designsystem.AfadaProductThumbnail
import com.storemanager.afada.data.local.PosSellableUnitItem
import com.storemanager.afada.ui.FormatUtils
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaOrangeAccent
import com.storemanager.afada.ui.theme.AfadaRedDebt

/**
 * Thẻ mặt hàng trên màn hình Tạo đơn (POS):
 * - Label Thùng / Lốc / Két nằm ở góc trên bên phải của thẻ.
 * - Giá tiền hiển thị thoáng đãng bằng màu XANH LÁ (AfadaGreenSuccess).
 * - Cụm nút (-) [ 0 ] (+) đồng bộ nền trắng nhạt tinh tế.
 * - Nhấn vào ô số lượng [ 0 ] -> Bàn phím số Numpad trượt từ dưới lên (ModalBottomSheet):
 *   + Nhấn Xác nhận, chạm ra ngoài hoặc vuốt xuống / Back đều tự động lưu số lượng.
 *   + Chỉ nhấn Hủy mới không lưu.
 */
@Composable
fun PosSellableUnitCard(
    sellableItem: PosSellableUnitItem,
    qtyInCart: Int,
    manageStockEnabled: Boolean,
    onQuantityChange: (Int) -> Unit
) {
    var showNumpadSheet by remember { mutableStateOf(false) }

    val product = sellableItem.product
    val unit = sellableItem.unit

    val subtleBtnBg = Color(0xFFF5F7FA)
    val subtleBorder = Color(0xFFE0E5EC)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Ảnh sản phẩm TO (94dp), nằm sát viền bên trái của thẻ món hàng
                AfadaProductThumbnail(
                    imageUri = product.imageUri,
                    size = 94.dp,
                    cornerRadius = 0.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Tên món + Giá bán màu xanh lá thoáng đãng + Cụm nút - [ 0 ] +
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 10.dp, bottom = 10.dp, end = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Tên món hàng (chừa khoảng trống bên phải cho nhãn Thùng/Lốc ở góc trên phải)
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1A1C1E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = 64.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hàng dưới: Giá tiền màu Xanh Lá bên trái & Cụm - [ 0 ] + bên phải
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = FormatUtils.formatVnd(unit.sellPrice),
                                color = AfadaGreenSuccess,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            if (manageStockEnabled) {
                                Text(
                                    text = "Tồn: ${sellableItem.formattedStockText}",
                                    fontSize = 12.sp,
                                    color = if (product.stockBaseQty <= product.minStockAlert) AfadaRedDebt else Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Cụm điều chỉnh số lượng:  -  [ 0 ]  +
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Nút trừ (-)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(subtleBtnBg)
                                    .border(1.dp, subtleBorder, RoundedCornerShape(9.dp))
                                    .clickable { onQuantityChange((qtyInCart - 1).coerceAtLeast(0)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Giảm",
                                    tint = Color(0xFF374151),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Ô số lượng [ 0 ] (Nhấn vào trượt bàn phím số từ dưới lên)
                            Box(
                                modifier = Modifier
                                    .width(42.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (qtyInCart > 0) Color(0xFFF0F6FF) else Color(0xFFFAFBFD))
                                    .border(
                                        width = 1.dp,
                                        color = if (qtyInCart > 0) AfadaBluePrimary.copy(alpha = 0.5f) else subtleBorder,
                                        shape = RoundedCornerShape(9.dp)
                                    )
                                    .clickable { showNumpadSheet = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$qtyInCart",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (qtyInCart > 0) AfadaBluePrimary else Color(0xFF4B5563),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Nút cộng (+) giống hệt nút trừ (-)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(subtleBtnBg)
                                    .border(1.dp, subtleBorder, RoundedCornerShape(9.dp))
                                    .clickable { onQuantityChange(qtyInCart + 1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tăng",
                                    tint = Color(0xFF374151),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Label Thùng / Lốc / Két cố định ở GÓC TRÊN BÊN PHẢI của thẻ
            Surface(
                color = Color(0xFFEEF2F6),
                shape = RoundedCornerShape(bottomStart = 12.dp, topEnd = 16.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = unit.unitName,
                    color = AfadaBluePrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }

    // Bàn phím Numpad trượt từ dưới lên (ModalBottomSheet)
    if (showNumpadSheet) {
        QuantityNumpadBottomSheet(
            productName = product.name,
            unitName = unit.unitName,
            unitPrice = unit.sellPrice,
            initialQty = qtyInCart,
            onCommitQuantity = { finalQty ->
                onQuantityChange(finalQty.coerceAtLeast(0))
                showNumpadSheet = false
            },
            onCancelWithoutSaving = {
                showNumpadSheet = false
            }
        )
    }
}

/**
 * Bàn phím Numpad nhập số lượng trượt từ dưới lên (ModalBottomSheet):
 * - Khi người dùng nhấn "Xác nhận", HOẶC chạm ra ngoài (scrim), HOẶC vuốt xuống / nhấn nút Back
 *   -> Đều gọi `onCommitQuantity` để LƯU số lượng đã nhập.
 * - Chỉ khi nhấn nút "Hủy" mới bỏ qua không lưu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityNumpadBottomSheet(
    productName: String,
    unitName: String,
    unitPrice: Double,
    initialQty: Int,
    onCommitQuantity: (Int) -> Unit,
    onCancelWithoutSaving: () -> Unit
) {
    var inputStr by remember { mutableStateOf(if (initialQty > 0) "$initialQty" else "") }
    var isFirstKeyPress by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentParsedQty = if (inputStr.isEmpty()) 0 else (inputStr.toIntOrNull() ?: 0)

    ModalBottomSheet(
        onDismissRequest = {
            // Chạm ra ngoài hoặc nhấn Back / vuốt xuống -> Tự động lưu số lượng hiện tại!
            onCommitQuantity(currentParsedQty)
        },
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$productName ($unitName)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1C1E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = FormatUtils.formatVnd(unitPrice),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AfadaGreenSuccess
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Màn hình hiển thị số lượng đang nhập
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3F6FA))
                    .border(1.5.dp, AfadaBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (inputStr.isEmpty()) "0" else inputStr,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AfadaBluePrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val onDigitClick: (String) -> Unit = { digit ->
                if (isFirstKeyPress) {
                    inputStr = if (digit == "0") "" else digit
                    isFirstKeyPress = false
                } else if (inputStr.length < 5) {
                    inputStr = if (inputStr == "0") digit else (inputStr + digit)
                }
            }

            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { rowDigits ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowDigits.forEach { digit ->
                            NumpadKeyButton(
                                label = digit,
                                modifier = Modifier.weight(1f),
                                onClick = { onDigitClick(digit) }
                            )
                        }
                    }
                }

                // Hàng cuối: [ C ]  [ 0 ]  [ ⌫ ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumpadKeyButton(
                        label = "C",
                        textColor = AfadaRedDebt,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            inputStr = ""
                            isFirstKeyPress = false
                        }
                    )
                    NumpadKeyButton(
                        label = "0",
                        modifier = Modifier.weight(1f),
                        onClick = { onDigitClick("0") }
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F7FA))
                            .border(1.dp, Color(0xFFE0E5EC), RoundedCornerShape(12.dp))
                            .clickable {
                                isFirstKeyPress = false
                                if (inputStr.isNotEmpty()) {
                                    inputStr = inputStr.dropLast(1)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Xóa 1 số",
                            tint = Color(0xFF374151),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hàng nút hành động: [ Hủy (Không lưu) ]  [ Xác nhận (Lưu) ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelWithoutSaving,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Hủy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }

                Button(
                    onClick = { onCommitQuantity(currentParsedQty) },
                    colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Xác nhận",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun NumpadKeyButton(
    label: String,
    textColor: Color = Color(0xFF1A1C1E),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F7FA))
            .border(1.dp, Color(0xFFE0E5EC), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

/**
 * Thanh Giỏ hàng nổi dưới đáy màn hình
 */
@Composable
fun PosFloatingCartBar(
    totalCount: Int,
    totalAmount: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AfadaGreenSuccess),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$totalCount",
                        color = AfadaGreenSuccess,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Tổng tiền tạm tính",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = FormatUtils.formatVnd(totalAmount),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    )
                }
            }

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = AfadaGreenSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TÍNH TIỀN",
                        color = AfadaGreenSuccess,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
