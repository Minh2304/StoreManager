package com.storemanager.afada.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.theme.AfadaWarningAmber

/**
 * Màn hình Trang chủ:
 * - Đã được chia nhỏ gọn gàng thành các module:
 *   + DashboardComponents.kt (Header, Lưới chức năng, Cảnh báo tồn kho)
 *   + NotificationDropdownPopup.kt (Cửa sổ thông báo trỏ xuống ngay từ nút Thông báo)
 */
@Composable
fun DashboardScreen(
    viewModel: StoreViewModel,
    onNavigate: (String) -> Unit,
    onSwitchDatabase: () -> Unit
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    val products by viewModel.productsWithUnits.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val orders by viewModel.ordersWithItems.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val debtCustomerCount = customers.count { it.totalDebt > 0 }
    val lowStockProducts = products.filter { it.product.stockBaseQty <= it.product.minStockAlert }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 24.dp)
    ) {
        // 1. Header Banner + 3 Mục quan trọng nhất: Tạo đơn, Sổ công nợ, Lịch sử
        item {
            AfadaDashboardHeaderBanner(
                storeName = authState.currentDbName,
                ownerEmail = authState.loggedInEmail,
                orderCount = orders.size,
                debtCustomerCount = debtCustomerCount,
                syncState = syncState,
                notifications = notifications,
                onOpenNotifications = {
                    viewModel.checkForGitReleaseUpdate(silent = true)
                },
                onDismissNotificationItem = { id ->
                    viewModel.dismissNotification(id)
                },
                onClearAllNotifications = {
                    viewModel.clearAllNotifications()
                },
                onAutoUpdateClick = {
                    viewModel.startAutoUpdateApk(context)
                },
                onManageStoreClick = onSwitchDatabase,
                onNavigate = onNavigate
            )
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // 2. Quản lý Kho hàng
        item {
            AfadaQuickMenuGridCard(
                productCount = products.size,
                manageStockEnabled = settingsState.manageStockEnabled,
                onNavigate = onNavigate
            )
        }

        // 3. Chỉ hiện cảnh báo mặt hàng sắp hết tồn kho khi bật "Quản lý nhập hàng"
        if (settingsState.manageStockEnabled && lowStockProducts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Hàng tồn sắp hết (${lowStockProducts.size})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = AfadaWarningAmber,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                )
            }

            items(lowStockProducts, key = { it.product.productId }) { item ->
                AfadaLowStockAlertCard(
                    item = item,
                    onClick = { onNavigate("inventory") }
                )
            }
        }
    }
}
