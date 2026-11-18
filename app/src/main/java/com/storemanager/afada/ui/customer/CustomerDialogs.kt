package com.storemanager.afada.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.data.local.CustomerEntity
import com.storemanager.afada.ui.FormatUtils
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaRedDebt
import java.util.UUID

/**
 * Hộp thoại Thu nợ tiền hàng & Cập nhật số lượng vỏ két
 */
@Composable
fun CollectDebtDialog(
    customer: CustomerEntity,
    onConfirm: (Double, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var payText by remember { mutableStateOf(customer.totalDebt.toLong().toString()) }
    var crateNote by remember { mutableStateOf(customer.emptyCrateNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thu Tiền Nợ: ${customer.name}", fontWeight = FontWeight.ExtraBold, fontSize = 19.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Đang nợ: ${FormatUtils.formatVnd(customer.totalDebt)}",
                    color = AfadaRedDebt,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )

                OutlinedTextField(
                    value = payText,
                    onValueChange = { payText = it },
                    label = { Text("Số tiền khách trả (đ)", fontSize = 16.sp) },
                    textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = crateNote,
                    onValueChange = { crateNote = it },
                    label = { Text("Ghi chú vỏ két còn giữ", fontSize = 16.sp) },
                    textStyle = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pay = payText.toDoubleOrNull() ?: 0.0
                    onConfirm(pay, crateNote.trim(), "Thu nợ")
                },
                colors = ButtonDefaults.buttonColors(containerColor = AfadaGreenSuccess)
            ) {
                Text("Xác Nhận Thu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", fontSize = 16.sp)
            }
        }
    )
}

/**
 * Hộp thoại Thêm / Sửa thông tin Khách hàng & Đại lý
 */
@Composable
fun CustomerEditorDialog(
    existing: CustomerEntity?,
    onSave: (CustomerEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val customerId = existing?.customerId ?: "KH_${UUID.randomUUID().toString().take(5).uppercase()}"
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var debt by remember { mutableStateOf(existing?.totalDebt?.toLong()?.toString() ?: "0") }
    var crateNote by remember { mutableStateOf(existing?.emptyCrateNote ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing == null) "Thêm Khách Mới" else "Sửa Khách Hàng",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên khách hàng / Quán") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = debt,
                    onValueChange = { debt = it },
                    label = { Text("Tiền đang nợ (đ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = crateNote,
                    onValueChange = { crateNote = it },
                    label = { Text("Ghi chú nợ vỏ két") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    onSave(
                        CustomerEntity(
                            customerId = customerId,
                            name = name.trim(),
                            phone = phone.trim(),
                            address = "",
                            totalDebt = debt.toDoubleOrNull() ?: 0.0,
                            emptyCrateNote = crateNote.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary)
            ) {
                Text("Lưu Khách", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", fontSize = 16.sp)
            }
        }
    )
}

