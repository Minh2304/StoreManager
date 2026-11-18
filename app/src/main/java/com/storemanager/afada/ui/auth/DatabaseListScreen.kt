package com.storemanager.afada.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.data.local.StoreDatabaseEntity
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaOrangeAccent
import com.storemanager.afada.ui.theme.AfadaRedDebt

/**
 * Trang Quản Lý Cửa Hàng:
 * - Khi giữ vào 1 tên cửa hàng -> Nút "Xóa" trượt từ bên phải sang đè vào vị trí nút mũi tên.
 * - Khi người dùng nhấn vào nút "Xóa" đó -> Mới hiện popup yêu cầu nhập mật khẩu đăng nhập để xóa.
 * - Khi nhấn nút "Đăng xuất" -> Hỏi xác nhận người dùng trước khi đăng xuất.
 */
@Composable
fun DatabaseListScreen(
    viewModel: StoreViewModel,
    onDatabaseSelected: () -> Unit,
    onLogout: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val databases by viewModel.userDatabases.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newDbName by remember { mutableStateOf("") }

    // ID của cửa hàng đang trượt nút Xóa từ bên phải sang (khi người dùng nhấn giữ)
    var revealedDeleteDbId by remember { mutableStateOf<String?>(null) }

    // Cửa hàng đang mở Popup xác nhận mật khẩu để xóa (chỉ mở khi nhấn vào nút Xóa đã trượt ra)
    var deletingDb by remember { mutableStateOf<StoreDatabaseEntity?>(null) }

    // Hộp thoại hỏi xác nhận trước khi Đăng xuất
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Thanh tiêu đề "QUẢN LÝ CỬA HÀNG"
            Surface(
                color = AfadaBluePrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = "QUẢN LÝ CỬA HÀNG",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${authState.loggedInEmail} • Nhấn giữ để hiện nút xóa",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 2. Danh sách các cửa hàng đang quản lý
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (databases.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .background(AfadaBluePrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        tint = AfadaBluePrimary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Chưa có cửa hàng nào",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Nhấn nút \"Thêm cửa hàng\" bên dưới để tự động tạo Google Sheet mới trên Gmail.",
                                    fontSize = 15.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(databases, key = { it.dbId }) { db ->
                        val isDeleteRevealed = revealedDeleteDbId == db.dbId

                        DatabaseCardItem(
                            db = db,
                            isDeleteRevealed = isDeleteRevealed,
                            onClick = {
                                if (isDeleteRevealed) {
                                    // Nếu đang hiện nút Xóa mà chạm vào vùng thẻ ngoài nút Xóa -> Ẩn nút Xóa đi
                                    revealedDeleteDbId = null
                                } else {
                                    viewModel.selectDatabase(db, onDatabaseSelected)
                                }
                            },
                            onLongClick = {
                                revealedDeleteDbId = if (isDeleteRevealed) null else db.dbId
                            },
                            onDeleteButtonClick = {
                                revealedDeleteDbId = null
                                deletingDb = db
                            }
                        )
                    }
                }
            }

            // 3. Khu vực nút hành động cố định bên dưới: [ + Thêm cửa hàng ] và [ Đăng xuất ]
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AfadaOrangeAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thêm cửa hàng",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    OutlinedButton(
                        onClick = { showLogoutConfirmDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = AfadaRedDebt,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đăng xuất",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AfadaRedDebt
                        )
                    }
                }
            }
        }

        if (authState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Đang xử lý trên Gmail...", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 4. Hộp thoại hỏi xác nhận trước khi Đăng xuất
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "Đăng xuất tài khoản?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AfadaBluePrimary
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc muốn đăng xuất khỏi tài khoản Gmail ${authState.loggedInEmail} không?",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AfadaRedDebt),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đăng xuất", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 5. Hộp thoại đặt tên Cửa hàng mới -> Tự động tạo và gắn link Google Sheet mới trên Gmail
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "Thêm Cửa Hàng Mới",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AfadaBluePrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Hệ thống sẽ tự động tạo file Google Sheet với tên cửa hàng trên Gmail (${authState.loggedInEmail}) và gắn sẵn link vào mục Database:",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    OutlinedTextField(
                        value = newDbName,
                        onValueChange = { newDbName = it },
                        placeholder = { Text("Tên cửa hàng", fontSize = 16.sp) },
                        textStyle = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDbName.isNotBlank()) {
                            val nameToCreate = newDbName
                            newDbName = ""
                            showCreateDialog = false
                            viewModel.createNewDatabase(nameToCreate) { createdDb ->
                                viewModel.selectDatabase(createdDb, onDatabaseSelected)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AfadaGreenSuccess),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("Tạo ngay", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Hủy", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 6. Popup Xóa cửa hàng (Chỉ hiện sau khi người dùng bấm vào nút Xóa trượt ra ở bên phải thẻ)
    deletingDb?.let { targetDb ->
        DeleteDatabasePasswordDialog(
            db = targetDb,
            onConfirmDelete = { passwordInput, onErrorResult ->
                viewModel.deleteDatabaseWithPassword(
                    db = targetDb,
                    passwordInput = passwordInput,
                    onSuccess = {
                        deletingDb = null
                    },
                    onError = { errMsg ->
                        onErrorResult(errMsg)
                    }
                )
            },
            onDismiss = {
                deletingDb = null
            }
        )
    }
}

@Composable
private fun DeleteDatabasePasswordDialog(
    db: StoreDatabaseEntity,
    onConfirmDelete: (password: String, onError: (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Xóa cửa hàng \"${db.dbName}\"?",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AfadaRedDebt
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Nhập mật khẩu đăng nhập để xác nhận xóa cửa hàng này:",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    placeholder = { Text("Mật khẩu đăng nhập", fontSize = 16.sp) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Hiện mật khẩu"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = AfadaRedDebt,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password.isBlank()) {
                        errorMessage = "Vui lòng nhập mật khẩu đăng nhập!"
                        return@Button
                    }
                    onConfirmDelete(password) { errMsg ->
                        errorMessage = errMsg
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AfadaRedDebt),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Xóa cửa hàng", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Thẻ cửa hàng trong danh sách:
 * - Bình thường hiển thị nút mũi tên màu cam ở bên phải.
 * - Khi người dùng nhấn giữ (onLongClick): Nút "Xóa" màu đỏ sẽ trượt từ bên phải sang (`slideInHorizontally`)
 *   đè lên vị trí nút mũi tên.
 * - Khi nhấn vào nút "Xóa" đó -> Mới gọi `onDeleteButtonClick` để hiện popup nhập mật khẩu xóa!
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DatabaseCardItem(
    db: StoreDatabaseEntity,
    isDeleteRevealed: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(AfadaBluePrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = AfadaBluePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = db.dbName,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AfadaBluePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = AfadaGreenSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Google Sheet: ${db.dbName}",
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Vị trí bên phải: Nút mũi tên (Và nút Xóa trượt từ bên phải sang đè lên khi nhấn giữ)
                Box(contentAlignment = Alignment.CenterEnd) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AfadaOrangeAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Mở",
                            tint = Color.White
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = isDeleteRevealed,
                        enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) + fadeOut()
                    ) {
                        Button(
                            onClick = onDeleteButtonClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AfadaRedDebt),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Xóa",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
