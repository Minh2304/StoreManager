package com.storemanager.afada.ui.dashboard

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.ui.AppNotificationItem
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaRedDebt

/**
 * Module Cửa sổ Thông báo trỏ xuống ngay từ nút Thông báo (DropdownMenu anchored to Bell icon):
 * - Không hiện popup giữa màn hình, mà trỏ thẳng từ nút chuông thông báo xuống.
 * - Nếu không có thông báo nào -> Hiện "Chưa có thông báo mới".
 * - Có nút "Xóa tất cả" ở góc phải tiêu đề hoặc người dùng có thể VUỐT thông báo sang 2 bên (trái/phải) để xóa từng thông báo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDropdownPopup(
    expanded: Boolean,
    notifications: List<AppNotificationItem>,
    onDismissRequest: () -> Unit,
    onDismissItem: (String) -> Unit,
    onClearAll: () -> Unit,
    onAutoUpdateClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .width(310.dp)
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thông báo",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AfadaBluePrimary
                )

                if (notifications.isNotEmpty()) {
                    TextButton(
                        onClick = onClearAll,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = AfadaRedDebt,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Xóa tất cả",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AfadaRedDebt
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 6.dp))

            if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chưa có thông báo mới",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    notifications.forEach { item ->
                        key(item.id) {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.StartToEnd ||
                                        value == SwipeToDismissBoxValue.EndToStart
                                    ) {
                                        onDismissItem(item.id)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFFEBEE))
                                            .padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Đã xóa",
                                            color = AfadaRedDebt,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                content = {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.title,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF1A1C1E)
                                                )
                                                Text(
                                                    text = item.timeText,
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }

                                            Text(
                                                text = item.message,
                                                fontSize = 13.sp,
                                                color = Color.DarkGray
                                            )

                                            if (item.isUpdateNotification) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Button(
                                                    onClick = onAutoUpdateClick,
                                                    colors = ButtonDefaults.buttonColors(containerColor = AfadaGreenSuccess),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(38.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.SystemUpdate,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Cập nhật",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
