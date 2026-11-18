package com.storemanager.afada.ui.customer

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.core.designsystem.AfadaTopHeader
import com.storemanager.afada.data.local.CustomerEntity
import com.storemanager.afada.ui.FormatUtils
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaOrangeAccent
import com.storemanager.afada.ui.theme.AfadaRedDebt

/**
 * Màn hình Sổ Công Nợ & Vỏ Két (Đã tách các hộp thoại sang CustomerDialogs.kt)
 */
@Composable
fun CustomerScreen(
    viewModel: StoreViewModel,
    onBack: (() -> Unit)? = null
) {
    val customers by viewModel.customers.collectAsState()

    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var collectingDebtCustomer by remember { mutableStateOf<CustomerEntity?>(null) }

    val totalCustomerDebt = customers.sumOf { it.totalDebt }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AfadaTopHeader(
                title = "SỔ CÔNG NỢ",
                subtitle = "Tổng nợ cần thu: ${FormatUtils.formatVnd(totalCustomerDebt)}",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(customers, key = { it.customerId }) { customer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = customer.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                    if (customer.phone.isNotBlank()) {
                                        Text(
                                            text = "SĐT: ${customer.phone}",
                                            fontSize = 15.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                IconButton(onClick = { editingCustomer = customer }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = AfadaBluePrimary)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Đang nợ:",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = FormatUtils.formatVnd(customer.totalDebt),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = if (customer.totalDebt > 0) AfadaRedDebt else AfadaGreenSuccess
                                    )
                                }

                                if (!customer.customerId.endsWith("KH_LE")) {
                                    Button(
                                        onClick = { collectingDebtCustomer = customer },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (customer.totalDebt > 0) AfadaRedDebt else AfadaBluePrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Thu nợ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (customer.emptyCrateNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = Color(0xFFFFF8E1),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🍺 Vỏ két: ${customer.emptyCrateNote}",
                                        fontSize = 15.sp,
                                        color = Color(0xFFE65100),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { isAddingNew = true },
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
                Text("Thêm khách", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }

    collectingDebtCustomer?.let { customer ->
        CollectDebtDialog(
            customer = customer,
            onConfirm = { payAmount, updatedCrateNote, note ->
                viewModel.collectDebt(customer, payAmount, updatedCrateNote, note)
                collectingDebtCustomer = null
            },
            onDismiss = { collectingDebtCustomer = null }
        )
    }

    if (isAddingNew || editingCustomer != null) {
        CustomerEditorDialog(
            existing = editingCustomer,
            onSave = { customer ->
                viewModel.saveCustomer(customer)
                isAddingNew = false
                editingCustomer = null
            },
            onDismiss = {
                isAddingNew = false
                editingCustomer = null
            }
        )
    }
}
