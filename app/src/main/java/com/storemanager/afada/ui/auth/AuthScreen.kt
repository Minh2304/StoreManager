package com.storemanager.afada.ui.auth

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaBlueSecondary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaOrangeAccent
import com.storemanager.afada.ui.theme.AfadaRedDebt

private enum class AuthMode {
    LOGIN,      // Đăng nhập (Cần xác thực Gmail + Kiểm tra tài khoản đã đăng ký)
    REGISTER,   // Đăng ký mới (Cần mã xác thực 6 số qua Gmail)
    FORGOT_PASS // Quên mật khẩu (Cần mã xác thực 6 số qua Gmail)
}

/**
 * Màn hình Đăng nhập / Đăng ký / Quên mật khẩu:
 * 1. Đã khắc phục lỗi chưa đăng ký vẫn vào được: Tài khoản chưa đăng ký sẽ bị chặn và báo rõ cần bấm "Đăng ký mới".
 * 2. Bắt buộc xác thực Gmail khi đăng nhập (qua Mã xác thực 6 số gửi về Gmail HOẶC bấm nút "Xác thực bằng Tài khoản Google"
 *    kèm quyền Google Drive/Sheets `drive.file` & `spreadsheets` để tạo Sheet trực tiếp trên Google Drive).
 * 3. Nút "Đăng ký mới" và "Quên mật khẩu" không bọc trong ô, chỉ để Text có gạch chân (`TextDecoration.Underline`),
 *    căn chỉnh đều ở 2 đầu lề trái phải (`Arrangement.SpaceBetween`).
 * 4. Tương thích mọi tỷ lệ màn hình (`widthIn(max = 460.dp)` + `verticalScroll` + `imePadding`).
 */
@Composable
fun AuthScreen(
    viewModel: StoreViewModel,
    onLoginSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Trình khởi chạy chọn tài khoản Gmail có sẵn trên thiết bị (không cần đăng ký hoặc nhập mật khẩu)
    val gmailPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!selectedEmail.isNullOrBlank()) {
                email = selectedEmail
                viewModel.loginWithVerifiedGoogleOAuth(
                    email = selectedEmail,
                    displayName = null,
                    onSuccess = onLoginSuccess
                )
            } else {
                viewModel.setAuthErrorMessage("Không lấy được tài khoản Gmail từ thiết bị!")
            }
        }
    }

    val launchGmailAccountChooser = {
        try {
            val intent = AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null
            )
            gmailPickerLauncher.launch(intent)
        } catch (e: Exception) {
            viewModel.setAuthErrorMessage("Không mở được danh sách Gmail trên máy: ${e.message}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Avatar ứng dụng mới (Biểu tượng Cốc Bia Vàng trên nền Xanh - Cam sắc nét)
            AfadaAppAvatarLogo()

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "QUẢN LÝ BÁN HÀNG",
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AfadaBluePrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (mode) {
                    AuthMode.LOGIN -> "Đăng nhập"
                    AuthMode.REGISTER -> "Đăng ký tài khoản Email mới"
                    AuthMode.FORGOT_PASS -> "Lấy lại mật khẩu qua Email"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Ô nhập Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", fontSize = 15.sp) },
                        placeholder = { Text("vidu@email.com", fontSize = 15.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        textStyle = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Ô nhập Mật khẩu
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = {
                            Text(
                                text = if (mode == AuthMode.FORGOT_PASS) "Mật khẩu mới" else "Mật khẩu",
                                fontSize = 15.sp
                            )
                        },
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
                        textStyle = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (mode == AuthMode.LOGIN) {
                        // Nút Đăng nhập bằng Gmail ở phía dưới ô Mật khẩu: Text gạch chân, sát lề phải
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "Đăng nhập bằng Gmail",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AfadaBluePrimary,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { launchGmailAccountChooser() }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                        }
                    } else {
                        // Xác thực mã Email 6 số cho Đăng ký mới và Quên mật khẩu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 6) otpCode = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Mã 6 số", fontSize = 15.sp) },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                textStyle = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = { viewModel.sendOtpCode(email) },
                                colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text(
                                    text = "Lấy mã",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Hiển thị thông báo trạng thái / mã OTP
                    authState.message?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (authState.isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                color = if (authState.isError) AfadaRedDebt else AfadaGreenSuccess,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            )
                        }
                    }

                    // Nút hành động chính
                    if (authState.isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Button(
                            onClick = {
                                when (mode) {
                                    AuthMode.LOGIN -> {
                                        viewModel.loginWithGmail(email, password, onLoginSuccess)
                                    }
                                    AuthMode.REGISTER -> {
                                        viewModel.registerWithOtp(email, password, otpCode, onLoginSuccess)
                                    }
                                    AuthMode.FORGOT_PASS -> {
                                        viewModel.resetPasswordWithOtp(email, password, otpCode) {
                                            mode = AuthMode.LOGIN
                                            otpCode = ""
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mode == AuthMode.LOGIN) AfadaOrangeAccent else AfadaGreenSuccess
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = when (mode) {
                                    AuthMode.LOGIN -> "ĐĂNG NHẬP"
                                    AuthMode.REGISTER -> "XÁC NHẬN ĐĂNG KÝ"
                                    AuthMode.FORGOT_PASS -> "ĐỔI MẬT KHẨU"
                                },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Nút "Đăng ký mới" và "Quên mật khẩu":
            // KHÔNG bọc trong ô, chỉ để Text có gạch chân, căn chỉnh đều ở 2 đầu lề trái phải
            if (mode == AuthMode.LOGIN) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đăng ký mới",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AfadaBluePrimary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                mode = AuthMode.REGISTER
                                otpCode = ""
                                viewModel.resetOtpStage()
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    )

                    Text(
                        text = "Quên mật khẩu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AfadaRedDebt,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                mode = AuthMode.FORGOT_PASS
                                otpCode = ""
                                viewModel.resetOtpStage()
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Quay lại Đăng nhập",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AfadaBluePrimary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                mode = AuthMode.LOGIN
                                otpCode = ""
                                viewModel.resetOtpStage()
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Avatar biểu tượng chính thức của ứng dụng AFaDa Đồ Uống
 */
@Composable
private fun AfadaAppAvatarLogo() {
    Box(
        modifier = Modifier
            .size(88.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(AfadaBluePrimary, AfadaBlueSecondary)
                )
            )
            .border(2.dp, Color.White, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(AfadaOrangeAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsBar,
                contentDescription = "Avatar ứng dụng",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
