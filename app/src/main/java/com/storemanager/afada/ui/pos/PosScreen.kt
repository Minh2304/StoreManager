package com.storemanager.afada.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.data.local.CustomerEntity
import com.storemanager.afada.data.local.PosSellableUnitItem
import com.storemanager.afada.ui.FormatUtils
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.theme.AfadaBluePrimary

/**
 * Màn hình Bán hàng POS:
 * - Đã xóa thanh phân loại hàng Bia hay Nước Ngọt.
 * - Mỗi đơn vị có giá bán > 0 (Két / Thùng / Lốc) hiện thành 1 thẻ mặt hàng riêng.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: StoreViewModel,
    onBack: (() -> Unit)? = null
) {
    val products by viewModel.productsWithUnits.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val posState by viewModel.posState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()

    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    val sellableItems: List<PosSellableUnitItem> = remember(products, posState.searchQuery) {
        products
            .filter { item ->
                posState.searchQuery.isBlank() ||
                    item.product.name.contains(posState.searchQuery, ignoreCase = true)
            }
            .flatMap { item ->
                val stockText = item.formattedStock()
                item.activePricedUnits().map { unit ->
                    PosSellableUnitItem(
                        product = item.product,
                        unit = unit,
                        formattedStockText = stockText
                    )
                }
            }
    }

    val activeCustomer = posState.selectedCustomer
        ?: customers.firstOrNull { it.customerId.endsWith("KH_LE") }
        ?: CustomerEntity("KH_LE", "", "Khách lẻ", "", "")

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Header POS + Thanh chọn khách hàng
            Surface(color = AfadaBluePrimary, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Quay lại",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "TẠO ĐƠN BÁN HÀNG",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }
                        if (posState.cart.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearCart() }) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Đặt về 0", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomerPicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Khách: ${activeCustomer.name}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    if (activeCustomer.totalDebt > 0 || activeCustomer.emptyCrateNote.isNotBlank()) {
                                        Text(
                                            text = "Nợ cũ: ${FormatUtils.formatVnd(activeCustomer.totalDebt)} ${if (activeCustomer.emptyCrateNote.isNotBlank()) "• ${activeCustomer.emptyCrateNote}" else ""}",
                                            color = Color(0xFFFFD54F),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Surface(color = Color.White, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = "Đổi khách",
                                    color = AfadaBluePrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Thanh tìm kiếm (Đã xóa hàng nút phân loại Bia/Nước ngọt)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = posState.searchQuery,
                    onValueChange = { viewModel.updatePosSearch(it) },
                    placeholder = { Text("Tìm tên hàng (VD: Hà Nội, Tiger...)", fontSize = 16.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (posState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updatePosSearch("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    textStyle = TextStyle(fontSize = 17.sp),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Danh sách mặt hàng theo từng đơn vị đã có giá
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = if (posState.cart.isNotEmpty()) 104.dp else 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sellableItems, key = { it.uniqueKey }) { sellable ->
                    val qtyInCart = posState.cart
                        .firstOrNull {
                            it.product.productId == sellable.product.productId &&
                                it.selectedUnit.unitId == sellable.unit.unitId
                        }
                        ?.quantity ?: 0

                    PosSellableUnitCard(
                        sellableItem = sellable,
                        qtyInCart = qtyInCart,
                        manageStockEnabled = settingsState.manageStockEnabled,
                        onQuantityChange = { newQty ->
                            viewModel.setCartQuantityForProductUnit(
                                product = sellable.product,
                                unit = sellable.unit,
                                newQty = newQty
                            )
                        }
                    )
                }
            }
        }

        // 4. Thanh Giỏ Hàng nổi phía dưới
        if (posState.cart.isNotEmpty()) {
            PosFloatingCartBar(
                totalCount = posState.totalCartItemCount,
                totalAmount = posState.finalPayableAmount,
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = { showCheckoutSheet = true }
            )
        }
    }

    if (showCustomerPicker) {
        CustomerPickerDialog(
            customers = customers,
            onSelect = {
                viewModel.selectPosCustomer(it)
                showCustomerPicker = false
            },
            onDismiss = { showCustomerPicker = false }
        )
    }

    if (showCheckoutSheet) {
        ModalBottomSheet(onDismissRequest = { showCheckoutSheet = false }) {
            CheckoutBottomSheetContent(
                viewModel = viewModel,
                activeCustomer = activeCustomer,
                onCheckoutSuccess = { showCheckoutSheet = false }
            )
        }
    }

    posState.lastCreatedOrder?.let { createdOrder ->
        OrderReceiptDialog(
            orderWithItems = createdOrder,
            onDismiss = { viewModel.dismissReceiptDialog() }
        )
    }
}
