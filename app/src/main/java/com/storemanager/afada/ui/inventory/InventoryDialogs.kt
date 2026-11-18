package com.storemanager.afada.ui.inventory

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.storemanager.afada.core.designsystem.AfadaProductThumbnail
import com.storemanager.afada.data.local.ProductEntity
import com.storemanager.afada.data.local.ProductUnitEntity
import com.storemanager.afada.data.local.ProductWithUnits
import com.storemanager.afada.ui.FormatUtils
import com.storemanager.afada.ui.pos.QuantityNumpadBottomSheet
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaRedDebt
import java.util.UUID

/**
 * Hộp thoại Nhập Kho theo Đơn vị tính (Khi nhấn vào ô số lượng sẽ trượt bàn phím số từ dưới lên)
 */
@Composable
fun ImportStockDialog(
    item: ProductWithUnits,
    onConfirm: (ProductUnitEntity, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val activeUnits = item.activePricedUnits().ifEmpty { item.units }
    var selectedUnit by remember { mutableStateOf(activeUnits.firstOrNull() ?: item.defaultUnit()) }
    var qty by remember { mutableStateOf(10) }
    var showQtyBottomSheet by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nhập Hàng: ${item.product.name}", fontWeight = FontWeight.ExtraBold, fontSize = 19.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                activeUnits.forEach { u ->
                    val isSelected = u.unitId == selectedUnit.unitId
                    Surface(
                        color = if (isSelected) AfadaBluePrimary.copy(alpha = 0.14f) else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedUnit = u }
                    ) {
                        Text(
                            text = u.unitName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) AfadaBluePrimary else Color.DarkGray,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Ô số lượng (Chạm vào để mở bàn phím số trượt từ dưới lên)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Số lượng nhập:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF5F7FA))
                            .border(1.dp, AfadaBluePrimary, RoundedCornerShape(10.dp))
                            .clickable { showQtyBottomSheet = true }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$qty",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AfadaBluePrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedUnit, qty) },
                colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary)
            ) {
                Text("Nhập Kho", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", fontSize = 16.sp)
            }
        }
    )

    if (showQtyBottomSheet) {
        QuantityNumpadBottomSheet(
            productName = item.product.name,
            unitName = selectedUnit.unitName,
            unitPrice = selectedUnit.sellPrice,
            initialQty = qty,
            onCommitQuantity = { newQty ->
                qty = newQty
                showQtyBottomSheet = false
            },
            onCancelWithoutSaving = {
                showQtyBottomSheet = false
            }
        )
    }
}

/**
 * Hộp thoại xác nhận Xóa món hàng khi người dùng nhấn giữ (Long-press) vào thẻ món hàng
 */
@Composable
fun DeleteProductConfirmDialog(
    productName: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Xóa món hàng?",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = AfadaRedDebt
            )
        },
        text = {
            Text(
                text = "Bạn có chắc muốn xóa \"$productName\" khỏi kho hàng không?",
                fontSize = 17.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = AfadaRedDebt)
            ) {
                Text("XÓA MÓN NÀY", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Không xóa", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Hộp thoại Thêm món hàng mới (Khi nhấn nút + Thêm món):
 * (Đối với món hàng đã có trong kho, khi nhấn icon bút chì Sửa sẽ sửa trực tiếp ngay trên thẻ món hàng!)
 */
@Composable
fun ProductEditorDialog(
    existing: ProductWithUnits?,
    manageStockEnabled: Boolean,
    onSave: (ProductEntity, List<ProductUnitEntity>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val productId = existing?.product?.productId ?: "SP_${UUID.randomUUID().toString().take(5).uppercase()}"

    var name by remember { mutableStateOf(existing?.product?.name ?: "") }
    var imageUri by remember { mutableStateOf(existing?.product?.imageUri ?: "preset:beer_gold") }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }

    var ketPrice by remember { mutableStateOf(0L) }
    var thungPrice by remember { mutableStateOf(0L) }
    var locPrice by remember { mutableStateOf(0L) }

    var editingPriceLabel by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            pendingCropUri = uri
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AfadaProductThumbnail(
                    imageUri = imageUri,
                    size = 128.dp,
                    cornerRadius = 18.dp,
                    showEditOverlay = true,
                    onClick = { imagePickerLauncher.launch(arrayOf("image/*")) }
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Tên món hàng", fontSize = 16.sp, color = Color.Gray) },
                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                PriceRowField(
                    label = "Giá Két",
                    priceValue = ketPrice,
                    onClick = { editingPriceLabel = "Giá Két" }
                )

                PriceRowField(
                    label = "Giá Thùng",
                    priceValue = thungPrice,
                    onClick = { editingPriceLabel = "Giá Thùng" }
                )

                PriceRowField(
                    label = "Giá Lốc",
                    priceValue = locPrice,
                    onClick = { editingPriceLabel = "Giá Lốc" }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            if (name.isBlank()) return@Button
                            val pKet = ketPrice.toDouble()
                            val pThung = thungPrice.toDouble()
                            val pLoc = locPrice.toDouble()

                            val units = mutableListOf<ProductUnitEntity>()
                            if (pKet > 0) {
                                units.add(
                                    ProductUnitEntity(
                                        unitId = "${productId}_KET",
                                        productId = productId,
                                        unitName = "Két",
                                        conversionRate = 20,
                                        sellPrice = pKet,
                                        isDefault = true
                                    )
                                )
                            }
                            if (pThung > 0) {
                                units.add(
                                    ProductUnitEntity(
                                        unitId = "${productId}_THUNG",
                                        productId = productId,
                                        unitName = "Thùng",
                                        conversionRate = 24,
                                        sellPrice = pThung,
                                        isDefault = units.isEmpty()
                                    )
                                )
                            }
                            if (pLoc > 0) {
                                units.add(
                                    ProductUnitEntity(
                                        unitId = "${productId}_LOC",
                                        productId = productId,
                                        unitName = "Lốc",
                                        conversionRate = 6,
                                        sellPrice = pLoc,
                                        isDefault = units.isEmpty()
                                    )
                                )
                            }

                            val product = ProductEntity(
                                productId = productId,
                                name = name.trim(),
                                category = "Đồ uống",
                                baseUnit = "Lon",
                                baseCostPrice = 0.0,
                                baseSellPrice = pLoc.takeIf { it > 0 } ?: pThung.takeIf { it > 0 } ?: pKet,
                                stockBaseQty = 0,
                                imageUri = imageUri
                            )

                            onSave(product, units)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Lưu", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }

    pendingCropUri?.let { uriToCrop ->
        ImageCropDialog(
            sourceUri = uriToCrop,
            onCropSaved = { savedFilePath ->
                imageUri = savedFilePath
                pendingCropUri = null
            },
            onDismiss = { pendingCropUri = null }
        )
    }

    editingPriceLabel?.let { label ->
        val currentVal = when (label) {
            "Giá Két" -> ketPrice
            "Giá Thùng" -> thungPrice
            else -> locPrice
        }
        PriceNumpadBottomSheet(
            title = label,
            initialPrice = currentVal,
            onCommitPrice = { newPrice ->
                when (label) {
                    "Giá Két" -> ketPrice = newPrice
                    "Giá Thùng" -> thungPrice = newPrice
                    else -> locPrice = newPrice
                }
                editingPriceLabel = null
            },
            onCancel = { editingPriceLabel = null }
        )
    }
}

@Composable
fun PriceRowField(
    label: String,
    priceValue: Long,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E),
            modifier = Modifier.weight(0.38f)
        )

        Box(
            modifier = Modifier
                .weight(0.62f)
                .height(40.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFFF5F7FA))
                .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(9.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = if (priceValue > 0) FormatUtils.formatVnd(priceValue.toDouble()) else "0đ",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (priceValue > 0) AfadaGreenSuccess else Color.Gray,
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Bàn phím số Numpad trượt từ dưới lên (ModalBottomSheet) để nhập tiền cho Giá Két, Giá Thùng, Giá Lốc
 * Các số tiền hiển thị màu XANH LÁ (AfadaGreenSuccess).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceNumpadBottomSheet(
    title: String,
    initialPrice: Long,
    onCommitPrice: (Long) -> Unit,
    onCancel: () -> Unit
) {
    var inputStr by remember { mutableStateOf(if (initialPrice > 0) "$initialPrice" else "") }
    var isFirstKeyPress by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val parsedPrice = inputStr.toLongOrNull() ?: 0L

    ModalBottomSheet(
        onDismissRequest = { onCommitPrice(parsedPrice) },
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
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                color = Color(0xFF1A1C1E)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3F6FA))
                    .border(1.5.dp, AfadaGreenSuccess.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = FormatUtils.formatVnd(parsedPrice.toDouble()),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AfadaGreenSuccess
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val onAppendDigits: (String) -> Unit = { digits ->
                if (isFirstKeyPress) {
                    inputStr = if (digits.all { it == '0' }) "" else digits
                    isFirstKeyPress = false
                } else if (inputStr.length + digits.length <= 9) {
                    inputStr = if (inputStr.isEmpty() && digits.all { it == '0' }) "" else (inputStr + digits)
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
                            PriceKeyButton(
                                label = digit,
                                modifier = Modifier.weight(1f),
                                onClick = { onAppendDigits(digit) }
                            )
                        }
                    }
                }

                // Hàng 4: [ C ]  [ 0 ]  [ 000 ]  [ ⌫ ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriceKeyButton(
                        label = "C",
                        textColor = AfadaRedDebt,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            inputStr = ""
                            isFirstKeyPress = false
                        }
                    )
                    PriceKeyButton(
                        label = "0",
                        modifier = Modifier.weight(1f),
                        onClick = { onAppendDigits("0") }
                    )
                    PriceKeyButton(
                        label = "000",
                        textColor = AfadaGreenSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { onAppendDigits("000") }
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
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
                            contentDescription = "Xóa",
                            tint = Color(0xFF374151),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }

                Button(
                    onClick = { onCommitPrice(parsedPrice) },
                    colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PriceKeyButton(
    label: String,
    textColor: Color = Color(0xFF1A1C1E),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F7FA))
            .border(1.dp, Color(0xFFE0E5EC), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (label.length > 1) 17.sp else 21.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}
