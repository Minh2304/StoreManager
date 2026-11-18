package com.storemanager.afada.ui.report

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsBluetooth
import androidx.compose.material.icons.filled.ViewColumn
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.storemanager.afada.ui.PrinterSettingsState
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.theme.AfadaBluePrimary
import com.storemanager.afada.ui.theme.AfadaGreenSuccess
import com.storemanager.afada.ui.theme.AfadaOrangeAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.roundToInt

data class PairedBtPrinter(
    val name: String,
    val address: String
)

/**
 * Tab "Máy in" (Đã tách phần hiển thị Xem trước bản in sang ReceiptPreviewComponents.kt để dễ bảo trì):
 * 1. Kết nối Máy in Bluetooth (trên cùng).
 * 2. Xem trước bản in trực tiếp (LiveReceiptPreviewCard).
 * 3. Khổ giấy, Thanh kéo cỡ chữ, Kích thước các cột kẻ ô & Ô nhập Tiêu đề.
 * 4. Cài đặt Ngân hàng & Mã QR chuyển khoản (đặt ở dưới cùng).
 */
@Composable
fun PrinterSettingsSection(viewModel: StoreViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val printerState by viewModel.printerState.collectAsState()
    val authState by viewModel.authState.collectAsState()

    var pairedDevices by remember { mutableStateOf<List<PairedBtPrinter>>(emptyList()) }
    var localStatus by remember(printerState.statusMessage) { mutableStateOf(printerState.statusMessage) }
    var showBankSearchDialog by remember { mutableStateOf(false) }

    val customQrPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            viewModel.updatePrinterSettings(printerState.copy(customQrImageUri = uri.toString()))
        }
    }

    val btPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        pairedDevices = loadPairedBluetoothPrinters(context)
        if (pairedDevices.isEmpty()) {
            localStatus = "Hiển thị 2 máy in Bluetooth mẫu để test trên máy ảo."
            pairedDevices = listOf(
                PairedBtPrinter("Xprinter XP-N160II (Bluetooth)", "DC:0D:30:A1:B2:C3"),
                PairedBtPrinter("Máy in nhiệt cầm tay K58-BT", "00:11:22:33:FF:EE")
            )
        }
    }

    val scanPairedPrinters = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            btPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            val found = loadPairedBluetoothPrinters(context)
            if (found.isNotEmpty()) {
                pairedDevices = found
                localStatus = "Tìm thấy ${found.size} thiết bị Bluetooth đã ghép đôi."
            } else {
                pairedDevices = listOf(
                    PairedBtPrinter("Xprinter XP-N160II (Bluetooth)", "DC:0D:30:A1:B2:C3"),
                    PairedBtPrinter("Máy in nhiệt cầm tay K58-BT", "00:11:22:33:FF:EE")
                )
                localStatus = "Hiển thị 2 máy in mẫu để test trên máy ảo."
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. KẾT NỐI MÁY IN NHIỆT BLUETOOTH (ĐẶT LÊN ĐẦU TIÊN)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AfadaBluePrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (printerState.bluetoothDeviceAddress.isNotBlank()) {
                                    Icons.Default.BluetoothConnected
                                } else {
                                    Icons.Default.Bluetooth
                                },
                                contentDescription = null,
                                tint = AfadaBluePrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kết nối Máy in Bluetooth",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = AfadaBluePrimary
                            )
                            Text(
                                text = if (printerState.bluetoothDeviceAddress.isNotBlank()) {
                                    "Đang chọn: ${printerState.bluetoothDeviceName}"
                                } else {
                                    "Chưa chọn máy in nhiệt Bluetooth"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (printerState.bluetoothDeviceAddress.isNotBlank()) AfadaGreenSuccess else Color.Gray
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { scanPairedPrinters() },
                            colors = ButtonDefaults.buttonColors(containerColor = AfadaBluePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tìm máy in BT", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                                } catch (_: Exception) {
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.SettingsBluetooth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ghép đôi BT", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (pairedDevices.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 2.dp))
                        pairedDevices.forEach { device ->
                            val isSelected = device.address == printerState.bluetoothDeviceAddress
                            Surface(
                                color = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF5F7FA),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) AfadaGreenSuccess else Color(0xFFE0E5EC),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.selectBluetoothPrinter(device.name, device.address)
                                        localStatus = "Đã chọn máy in: ${device.name}"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = device.name,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1A1C1E)
                                        )
                                        Text(
                                            text = "MAC: ${device.address}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Đã chọn",
                                            tint = AfadaGreenSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                if (printerState.bluetoothDeviceAddress.isNotBlank()) {
                                    sendEscPosTestTicketViaBluetooth(
                                        context = context,
                                        macAddress = printerState.bluetoothDeviceAddress,
                                        storeName = authState.currentDbName.ifBlank { "Đại Lý Đồ Uống" },
                                        printerState = printerState
                                    )
                                }
                                localStatus = "Đã gửi lệnh in thử tới máy in Bluetooth!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AfadaOrangeAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("IN THỬ BLUETOOTH NGAY", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    localStatus?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AfadaGreenSuccess
                        )
                    }
                }
            }
        }

        // 2. XEM TRƯỚC BẢN IN TRỰC TIẾP (LIVE RECEIPT PREVIEW)
        item {
            LiveReceiptPreviewCard(
                storeName = authState.currentDbName.ifBlank { "ĐẠI LÝ ĐỒ UỐNG AFADA" },
                printerState = printerState
            )
        }

        // 3. CÀI ĐẶT KHỔ GIẤY, THANH KÉO CỠ CHỮ, ĐỘ RỘNG CỘT KẺ Ô & Ô NHẬP TIÊU ĐỀ
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
                        Icon(Icons.Default.ViewColumn, contentDescription = null, tint = AfadaBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Khổ giấy, Cỡ chữ & Cột hóa đơn",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = AfadaBluePrimary
                        )
                    }

                    OptionSelectorRow(
                        label = "Khổ giấy nhiệt:",
                        options = listOf("K58 (58mm)", "K80 (80mm)"),
                        selectedOption = printerState.paperSize,
                        onSelect = { chosen ->
                            viewModel.updatePrinterSettings(printerState.copy(paperSize = chosen))
                        }
                    )

                    SliderSettingRow(
                        label = "Cỡ chữ trên hóa đơn",
                        valueText = "${printerState.fontScale} pt",
                        currentValue = printerState.fontScale,
                        valueRange = 9f..16f,
                        onValueChange = { newScale ->
                            viewModel.updatePrinterSettings(printerState.copy(fontScale = newScale))
                        }
                    )

                    Divider()

                    SliderSettingRow(
                        label = "Cột Tên hàng",
                        valueText = "${printerState.colNameWeight}%",
                        currentValue = printerState.colNameWeight,
                        valueRange = 25f..60f,
                        onValueChange = { newVal ->
                            viewModel.updatePrinterSettings(printerState.copy(colNameWeight = newVal))
                        }
                    )

                    SliderSettingRow(
                        label = "Cột Số lượng (SL)",
                        valueText = "${printerState.colQtyWeight}%",
                        currentValue = printerState.colQtyWeight,
                        valueRange = 10f..30f,
                        onValueChange = { newVal ->
                            viewModel.updatePrinterSettings(printerState.copy(colQtyWeight = newVal))
                        }
                    )

                    SliderSettingRow(
                        label = "Cột Đơn giá",
                        valueText = "${printerState.colPriceWeight}%",
                        currentValue = printerState.colPriceWeight,
                        valueRange = 12f..35f,
                        onValueChange = { newVal ->
                            viewModel.updatePrinterSettings(printerState.copy(colPriceWeight = newVal))
                        }
                    )

                    SliderSettingRow(
                        label = "Cột Thành tiền",
                        valueText = "${printerState.colTotalWeight}%",
                        currentValue = printerState.colTotalWeight,
                        valueRange = 15f..40f,
                        onValueChange = { newVal ->
                            viewModel.updatePrinterSettings(printerState.copy(colTotalWeight = newVal))
                        }
                    )

                    Divider()

                    Text(
                        text = "Tiêu đề:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                    OutlinedTextField(
                        value = printerState.headerText,
                        onValueChange = {
                            viewModel.updatePrinterSettings(printerState.copy(headerText = it))
                        },
                        label = { Text("Tiêu đề (Dòng viết HOA toàn bộ sẽ in chữ TO)") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 4. CÀI ĐẶT NGÂN HÀNG & MÃ QR CHUYỂN KHOẢN (ĐẶT Ở DƯỚI CÙNG)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = AfadaBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ngân hàng & Mã QR chuyển khoản",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = AfadaBluePrimary
                        )
                    }

                    Text(
                        text = "1. Chọn ngân hàng:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Surface(
                        color = Color(0xFFF5F7FA),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AfadaBluePrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { showBankSearchDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = printerState.bankName.ifBlank { "Chạm để tìm ngân hàng" },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AfadaBluePrimary
                                )
                                Text(
                                    text = "Mã BIN VietQR: ${printerState.bankBin}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Tìm NH",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AfadaBluePrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = AfadaBluePrimary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = printerState.bankAccount,
                        onValueChange = { acc ->
                            viewModel.updatePrinterSettings(
                                printerState.copy(bankAccount = acc.filter { it.isLetterOrDigit() })
                            )
                        },
                        label = { Text("2. Số tài khoản ngân hàng") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (printerState.bankAccount.isNotBlank()) {
                        OutlinedTextField(
                            value = printerState.bankOwner,
                            onValueChange = { owner ->
                                viewModel.updatePrinterSettings(
                                    printerState.copy(bankOwner = owner.uppercase())
                                )
                            },
                            label = { Text("3. Tên chủ tài khoản (In hoa không dấu)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 2.dp))

                    Text(
                        text = "Chế độ tạo mã QR:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (printerState.qrIncludeAmount) Color(0xFFE8F5E9) else Color(0xFFF5F7FA))
                            .clickable {
                                viewModel.updatePrinterSettings(printerState.copy(qrIncludeAmount = true))
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = printerState.qrIncludeAmount,
                            onClick = {
                                viewModel.updatePrinterSettings(printerState.copy(qrIncludeAmount = true))
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = AfadaGreenSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "QR theo số tiền trên hóa đơn",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!printerState.qrIncludeAmount) Color(0xFFE8F5E9) else Color(0xFFF5F7FA))
                            .clickable {
                                viewModel.updatePrinterSettings(printerState.copy(qrIncludeAmount = false))
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !printerState.qrIncludeAmount,
                            onClick = {
                                viewModel.updatePrinterSettings(printerState.copy(qrIncludeAmount = false))
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = AfadaGreenSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "QR chỉ quét ra tài khoản ngân hàng",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { customQrPickerLauncher.launch(arrayOf("image/*")) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (printerState.customQrImageUri.isBlank()) "Hoặc tải ảnh QR từ máy" else "Đổi ảnh QR từ máy",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (printerState.customQrImageUri.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.updatePrinterSettings(printerState.copy(customQrImageUri = ""))
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Dùng VietQR tự động", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBankSearchDialog) {
        BankSearchDialog(
            onSelectBank = { bank ->
                viewModel.updatePrinterSettings(
                    printerState.copy(
                        bankName = bank.shortName,
                        bankBin = bank.bin
                    )
                )
                showBankSearchDialog = false
            },
            onDismiss = { showBankSearchDialog = false }
        )
    }
}

@Composable
private fun BankSearchDialog(
    onSelectBank: (VietnamBankInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredBanks = remember(searchQuery) {
        VietQrGenerator.POPULAR_BANKS.filter {
            searchQuery.isBlank() ||
                it.shortName.contains(searchQuery, ignoreCase = true) ||
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.bin.contains(searchQuery)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tìm kiếm Ngân hàng", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Gõ tên NH (VD: Vietcombank, MB, BIDV...)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier.height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBanks, key = { it.bin }) { bank ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectBank(bank) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = bank.shortName,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = AfadaBluePrimary
                                )
                                Text(
                                    text = "${bank.fullName} • BIN: ${bank.bin}",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SliderSettingRow(
    label: String,
    valueText: String,
    currentValue: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            Text(
                text = valueText,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AfadaBluePrimary
            )
        }
        Slider(
            value = currentValue.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = AfadaBluePrimary,
                activeTrackColor = AfadaBluePrimary
            ),
            modifier = Modifier.height(32.dp)
        )
    }
}

@Composable
private fun OptionSelectorRow(
    label: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E),
            modifier = Modifier.weight(0.42f)
        )

        Row(
            modifier = Modifier.weight(0.58f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val selected = option == selectedOption
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) AfadaBluePrimary else Color(0xFFF5F7FA))
                        .border(
                            1.dp,
                            if (selected) AfadaBluePrimary else Color(0xFFE0E5EC),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else Color.DarkGray
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun loadPairedBluetoothPrinters(context: Context): List<PairedBtPrinter> {
    return try {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter: BluetoothAdapter? = btManager?.adapter
        val bonded: Set<BluetoothDevice>? = adapter?.bondedDevices
        bonded?.map { device ->
            PairedBtPrinter(
                name = device.name ?: "Máy in Bluetooth",
                address = device.address ?: ""
            )
        } ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}

@SuppressLint("MissingPermission")
private suspend fun sendEscPosTestTicketViaBluetooth(
    context: Context,
    macAddress: String,
    storeName: String,
    printerState: PrinterSettingsState
) = withContext(Dispatchers.IO) {
    try {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = btManager?.adapter ?: return@withContext
        val device = adapter.getRemoteDevice(macAddress) ?: return@withContext
        val sppUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val socket = device.createRfcommSocketToServiceRecord(sppUuid)
        socket.connect()
        socket.outputStream.use { out ->
            out.write(byteArrayOf(0x1B, 0x40))
            val text = buildString {
                printerState.headerText.lines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .ifEmpty { listOf(storeName.uppercase()) }
                    .forEach { appendLine(it) }
                appendLine("+----------------+--------+-------+-------+")
                appendLine("| Ten hang       |   SL   | D.Gia | T.Tien|")
                appendLine("+----------------+--------+-------+-------+")
                appendLine("| Bia Ha Noi lon |1 thung |250.000|250.000|")
                appendLine("| Bia Ha Noi lon | 2 loc  | 65.000|130.000|")
                appendLine("+----------------+--------+-------+-------+")
                appendLine("TONG TIEN: 380.000")
                appendLine("--------------------------------------")
                appendLine(printerState.bankName.uppercase())
                appendLine("STK: ${printerState.bankAccount}")
                if (printerState.bankOwner.isNotBlank()) {
                    appendLine(printerState.bankOwner.uppercase())
                }
                appendLine("\n\n")
            }
            out.write(text.toByteArray(Charsets.UTF_8))
            out.write(byteArrayOf(0x1D, 0x56, 0x42, 0x00))
            out.flush()
        }
        socket.close()
    } catch (_: Exception) {
    }
}
