package com.storemanager.afada.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.data.local.CustomerEntity
import com.storemanager.afada.data.local.OrderWithItems
import com.storemanager.afada.ui.FormatUtils
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.report.BorderedReceiptTableRow
import com.storemanager.afada.ui.report.ScannableVietQrBox
import com.storemanager.afada.ui.report.isAllCapsLineWithoutDigits
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaOrangeAccent
import com.storemanager.afada.ui.theme.AfadaRedDebt

/**
 * Module Thanh toán & Đối chiếu Vỏ két ngay trên đơn hàng (Chuẩn AFaDa ngành Đồ uống/Gas)
 */
@Composable
fun CheckoutBottomSheetContent(
    viewModel: StoreViewModel,
    activeCustomer: CustomerEntity,
    onCheckoutSuccess: () -> Unit
) {
    val posState by viewModel.posState.collectAsState()
    var paidInput by remember(posState.finalPayableAmount) {
        mutableStateOf(posState.finalPayableAmount.toLong().toString())
    }
    var paymentMethod by remember { mutableStateOf("Tiền mặt") }

    // Bóc tách Vỏ két giao đi & Vỏ két thu về chuẩn AFaDa
    var crateLentInput by remember { mutableStateOf("") }
    var crateReturnedInput by remember { mutableStateOf("") }
    var orderNote by remember { mutableStateOf("") }

    val paidAmount = paidInput.toDoubleOrNull() ?: 0.0
    val remainingDebt = (posState.finalPayableAmount - paidAmount).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "KIỂM TRA ĐƠN & THANH TOÁN",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = AfadaBluePrimary
            )
            Text(
                text = "Khách: ${activeCustomer.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Divider(modifier = Modifier.padding(vertical = 6.dp))
        }

        items(posState.cart) { cartItem ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = cartItem.product.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(
                            text = "${cartItem.selectedUnit.unitName} • ${FormatUtils.formatVnd(cartItem.lineTotal)}",
                            fontSize = 14.sp,
                            color = AfadaGreenSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                viewModel.updateCartItemQuantity(
                                    cartItem.product.productId,
                                    cartItem.selectedUnit.unitId,
                                    cartItem.quantity - 1
                                )
                            }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Giảm")
                        }
                        Text(
                            text = "${cartItem.quantity}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = {
                                viewModel.updateCartItemQuantity(
                                    cartItem.product.productId,
                                    cartItem.selectedUnit.unitId,
                                    cartItem.quantity + 1
                                )
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tăng")
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = paidInput,
                onValueChange = { paidInput = it },
                label = { Text("Số tiền khách trả (đ)", fontSize = 16.sp) },
                textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { paidInput = posState.finalPayableAmount.toLong().toString() },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Trả đủ", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { paidInput = "0" },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Nợ hết", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AfadaRedDebt)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Khu vực Quản lý Vỏ két giao đi / thu về (Điểm nhấn đặc thù của AFaDa)
            Text(
                text = "Quản lý Vỏ két / Vỏ chai (Nếu có):",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AfadaBluePrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = crateLentInput,
                    onValueChange = { crateLentInput = it },
                    label = { Text("Vỏ giao đi") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = crateReturnedInput,
                    onValueChange = { crateReturnedInput = it },
                    label = { Text("Vỏ khách trả") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = orderNote,
                onValueChange = { orderNote = it },
                label = { Text("Ghi chú thêm", fontSize = 15.sp) },
                textStyle = TextStyle(fontSize = 16.sp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TỔNG TIỀN:", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        Text(
                            text = FormatUtils.formatVnd(posState.finalPayableAmount),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = AfadaGreenSuccess
                        )
                    }
                    if (remainingDebt > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("GHI SỔ NỢ:", color = AfadaRedDebt, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text(
                                text = FormatUtils.formatVnd(remainingDebt),
                                color = AfadaRedDebt,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val crateSummary = buildList {
                        if (crateLentInput.isNotBlank()) add("Giao $crateLentInput vỏ")
                        if (crateReturnedInput.isNotBlank()) add("Thu $crateReturnedInput vỏ")
                        if (orderNote.isNotBlank()) add(orderNote.trim())
                    }.joinToString(" • ")

                    viewModel.checkoutOrder(
                        paidAmount = paidAmount,
                        paymentMethod = paymentMethod,
                        note = crateSummary
                    )
                    onCheckoutSuccess()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AfadaOrangeAccent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                Text(
                    text = "XONG • XUẤT HÓA ĐƠN",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Hộp thoại chọn khách hàng / đại lý nhanh
 */
@Composable
fun CustomerPickerDialog(
    customers: List<CustomerEntity>,
    onSelect: (CustomerEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn Khách Hàng", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
        text = {
            LazyColumn(
                modifier = Modifier.height(320.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(customers) { customer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(customer) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = customer.name, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                            if (customer.totalDebt > 0) {
                                Text(
                                    text = "Nợ cũ: ${FormatUtils.formatVnd(customer.totalDebt)}",
                                    fontSize = 15.sp,
                                    color = AfadaRedDebt,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (customer.emptyCrateNote.isNotBlank()) {
                                Text(
                                    text = "Vỏ két: ${customer.emptyCrateNote}",
                                    fontSize = 13.sp,
                                    color = AfadaBluePrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Phiếu hóa đơn in nhiệt K80 / K58 sau khi tạo đơn:
 * - Bảng 4 cột kẻ ô đen trắng (Tên hàng | SL | Đ.Giá | T.Tiền).
 * - Phần két, thùng, lốc được hiển thị ngay ở cột SL (ví dụ: "2 lốc", "1 thùng").
 * - Cột T.Tiền và TỔNG TIỀN bỏ chữ "đ", chỉ để lại số tiền màu đen (Tổng tiền in đậm to hơn).
 */
@Composable
fun OrderReceiptDialog(
    orderWithItems: OrderWithItems,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val order = orderWithItems.order

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AfadaGreenSuccess, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hóa Đơn #${order.orderId}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Text("Khách: ${order.customerName}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("Ngày: ${FormatUtils.formatDateTime(order.orderDate)}", fontSize = 12.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                // Bảng kẻ ô 4 cột: Cột SL hiển thị dạng "2 lốc", "1 thùng"
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
                        wName = 40f,
                        wQty = 20f,
                        wPrice = 20f,
                        wTotal = 20f,
                        fontSize = 12.sp,
                        isHeader = true
                    )

                    orderWithItems.items.forEach { item ->
                        Divider(color = Color.Black, thickness = 1.dp)
                        BorderedReceiptTableRow(
                            col1 = item.productName,
                            col2 = "${item.quantity} ${item.unitName.lowercase()}",
                            col3 = FormatUtils.formatNumberPlain(item.unitPrice),
                            col4 = FormatUtils.formatNumberPlain(item.lineTotal),
                            wName = 40f,
                            wQty = 20f,
                            wPrice = 20f,
                            wTotal = 20f,
                            fontSize = 12.sp,
                            isHeader = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TỔNG TIỀN:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                    Text(
                        text = FormatUtils.formatNumberPlain(order.finalAmount),
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        color = Color.Black
                    )
                }
                if (order.debtAmount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CÒN NỢ:", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        Text(
                            text = FormatUtils.formatNumberPlain(order.debtAmount),
                            fontSize = 15.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                if (order.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Ghi chú: ${order.note}", fontSize = 12.sp, color = Color.Black)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { FormatUtils.shareOrderReceipt(context, orderWithItems) },
                colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Gửi Zalo / In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

