package com.storemanager.afada.ui.inventory

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.core.designsystem.AfadaProductThumbnail
import com.storemanager.afada.core.designsystem.AfadaTopHeader
import com.storemanager.afada.data.local.ProductEntity
import com.storemanager.afada.data.local.ProductUnitEntity
import com.storemanager.afada.data.local.ProductWithUnits
import com.storemanager.afada.ui.FormatUtils
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaOrangeAccent
import com.storemanager.afada.ui.theme.AfadaRedDebt

/**
 * Màn hình Quản lý Kho hàng:
 * - Khi nhấn vào thẻ món hàng: KHÔNG thay đổi gì (chỉ khi nhấn giữ mới hiện hộp thoại Xóa).
 * - Chỉ khi nhấn vào icon bút chì Sửa (Edit) ở góc phải thẻ món hàng:
 *   + Sửa trực tiếp ngay trên thẻ món hàng đó (Inline Edit), KHÔNG mở popup!
 *   + Trên thẻ lúc đang sửa: Ảnh hiện bút chì mờ để đổi & crop ảnh, tên món thành ô nhập,
 *     và 3 dòng Giá Két, Giá Thùng, Giá Lốc thành ô bấm mở bàn phím số trượt từ dưới lên.
 * - Các số tiền đều hiển thị màu XANH LÁ (AfadaGreenSuccess).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryScreen(
    viewModel: StoreViewModel,
    onBack: (() -> Unit)? = null
) {
    val products by viewModel.productsWithUnits.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()

    // ID của món hàng đang bật chế độ sửa trực tiếp ngay trên thẻ
    var inlineEditingProductId by remember { mutableStateOf<String?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var importingStockItem by remember { mutableStateOf<ProductWithUnits?>(null) }
    var deletingItem by remember { mutableStateOf<ProductWithUnits?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AfadaTopHeader(
                title = "KHO HÀNG (${products.size} MÓN)",
                subtitle = "Nhấn icon bút chì để sửa • Nhấn giữ để xóa",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products, key = { it.product.productId }) { item ->
                    val isEditingThisCard = inlineEditingProductId == item.product.productId

                    if (isEditingThisCard) {
                        InlineEditableInventoryCard(
                            item = item,
                            onSave = { updatedProduct, updatedUnits ->
                                viewModel.saveProductWithUnits(updatedProduct, updatedUnits)
                                inlineEditingProductId = null
                            },
                            onCancel = {
                                inlineEditingProductId = null
                            }
                        )
                    } else {
                        ReadOnlyInventoryCard(
                            item = item,
                            manageStockEnabled = settingsState.manageStockEnabled,
                            onEditIconClick = {
                                inlineEditingProductId = item.product.productId
                            },
                            onLongPressCard = {
                                deletingItem = item
                            },
                            onImportStockClick = {
                                importingStockItem = item
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { isCreatingNew = true },
            containerColor = AfadaOrangeAccent,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Thêm món", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }

    // Hộp thoại Nhập hàng (Khi bật Quản lý nhập hàng)
    importingStockItem?.let { item ->
        ImportStockDialog(
            item = item,
            onConfirm = { selectedUnit, qty ->
                viewModel.importStockByUnit(item.product, selectedUnit, qty)
                importingStockItem = null
            },
            onDismiss = { importingStockItem = null }
        )
    }

    // Hộp thoại Thêm món mới (Khi nhấn nút + Thêm món ở góc dưới)
    if (isCreatingNew) {
        ProductEditorDialog(
            existing = null,
            manageStockEnabled = settingsState.manageStockEnabled,
            onSave = { product, units ->
                viewModel.saveProductWithUnits(product, units)
                isCreatingNew = false
            },
            onDismiss = {
                isCreatingNew = false
            }
        )
    }

    // Hộp thoại Xóa món hàng (Chỉ hiện khi nhấn giữ vào thẻ món hàng)
    deletingItem?.let { item ->
        DeleteProductConfirmDialog(
            productName = item.product.name,
            onConfirmDelete = {
                viewModel.deleteProduct(item.product.productId)
                deletingItem = null
            },
            onDismiss = { deletingItem = null }
        )
    }
}

/**
 * Thẻ món hàng ở chế độ xem bình thường:
 * - Nhấn vào thẻ (onClick) KHÔNG làm gì cả.
 * - Nhấn giữ vào thẻ (onLongClick) hiện xác nhận Xóa.
 * - Chỉ nhấn vào icon bút chì (onEditIconClick) mới chuyển thẻ này sang chế độ sửa trực tiếp!
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReadOnlyInventoryCard(
    item: ProductWithUnits,
    manageStockEnabled: Boolean,
    onEditIconClick: () -> Unit,
    onLongPressCard: () -> Unit,
    onImportStockClick: () -> Unit
) {
    val ketPrice = item.units.firstOrNull { it.unitName.startsWith("Két", ignoreCase = true) }?.sellPrice ?: 0.0
    val thungPrice = item.units.firstOrNull { it.unitName.startsWith("Thùng", ignoreCase = true) }?.sellPrice ?: 0.0
    val locPrice = item.units.firstOrNull { it.unitName.startsWith("Lốc", ignoreCase = true) }?.sellPrice ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Nhấn vào thẻ món hàng không thay đổi gì */ },
                onLongClick = onLongPressCard
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ảnh món hàng TO (108dp), nằm sát viền bên trái của thẻ món hàng
                AfadaProductThumbnail(
                    imageUri = item.product.imageUri,
                    size = 108.dp,
                    cornerRadius = 0.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.product.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color(0xFF1A1C1E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Chỉ khi nhấn vào icon bút chì này mới bật chế độ sửa trực tiếp trên thẻ
                        IconButton(
                            onClick = onEditIconClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Sửa trực tiếp",
                                tint = AfadaBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (manageStockEnabled) {
                        Text(
                            text = "Tồn: ${item.formattedStock()}",
                            color = if (item.product.stockBaseQty <= item.product.minStockAlert) AfadaRedDebt else AfadaGreenSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    UnitPriceLine(label = "Két", price = ketPrice)
                    UnitPriceLine(label = "Thùng", price = thungPrice)
                    UnitPriceLine(label = "Lốc", price = locPrice)
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            if (manageStockEnabled) {
                Divider()
                OutlinedButton(
                    onClick = onImportStockClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddBox, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nhập thêm hàng vào kho", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Thẻ món hàng ở chế độ SỬA TRỰC TIẾP TRÊN THẺ (Inline Edit Card):
 * - Không mở popup! Sửa ngay tại vị trí thẻ món hàng.
 * - Ảnh bên trái to hơn một chút (124dp) và hiện biểu tượng bút chì mờ để nhấn vào đổi & crop ảnh.
 * - Tên món hàng là ô nhập trực tiếp.
 * - Các mục Giá Két, Giá Thùng, Giá Lốc nằm bên dưới: nhấn vào ô giá bên phải sẽ trượt bàn phím số từ dưới lên.
 */
@Composable
private fun InlineEditableInventoryCard(
    item: ProductWithUnits,
    onSave: (ProductEntity, List<ProductUnitEntity>) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val productId = item.product.productId

    var name by remember(productId) { mutableStateOf(item.product.name) }
    var imageUri by remember(productId) { mutableStateOf(item.product.imageUri) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }

    val existingKet = item.units.firstOrNull { it.unitName.startsWith("Két", ignoreCase = true) }
    val existingThung = item.units.firstOrNull { it.unitName.startsWith("Thùng", ignoreCase = true) }
    val existingLoc = item.units.firstOrNull { it.unitName.startsWith("Lốc", ignoreCase = true) }

    var ketPrice by remember(productId) {
        mutableStateOf(existingKet?.sellPrice?.takeIf { it > 0 }?.toLong() ?: 0L)
    }
    var thungPrice by remember(productId) {
        mutableStateOf(existingThung?.sellPrice?.takeIf { it > 0 }?.toLong() ?: 0L)
    }
    var locPrice by remember(productId) {
        mutableStateOf(existingLoc?.sellPrice?.takeIf { it > 0 }?.toLong() ?: 0L)
    }

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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hàng trên: Ảnh to (118dp) có bút chì mờ bên trái + Ô nhập Tên món hàng bên phải
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AfadaProductThumbnail(
                    imageUri = imageUri,
                    size = 118.dp,
                    cornerRadius = 14.dp,
                    showEditOverlay = true,
                    onClick = { imagePickerLauncher.launch(arrayOf("image/*")) }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Tên món hàng", fontSize = 15.sp, color = Color.Gray) },
                        textStyle = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Nút Lưu & Hủy gọn gàng ngay trên thẻ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.DarkGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hủy", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
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

                                val updatedProduct = item.product.copy(
                                    name = name.trim(),
                                    baseSellPrice = pLoc.takeIf { it > 0 } ?: pThung.takeIf { it > 0 } ?: pKet,
                                    imageUri = imageUri
                                )
                                onSave(updatedProduct, units)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lưu", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            Divider()

            // Các mục giá: Giá Két, Giá Thùng, Giá Lốc bên trái - Ô nhập đơn giá bên phải
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
        }
    }

    // Hộp thoại Crop thu phóng ảnh tỷ lệ 1:1 sau khi chọn từ thư viện
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

    // Bàn phím số Numpad trượt từ dưới lên khi nhấn vào ô Giá Két / Giá Thùng / Giá Lốc
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
private fun UnitPriceLine(label: String, price: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.SemiBold
        )
        if (price > 0) {
            Text(
                text = FormatUtils.formatVnd(price),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AfadaGreenSuccess
            )
        } else {
            Text(
                text = "—",
                fontSize = 14.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
