package com.storemanager.afada.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.core.designsystem.AfadaMenuIconTile
import com.storemanager.afada.data.local.ProductWithUnits
import com.storemanager.afada.ui.AppNotificationItem
import com.storemanager.afada.ui.FormatUtils
import com.storemanager.afada.ui.SyncStatusState
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaBlueSecondary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaOrangeAccent
import com.storemanager.afada.ui.theme.AfadaRedDebt
import com.storemanager.afada.ui.theme.AfadaWarningAmber

/**
 * Module 1: Header cửa hàng + Thẻ 3 mục quan trọng nhất:
 * - Bên cạnh tên Cửa hàng có 1 chấm tròn nhỏ báo trạng thái đồng bộ Google Sheet:
 *   + Màu Xanh lá (#00E676): Mọi dữ liệu đã được cập nhật với Google Sheet.
 *   + Màu Vàng (#FFD600): Đang đồng bộ với Google Sheet.
 *   + Màu Đỏ (#FF5252): Không kết nối được với Google Sheet.
 * - Khi nhấn vào Tên Cửa Hàng -> Trỏ 1 list nhỏ (DropdownMenu) ngay bên dưới có nút "Quản lý cửa hàng".
 * - Nút Cài đặt đặt ở góc trên bên phải (không bao viền).
 */
@Composable
fun AfadaDashboardHeaderBanner(
    storeName: String,
    ownerEmail: String,
    orderCount: Int,
    debtCustomerCount: Int,
    syncState: SyncStatusState,
    notifications: List<AppNotificationItem>,
    onOpenNotifications: () -> Unit,
    onDismissNotificationItem: (String) -> Unit,
    onClearAllNotifications: () -> Unit,
    onAutoUpdateClick: () -> Unit,
    onManageStoreClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var showStoreDropdown by remember { mutableStateOf(false) }
    var showNotificationDropdown by remember { mutableStateOf(false) }

    // Màu chấm trạng thái đồng bộ bên cạnh tên cửa hàng:
    // - Vàng: đang đồng bộ (isSyncing)
    // - Đỏ: lỗi kết nối (isError)
    // - Xanh lá: mọi thứ đã được cập nhật với Sheet
    val syncDotColor = when {
        syncState.isSyncing -> Color(0xFFFFD600)
        syncState.isError -> Color(0xFFFF5252)
        else -> Color(0xFF00E676)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AfadaBluePrimary, AfadaBlueSecondary)
                ),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column {
            // Hàng 1: Nhấn vào Tên cửa hàng để trỏ list nhỏ ngay dưới + Nút Cài đặt ở góc trên phải
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showStoreDropdown = true }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = storeName.ifBlank { "Đại Lý Đồ Uống" },
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Chấm tròn nhỏ báo trạng thái đồng bộ Google Sheet (Xanh lá / Vàng / Đỏ)
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(syncDotColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Mở menu cửa hàng",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = ownerEmail,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // List nhỏ trỏ ngay dưới tên cửa hàng với nút "Quản lý cửa hàng"
                    DropdownMenu(
                        expanded = showStoreDropdown,
                        onDismissRequest = { showStoreDropdown = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Quản lý cửa hàng",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1C1E)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = AfadaBluePrimary
                                )
                            },
                            onClick = {
                                showStoreDropdown = false
                                onManageStoreClick()
                            }
                        )
                    }
                }

                // Cụm bên phải: Icon Thông báo (trỏ cửa sổ thông báo ngay từ nút xuống) + Nút Cài đặt (không viền)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = {
                                onOpenNotifications()
                                showNotificationDropdown = true
                            },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Thông báo",
                                tint = Color.White,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                        if (notifications.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp, end = 6.dp)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252))
                            )
                        }

                        // Cửa sổ thông báo trỏ xuống trực tiếp từ nút chuông thông báo
                        NotificationDropdownPopup(
                            expanded = showNotificationDropdown,
                            notifications = notifications,
                            onDismissRequest = { showNotificationDropdown = false },
                            onDismissItem = onDismissNotificationItem,
                            onClearAll = onClearAllNotifications,
                            onAutoUpdateClick = {
                                showNotificationDropdown = false
                                onAutoUpdateClick()
                            }
                        )
                    }

                    IconButton(
                        onClick = { onNavigate("report") },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Hàng 2: Thẻ nổi hiển thị 3 mục quan trọng nhất: Tạo đơn - Sổ công nợ - Lịch sử
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryTopActionTile(
                        title = "Tạo đơn",
                        subtitle = "Bán hàng",
                        icon = Icons.Default.PointOfSale,
                        color = AfadaOrangeAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("pos") }
                    )

                    PrimaryTopActionTile(
                        title = "Sổ công nợ",
                        subtitle = if (debtCustomerCount > 0) "$debtCustomerCount khách nợ" else "Khách nợ",
                        icon = Icons.Default.MenuBook,
                        color = AfadaRedDebt,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("customer") }
                    )

                    PrimaryTopActionTile(
                        title = "Lịch sử",
                        subtitle = "$orderCount hóa đơn",
                        icon = Icons.Default.History,
                        color = AfadaBluePrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("history") }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryTopActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1C1E),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Module 2: Lưới quản lý Kho hàng (Kho hàng + Nhập hàng khi bật trong Cài đặt)
 */
@Composable
fun AfadaQuickMenuGridCard(
    productCount: Int,
    manageStockEnabled: Boolean,
    onNavigate: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            AfadaMenuIconTile(
                title = "Kho hàng",
                icon = Icons.Default.Inventory2,
                color = AfadaGreenSuccess,
                badgeText = "$productCount",
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("inventory") }
            )
            if (manageStockEnabled) {
                AfadaMenuIconTile(
                    title = "Nhập hàng",
                    icon = Icons.Default.AddBox,
                    color = Color(0xFF00897B),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("inventory") }
                )
            }
        }
    }
}

/**
 * Module 3: Thanh trạng thái đồng bộ Google Sheets
 */
@Composable
fun AfadaGoogleSheetSyncCard(
    syncState: SyncStatusState,
    onSyncNow: () -> Unit,
    onOpenSheetConfig: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (syncState.webAppUrl.isNotBlank()) Icons.Default.CloudDone else Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = AfadaBluePrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Đồng bộ Google Sheets",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = AfadaBluePrimary
                    )
                    Text(
                        text = if (syncState.webAppUrl.isBlank()) {
                            "Đang lưu trên máy • Chạm để gắn Sheet"
                        } else {
                            "Đã lưu lúc: ${FormatUtils.formatDateTime(syncState.lastSyncTime)}"
                        },
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }

            if (syncState.isSyncing) {
                CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 3.dp)
            } else {
                Button(
                    onClick = {
                        if (syncState.webAppUrl.isBlank()) onOpenSheetConfig() else onSyncNow()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (syncState.webAppUrl.isBlank()) "Kết nối" else "Lưu ngay",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Module 4: Thẻ cảnh báo mặt hàng sắp hết tồn kho (Chỉ hiển thị khi bật "Quản lý nhập hàng")
 */
@Composable
fun AfadaLowStockAlertCard(
    item: ProductWithUnits,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = AfadaWarningAmber,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.product.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Còn: ${item.formattedStock()}",
                        color = AfadaRedDebt,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                color = AfadaOrangeAccent,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "+ Nhập kho",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}
