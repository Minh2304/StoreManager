package com.storemanager.afada.ui.report

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.core.designsystem.AfadaTopHeader
import com.storemanager.afada.data.local.OrderWithItems
import com.storemanager.afada.ui.FormatUtils
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.pos.OrderReceiptDialog
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess

/**
 * Màn hình Cài Đặt Cửa Hàng (Gồm 3 Tab rõ ràng):
 * 1. Tab "Cài đặt chung": Công tắc Quản lý nhập hàng.
 * 2. Tab "Máy in": Kết nối Máy in nhiệt Bluetooth, Xem trước bản in, Thanh kéo cỡ chữ & Ngân hàng VietQR.
 * 3. Tab "Database": Tự động gắn link Google Sheet của cửa hàng vừa tạo; người dùng chỉ cần nhấn nút để mở Google Sheet kiểm tra.
 *    (Đã xóa toàn bộ các nút Lưu link database, Lưu lên Google Sheets, Tải từ Google Sheets, Nạp lại dữ liệu mẫu).
 */
@Composable
fun ReportAndSheetScreen(
    viewModel: StoreViewModel,
    initialTab: Int = 0,
    onBack: (() -> Unit)? = null
) {
    val settingsState by viewModel.settingsState.collectAsState()

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 2)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AfadaTopHeader(
            title = "CÀI ĐẶT CỬA HÀNG",
            subtitle = "Cài đặt chung • Máy in Bluetooth • Database",
            onBack = onBack
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = AfadaBluePrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Cài đặt chung", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Máy in", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Database", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            0 -> GeneralSettingsTabContent(
                viewModel = viewModel,
                manageStockEnabled = settingsState.manageStockEnabled,
                onToggleManageStock = { viewModel.toggleManageStock(it) }
            )
            1 -> PrinterSettingsSection(viewModel = viewModel)
            2 -> DatabaseGoogleSheetsTabContent(viewModel = viewModel)
        }
    }
}

/**
 * Tab 1: Cài đặt chung
 * - Công tắc Quản lý nhập hàng.
 * - Ở dưới hiện thanh đơn giản "Thông tin phần mềm": nhấn mũi tên xổ xuống để xem chi tiết
 *   phiên bản, nhánh git release và nút kiểm tra / cập nhật từ xa.
 */
@Composable
private fun GeneralSettingsTabContent(
    viewModel: StoreViewModel,
    manageStockEnabled: Boolean,
    onToggleManageStock: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val updateState by viewModel.updateState.collectAsState()
    var expandSoftwareInfo by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Công tắc Quản lý nhập hàng
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = AfadaBluePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Quản lý nhập hàng",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFF1A1C1E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (manageStockEnabled) {
                                    "Đang BẬT: Hiện số lượng hàng tồn trong kho"
                                } else {
                                    "Đang TẮT: Không cần quan tâm số lượng hàng tồn"
                                },
                                fontSize = 14.sp,
                                color = if (manageStockEnabled) AfadaGreenSuccess else Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Switch(
                        checked = manageStockEnabled,
                        onCheckedChange = onToggleManageStock,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AfadaGreenSuccess
                        )
                    )
                }
            }
        }

        // 2. Thanh "Thông tin phần mềm" đơn giản ở dưới: Nhấn mũi tên xổ xuống để xem chi tiết
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Thanh đơn giản: Chạm vào để xổ xuống / thu gọn
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandSoftwareInfo = !expandSoftwareInfo }
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AfadaBluePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Thông tin phần mềm",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF1A1C1E)
                                )
                                Text(
                                    text = "Phiên bản v${updateState.currentVersionName}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Icon(
                            imageVector = if (expandSoftwareInfo) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = "Xem chi tiết phần mềm",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Nội dung gọn gàng xổ xuống khi nhấn mũi tên:
                    // Chỉ hiện thông tin quan trọng, xóa nút Tải APK mới, chỉ khi có bản mới mới hiện nút "Cập nhật"
                    AnimatedVisibility(visible = expandSoftwareInfo) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Divider(modifier = Modifier.padding(bottom = 4.dp))

                            SoftwareDetailRow("Phiên bản hiện tại:", "v${updateState.currentVersionName}")
                            SoftwareDetailRow("Phiên bản mới nhất:", "v${updateState.latestVersionName}")

                            updateState.checkMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (updateState.hasNewUpdate) AfadaGreenSuccess else AfadaBluePrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (updateState.hasNewUpdate) {
                                Button(
                                    onClick = { viewModel.startAutoUpdateApk(context) },
                                    enabled = !updateState.isDownloadingApk,
                                    colors = ButtonDefaults.buttonColors(containerColor = AfadaGreenSuccess),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                ) {
                                    Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (updateState.isDownloadingApk) "Đang tự động cập nhật..." else "Cập nhật",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.checkForGitReleaseUpdate(silent = false) },
                                    enabled = !updateState.isChecking,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (updateState.isChecking) "Đang kiểm tra..." else "Kiểm tra cập nhật",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SoftwareDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF1A1C1E),
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Tab 3: Database
 * - Tự động gắn link Google Sheet của cửa hàng ngay sau khi tạo cửa hàng.
 * - Có nút "MỞ GOOGLE SHEET ĐỂ KIỂM TRA" để mở trực tiếp file Google Sheet của cửa hàng.
 * - Đã xóa các nút: Lưu link database, Lưu lên Google Sheets, Tải từ Google Sheet, Nạp lại dữ liệu mẫu.
 */
@Composable
private fun DatabaseGoogleSheetsTabContent(
    viewModel: StoreViewModel
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val databases by viewModel.userDatabases.collectAsState()

    val currentDb = databases.firstOrNull { it.dbId == authState.currentDbId }
    val storeName = authState.currentDbName.ifBlank { currentDb?.dbName ?: "Cửa hàng của tôi" }
    val autoSheetLink = currentDb?.spreadsheetUrl?.takeIf { it.startsWith("http") }
        ?: authState.currentDbSpreadsheetUrl.takeIf { it.startsWith("http") }
        ?: "https://docs.google.com/spreadsheets/u/0/?q=${Uri.encode(storeName)}"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = AfadaBluePrimary,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = storeName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = Color(0xFF1A1C1E)
                            )
                            Text(
                                text = "Gmail: ${authState.loggedInEmail}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F6FA)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Link Google Sheet tự động gắn:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            Text(
                                text = autoSheetLink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AfadaBluePrimary
                            )
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(autoSheetLink))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AfadaGreenSuccess),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MỞ GOOGLE SHEET ĐỂ KIỂM TRA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Màn hình Lịch sử đơn hàng riêng khi người dùng nhấn nút "Lịch sử" ở Trang chủ
 */
@Composable
fun OrderHistoryScreen(
    viewModel: StoreViewModel,
    onBack: (() -> Unit)? = null
) {
    val orders by viewModel.ordersWithItems.collectAsState()
    var viewingOrderReceipt by remember { mutableStateOf<OrderWithItems?>(null) }
    val totalRevenue = orders.sumOf { it.order.finalAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AfadaTopHeader(
            title = "LỊCH SỬ ĐƠN HÀNG",
            subtitle = "Đã bán: ${orders.size} hóa đơn",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TỔNG TIỀN BÁN (${orders.size} đơn):",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = FormatUtils.formatVnd(totalRevenue),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AfadaGreenSuccess
                        )
                    }
                }
            }

            items(orders, key = { it.order.orderId }) { orderWithItems ->
                val o = orderWithItems.order
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewingOrderReceipt = orderWithItems },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = o.customerName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "#${o.orderId} • ${FormatUtils.formatDateTime(o.orderDate)}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            if (o.note.isNotBlank()) {
                                Text(
                                    text = "Vỏ két: ${o.note}",
                                    fontSize = 13.sp,
                                    color = AfadaBluePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Text(
                            text = FormatUtils.formatVnd(o.finalAmount),
                            fontWeight = FontWeight.ExtraBold,
                            color = AfadaGreenSuccess,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }

    viewingOrderReceipt?.let { orderWithItems ->
        OrderReceiptDialog(
            orderWithItems = orderWithItems,
            onDismiss = { viewingOrderReceipt = null }
        )
    }
}
