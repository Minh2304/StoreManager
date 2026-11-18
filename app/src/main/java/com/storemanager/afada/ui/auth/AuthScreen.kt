package com.storemanager.afada.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaOrangeAccent
import com.storemanager.afada.ui.theme.AfadaRedDebt

private enum class AuthMode {
    LOGIN,      // Đăng nhập nhanh (Không cần xác thực OTP)
    REGISTER,   // Đăng ký mới (Cần mã xác thực 6 số qua Gmail)
    FORGOT_PASS // Quên mật khẩu (Cần mã xác thực 6 số qua Gmail)
}

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Biểu tượng lớn, rõ ràng cho người lớn tuổi
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(AfadaBluePrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "QUẢN LÝ BÁN HÀNG",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AfadaBluePrimary
        )

        Text(
            text = when (mode) {
                AuthMode.LOGIN -> "Đăng nhập bằng Gmail"
                AuthMode.REGISTER -> "Đăng ký Gmail mới"
                AuthMode.FORGOT_PASS -> "Lấy lại mật khẩu"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(22.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ô nhập Gmail cỡ chữ to 18sp
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Địa chỉ Gmail", fontSize = 16.sp) },
                    placeholder = { Text("vidu@gmail.com", fontSize = 16.sp) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Ô nhập Mật khẩu cỡ chữ to 18sp
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            text = if (mode == AuthMode.FORGOT_PASS) "Mật khẩu mới" else "Mật khẩu",
                            fontSize = 16.sp
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
                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // CHỈ HIỆN MÃ XÁC THỰC KHI ĐĂNG KÝ HOẶC QUÊN MẬT KHẨU
                if (mode == AuthMode.REGISTER || mode == AuthMode.FORGOT_PASS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6) otpCode = it },
                            label = { Text("Mã 6 số", fontSize = 16.sp) },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { viewModel.sendOtpCode(email) },
                            colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(58.dp)
                        ) {
                            Text(
                                text = "Lấy mã",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Hiển thị thông báo to, rõ ràng
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        )
                    }
                }

                // Nút bấm chính cực lớn (cao 58dp, chữ 18sp) dễ bấm cho người lớn tuổi
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
                            .height(58.dp)
                    ) {
                        Text(
                            text = when (mode) {
                                AuthMode.LOGIN -> "VÀO ỨNG DỤNG"
                                AuthMode.REGISTER -> "XÁC NHẬN ĐĂNG KÝ"
                                AuthMode.FORGOT_PASS -> "ĐỔI MẬT KHẨU"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Các nút chuyển đổi chế độ đơn giản, ít chữ
        if (mode == AuthMode.LOGIN) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        mode = AuthMode.REGISTER
                        otpCode = ""
                        viewModel.resetOtpStage()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("Đăng ký mới", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        mode = AuthMode.FORGOT_PASS
                        otpCode = ""
                        viewModel.resetOtpStage()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("Quên mật khẩu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AfadaRedDebt)
                }
            }
        } else {
            TextButton(
                onClick = {
                    mode = AuthMode.LOGIN
                    otpCode = ""
                    viewModel.resetOtpStage()
                }
            ) {
                Text(
                    text = "← Quay lại Đăng nhập",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AfadaBluePrimary
                )
            }
        }
    }
}

