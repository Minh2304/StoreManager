package com.storemanager.afada.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.storemanager.afada.data.local.CustomerEntity
import com.storemanager.afada.data.local.DebtTransactionEntity
import com.storemanager.afada.data.local.OrderWithItems
import com.storemanager.afada.data.local.ProductEntity
import com.storemanager.afada.data.local.ProductUnitEntity
import com.storemanager.afada.data.local.ProductWithUnits
import com.storemanager.afada.data.local.StoreDatabaseEntity
import com.storemanager.afada.data.repository.CartItem
import com.storemanager.afada.data.repository.StoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AuthUiState(
    val loggedInEmail: String = "",
    val currentDbId: String = "",
    val currentDbName: String = "",
    val currentDbSpreadsheetUrl: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val otpSent: Boolean = false
)

data class AppSettingsState(
    val manageStockEnabled: Boolean = false
)

data class AppNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeText: String,
    val isUpdateNotification: Boolean = false
)

data class AppUpdateUiState(
    val currentVersionCode: Int = StoreRepository.CURRENT_VERSION_CODE,
    val currentVersionName: String = StoreRepository.CURRENT_VERSION_NAME,
    val latestVersionCode: Int = StoreRepository.CURRENT_VERSION_CODE,
    val latestVersionName: String = StoreRepository.CURRENT_VERSION_NAME,
    val releaseDate: String = "18/11/2026",
    val apkDownloadUrl: String = StoreRepository.DEFAULT_APK_DOWNLOAD_URL,
    val releaseNotes: String = "Quản lý bán hàng đồ uống, tự động tạo Google Sheet theo cửa hàng, in hóa đơn Bluetooth kèm mã QR VietQR chuẩn NAPAS.",
    val isChecking: Boolean = false,
    val isDownloadingApk: Boolean = false,
    val hasNewUpdate: Boolean = false,
    val checkMessage: String? = null
)

data class PrinterSettingsState(
    val bluetoothDeviceName: String = "Chưa chọn máy in Bluetooth",
    val bluetoothDeviceAddress: String = "",
    val paperSize: String = "K80 (80mm)", // "K58 (58mm)" hoặc "K80 (80mm)"
    val fontScale: Int = 12, // Thanh kéo cỡ chữ trên hóa đơn (9sp -> 16sp)
    val colNameWeight: Int = 44,  // Tỷ lệ độ rộng cột Tên hàng (%)
    val colQtyWeight: Int = 16,   // Tỷ lệ độ rộng cột SL (%)
    val colPriceWeight: Int = 20, // Tỷ lệ độ rộng cột Đơn giá (%)
    val colTotalWeight: Int = 20, // Tỷ lệ độ rộng cột Thành tiền (%)
    val headerText: String = "ĐẠI LÝ BIA NƯỚC NGỌT\nĐC: 125 Nguyễn Văn Linh\nĐT: 0909 123 456\nHÓA ĐƠN BÁN HÀNG",
    val bankBin: String = "970436",
    val bankName: String = "Vietcombank",
    val bankAccount: String = "0123456789",
    val bankOwner: String = "NGUYEN VAN A",
    val customQrImageUri: String = "",
    val qrIncludeAmount: Boolean = true,
    val statusMessage: String? = null
)

data class PosUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "Tất cả",
    val cart: List<CartItem> = emptyList(),
    val selectedCustomer: CustomerEntity? = null,
    val discountAmount: Double = 0.0,
    val lastCreatedOrder: OrderWithItems? = null
) {
    val totalCartAmount: Double get() = cart.sumOf { it.lineTotal }
    val finalPayableAmount: Double get() = (totalCartAmount - discountAmount).coerceAtLeast(0.0)
    val totalCartItemCount: Int get() = cart.sumOf { it.quantity }
}

data class SyncStatusState(
    val isSyncing: Boolean = false,
    val statusMessage: String? = null,
    val isError: Boolean = false,
    val webAppUrl: String = "",
    val lastSyncTime: Long = 0L
)

@OptIn(ExperimentalCoroutinesApi::class)
class StoreViewModel(
    private val repository: StoreRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(
        AuthUiState(
            loggedInEmail = repository.getLoggedInEmail(),
            currentDbId = repository.getCurrentDbId(),
            currentDbName = repository.getCurrentDbName(),
            currentDbSpreadsheetUrl = repository.getCurrentDbSpreadsheetUrl()
        )
    )
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _settingsState = MutableStateFlow(
        AppSettingsState(
            manageStockEnabled = repository.isManageStockEnabled()
        )
    )
    val settingsState: StateFlow<AppSettingsState> = _settingsState.asStateFlow()

    private val _updateState = MutableStateFlow(AppUpdateUiState())
    val updateState: StateFlow<AppUpdateUiState> = _updateState.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotificationItem>>(emptyList())
    val notifications: StateFlow<List<AppNotificationItem>> = _notifications.asStateFlow()

    private val _printerState = MutableStateFlow(
        PrinterSettingsState(
            bluetoothDeviceName = repository.getPrinterDeviceName(),
            bluetoothDeviceAddress = repository.getPrinterDeviceAddress(),
            paperSize = repository.getPrinterPaperSize(),
            fontScale = repository.getPrinterFontScale(),
            colNameWeight = repository.getColNameWeight(),
            colQtyWeight = repository.getColQtyWeight(),
            colPriceWeight = repository.getColPriceWeight(),
            colTotalWeight = repository.getColTotalWeight(),
            headerText = repository.getReceiptHeaderText(),
            bankBin = repository.getReceiptBankBin(),
            bankName = repository.getReceiptBankName(),
            bankAccount = repository.getReceiptBankAccount(),
            bankOwner = repository.getReceiptBankOwner(),
            customQrImageUri = repository.getCustomQrImageUri(),
            qrIncludeAmount = repository.isQrIncludeAmount()
        )
    )
    val printerState: StateFlow<PrinterSettingsState> = _printerState.asStateFlow()

    private val _activeEmail = MutableStateFlow(repository.getLoggedInEmail())
    private val _activeDbId = MutableStateFlow(repository.getCurrentDbId())

    val userDatabases: StateFlow<List<StoreDatabaseEntity>> = _activeEmail
        .flatMapLatest { email ->
            if (email.isBlank()) flowOf(emptyList())
            else repository.observeDatabasesByEmail(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productsWithUnits: StateFlow<List<ProductWithUnits>> = _activeDbId
        .flatMapLatest { dbId ->
            if (dbId.isBlank()) flowOf(emptyList())
            else repository.observeProductsWithUnits(dbId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = _activeDbId
        .flatMapLatest { dbId ->
            if (dbId.isBlank()) flowOf(emptyList())
            else repository.observeCustomers(dbId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ordersWithItems: StateFlow<List<OrderWithItems>> = _activeDbId
        .flatMapLatest { dbId ->
            if (dbId.isBlank()) flowOf(emptyList())
            else repository.observeOrdersWithItems(dbId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debtTransactions: StateFlow<List<DebtTransactionEntity>> = _activeDbId
        .flatMapLatest { dbId ->
            if (dbId.isBlank()) flowOf(emptyList())
            else repository.observeDebtTransactions(dbId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _posState = MutableStateFlow(PosUiState())
    val posState: StateFlow<PosUiState> = _posState.asStateFlow()

    private val _syncState = MutableStateFlow(
        SyncStatusState(
            webAppUrl = repository.getWebAppUrl(),
            lastSyncTime = repository.getLastSyncTime()
        )
    )
    val syncState: StateFlow<SyncStatusState> = _syncState.asStateFlow()

    init {
        val savedEmail = repository.getLoggedInEmail()
        if (savedEmail.isNotBlank()) {
            viewModelScope.launch {
                repository.refreshDatabasesFromCloud(savedEmail)
            }
        }
        checkForGitReleaseUpdate(silent = true)
    }

    /**
     * Kiểm tra phiên bản mới từ nhánh git `release` (version.json trên GitHub):
     * Nếu `versionCode` trên nhánh `release` lớn hơn bản hiện tại -> Bật cờ `hasNewUpdate = true`
     * để chấm đỏ thông báo hiện lên ở icon chuông bên trái nút Cài đặt!
     */
    fun checkForGitReleaseUpdate(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _updateState.update { it.copy(isChecking = true, checkMessage = "Đang kiểm tra cập nhật...") }
            }
            val result = repository.fetchRemoteReleaseVersion()
            result.onSuccess { remote ->
                val isNewer = remote.versionCode > StoreRepository.CURRENT_VERSION_CODE ||
                    (remote.versionName.isNotBlank() && remote.versionName != StoreRepository.CURRENT_VERSION_NAME)
                val newVerName = remote.versionName.ifBlank { StoreRepository.CURRENT_VERSION_NAME }
                _updateState.update {
                    it.copy(
                        latestVersionCode = remote.versionCode,
                        latestVersionName = newVerName,
                        releaseDate = remote.releaseDate.ifBlank { it.releaseDate },
                        apkDownloadUrl = remote.apkUrl.ifBlank { StoreRepository.DEFAULT_APK_DOWNLOAD_URL },
                        releaseNotes = remote.releaseNotes.ifBlank { it.releaseNotes },
                        isChecking = false,
                        hasNewUpdate = isNewer,
                        checkMessage = if (isNewer) {
                            "Đã có phiên bản mới v$newVerName"
                        } else if (!silent) {
                            "Đang là phiên bản mới nhất"
                        } else null
                    )
                }

                // Nếu có phiên bản mới, tự động đẩy thông báo vào danh sách Thông báo
                if (isNewer) {
                    val notifId = "UPDATE_${remote.versionCode}_$newVerName"
                    _notifications.update { currentList ->
                        if (currentList.any { it.id == notifId }) currentList
                        else listOf(
                            AppNotificationItem(
                                id = notifId,
                                title = "Có phiên bản mới v$newVerName",
                                message = remote.releaseNotes.ifBlank { "Nhấn Cập nhật để tự động tải và cài đặt phiên bản mới." },
                                timeText = remote.releaseDate,
                                isUpdateNotification = true
                            )
                        ) + currentList
                    }
                }
            }.onFailure {
                if (!silent) {
                    _updateState.update {
                        it.copy(
                            isChecking = false,
                            checkMessage = "Đang dùng phiên bản mới nhất"
                        )
                    }
                }
            }
        }
    }

    fun dismissNotification(id: String) {
        _notifications.update { list -> list.filterNot { it.id == id } }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    /**
     * Tự động tải file APK từ nhánh release về điện thoại và mở trình cài đặt tự động của Android
     */
    fun startAutoUpdateApk(context: Context) {
        val url = _updateState.value.apkDownloadUrl.ifBlank { StoreRepository.DEFAULT_APK_DOWNLOAD_URL }
        _updateState.update { it.copy(isDownloadingApk = true, checkMessage = "Đang tải bản cập nhật...") }

        try {
            val fileName = "AFaDa-StoreManager-${System.currentTimeMillis()}.apk"
            val outFile = java.io.File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Cập nhật AFaDa Đồ Uống")
                .setDescription("Đang tải phiên bản v${_updateState.value.latestVersionName}...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(outFile))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = dm.enqueue(request)

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                    if (id == downloadId) {
                        try {
                            context.applicationContext.unregisterReceiver(this)
                        } catch (_: Exception) {
                        }
                        _updateState.update {
                            it.copy(
                                isDownloadingApk = false,
                                checkMessage = "Đã tải xong! Đang mở cài đặt..."
                            )
                        }
                        if (outFile.exists()) {
                            val apkUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                outFile
                            )
                            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(apkUri, "application/vnd.android.package-archive")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(installIntent)
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.applicationContext.registerReceiver(
                    receiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_EXPORTED
                )
            } else {
                context.applicationContext.registerReceiver(
                    receiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }
        } catch (e: Exception) {
            _updateState.update {
                it.copy(
                    isDownloadingApk = false,
                    checkMessage = "Không thể tải tự động: ${e.message}"
                )
            }
        }
    }

    // --- CÀI ĐẶT: QUẢN LÝ NHẬP HÀNG (BẬT/TẮT TỒN KHO) & MÁY IN NHIỆT BLUETOOTH ---
    fun toggleManageStock(enabled: Boolean) {
        repository.setManageStockEnabled(enabled)
        _settingsState.update { it.copy(manageStockEnabled = enabled) }
    }

    fun updatePrinterSettings(updated: PrinterSettingsState, showSavedToast: Boolean = false) {
        repository.savePrinterSettings(
            btName = updated.bluetoothDeviceName,
            btAddress = updated.bluetoothDeviceAddress,
            paperSize = updated.paperSize,
            fontScale = updated.fontScale,
            colNameWeight = updated.colNameWeight,
            colQtyWeight = updated.colQtyWeight,
            colPriceWeight = updated.colPriceWeight,
            colTotalWeight = updated.colTotalWeight,
            headerText = updated.headerText,
            bankBin = updated.bankBin,
            bankName = updated.bankName,
            bankAccount = updated.bankAccount,
            bankOwner = updated.bankOwner,
            customQrUri = updated.customQrImageUri,
            qrIncludeAmount = updated.qrIncludeAmount
        )
        _printerState.value = updated.copy(
            statusMessage = if (showSavedToast) "Đã lưu cài đặt máy in nhiệt Bluetooth!" else updated.statusMessage
        )
    }

    fun selectBluetoothPrinter(name: String, address: String) {
        val current = _printerState.value
        val next = current.copy(
            bluetoothDeviceName = name,
            bluetoothDeviceAddress = address,
            statusMessage = "Đã kết nối máy in Bluetooth: $name ($address)"
        )
        updatePrinterSettings(next)
    }

    // --- 1. ĐĂNG NHẬP / ĐĂNG KÝ / QUÊN MẬT KHẨU ---
    fun resetOtpStage() {
        _authState.update { it.copy(otpSent = false, message = null, isError = false) }
    }

    fun setAuthErrorMessage(msg: String) {
        _authState.update { it.copy(isLoading = false, message = msg, isError = true) }
    }

    fun loginWithGmail(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, message = null, isError = false) }
            val result = repository.loginWithGmail(email, password)
            result.onSuccess {
                val cleanEmail = email.trim().lowercase()
                _activeEmail.value = cleanEmail
                _authState.update {
                    it.copy(
                        loggedInEmail = cleanEmail,
                        isLoading = false,
                        otpSent = false,
                        message = null,
                        isError = false
                    )
                }
                repository.refreshDatabasesFromCloud(cleanEmail)
                onSuccess()
            }.onFailure { err ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        message = err.message ?: "Không thể đăng nhập",
                        isError = true
                    )
                }
            }
        }
    }

    /**
     * Đăng nhập & Xác thực chính chủ bằng Google OAuth2 (Cấp quyền tạo Google Sheet trên Google Drive)
     */
    fun loginWithVerifiedGoogleOAuth(email: String, displayName: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, message = null, isError = false) }
            val result = repository.loginWithVerifiedGoogleAccount(email, displayName)
            result.onSuccess {
                val cleanEmail = email.trim().lowercase()
                _activeEmail.value = cleanEmail
                _authState.update {
                    it.copy(
                        loggedInEmail = cleanEmail,
                        isLoading = false,
                        otpSent = false,
                        message = null,
                        isError = false
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        message = err.message ?: "Xác thực Google thất bại",
                        isError = true
                    )
                }
            }
        }
    }

    fun sendOtpCode(email: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, message = null, isError = false) }
            val result = repository.sendVerificationOtp(email)
            result.onSuccess { msg ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        otpSent = true,
                        message = msg,
                        isError = false
                    )
                }
            }.onFailure { err ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        message = err.message ?: "Không gửi được mã xác thực",
                        isError = true
                    )
                }
            }
        }
    }

    fun registerWithOtp(email: String, password: String, otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, message = null, isError = false) }
            val result = repository.registerWithOtp(email, password, otp)
            result.onSuccess {
                val cleanEmail = email.trim().lowercase()
                _activeEmail.value = cleanEmail
                _authState.update {
                    it.copy(
                        loggedInEmail = cleanEmail,
                        isLoading = false,
                        otpSent = false,
                        message = null,
                        isError = false
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        message = err.message ?: "Lỗi đăng ký",
                        isError = true
                    )
                }
            }
        }
    }

    fun resetPasswordWithOtp(email: String, newPassword: String, otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, message = null, isError = false) }
            val result = repository.resetPasswordWithOtp(email, newPassword, otp)
            result.onSuccess { msg ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        otpSent = false,
                        message = msg,
                        isError = false
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        message = err.message ?: "Không đổi được mật khẩu",
                        isError = true
                    )
                }
            }
        }
    }

    // --- 2. CHỌN HOẶC TẠO DATABASE MỚI TRÊN GMAIL ---
    fun createNewDatabase(dbName: String, onCreated: (StoreDatabaseEntity) -> Unit = {}) {
        val email = _authState.value.loggedInEmail
        if (email.isBlank()) return
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, message = null, isError = false) }
            val res = repository.createNewStoreDatabase(email, dbName)
            res.onSuccess { newDb ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        message = "Đã tự động tạo Sheet '${newDb.dbName}' trên Gmail!",
                        isError = false
                    )
                }
                onCreated(newDb)
            }.onFailure { err ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        message = err.message ?: "Không tạo được dữ liệu",
                        isError = true
                    )
                }
            }
        }
    }

    /**
     * Xóa Cửa hàng khi người dùng nhấn giữ vào cửa hàng và nhập đúng mật khẩu đăng nhập
     */
    fun deleteDatabaseWithPassword(
        db: StoreDatabaseEntity,
        passwordInput: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val email = _authState.value.loggedInEmail
        if (email.isBlank()) return
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, message = null, isError = false) }
            val res = repository.deleteStoreDatabaseWithPassword(email, passwordInput, db)
            res.onSuccess { msg ->
                if (_activeDbId.value == db.dbId) {
                    _activeDbId.value = ""
                }
                _authState.update {
                    it.copy(
                        currentDbId = if (it.currentDbId == db.dbId) "" else it.currentDbId,
                        currentDbName = if (it.currentDbId == db.dbId) "" else it.currentDbName,
                        isLoading = false,
                        message = msg,
                        isError = false
                    )
                }
                onSuccess()
            }.onFailure { err ->
                val errMsg = err.message ?: "Mật khẩu không đúng!"
                _authState.update {
                    it.copy(
                        isLoading = false,
                        message = errMsg,
                        isError = true
                    )
                }
                onError(errMsg)
            }
        }
    }

    fun selectDatabase(db: StoreDatabaseEntity, onReady: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            repository.selectDatabase(db)
            val resolvedSheetUrl = repository.getCurrentDbSpreadsheetUrl()
            _activeDbId.value = db.dbId
            _posState.value = PosUiState()
            _authState.update {
                it.copy(
                    currentDbId = db.dbId,
                    currentDbName = db.dbName,
                    currentDbSpreadsheetUrl = resolvedSheetUrl,
                    isLoading = false
                )
            }
            onReady()
        }
    }

    fun switchDatabaseScreen() {
        repository.saveCurrentDb("", "", "")
        _activeDbId.value = ""
        _authState.update { it.copy(currentDbId = "", currentDbName = "", currentDbSpreadsheetUrl = "") }
    }

    fun logout() {
        repository.logout()
        _activeEmail.value = ""
        _activeDbId.value = ""
        _posState.value = PosUiState()
        _authState.value = AuthUiState()
    }

    // --- 3. POS ACTIONS (ĐIỀU CHỈNH SỐ LƯỢNG - [ 0 ] + CHO TỪNG LOẠI ĐƠN VỊ) ---
    fun updatePosSearch(query: String) {
        _posState.update { it.copy(searchQuery = query) }
    }

    fun selectPosCategory(category: String) {
        _posState.update { it.copy(selectedCategory = category) }
    }

    /**
     * Đặt số lượng trực tiếp cho 1 dòng mặt hàng (Product + Unit cụ thể):
     * - Nếu newQty <= 0: Xóa khỏi giỏ hàng
     * - Nếu chưa có trong giỏ và newQty > 0: Thêm mới vào giỏ
     * - Nếu đã có trong giỏ: Cập nhật số lượng mới
     */
    fun setCartQuantityForProductUnit(
        product: ProductEntity,
        unit: ProductUnitEntity,
        newQty: Int
    ) {
        _posState.update { state ->
            val existingIdx = state.cart.indexOfFirst {
                it.product.productId == product.productId && it.selectedUnit.unitId == unit.unitId
            }
            val updatedCart = when {
                newQty <= 0 -> {
                    state.cart.filterNot {
                        it.product.productId == product.productId && it.selectedUnit.unitId == unit.unitId
                    }
                }
                existingIdx >= 0 -> {
                    state.cart.toMutableList().apply {
                        this[existingIdx] = this[existingIdx].copy(quantity = newQty)
                    }
                }
                else -> {
                    state.cart + CartItem(
                        product = product,
                        selectedUnit = unit,
                        availableUnits = listOf(unit),
                        quantity = newQty
                    )
                }
            }
            state.copy(cart = updatedCart)
        }
    }

    fun updateCartItemQuantity(productId: String, unitId: String, newQty: Int) {
        _posState.update { state ->
            val updatedCart = if (newQty <= 0) {
                state.cart.filterNot { it.product.productId == productId && it.selectedUnit.unitId == unitId }
            } else {
                state.cart.map {
                    if (it.product.productId == productId && it.selectedUnit.unitId == unitId) {
                        it.copy(quantity = newQty)
                    } else it
                }
            }
            state.copy(cart = updatedCart)
        }
    }

    fun selectPosCustomer(customer: CustomerEntity) {
        _posState.update { it.copy(selectedCustomer = customer) }
    }

    fun clearCart() {
        _posState.update { it.copy(cart = emptyList(), discountAmount = 0.0) }
    }

    fun dismissReceiptDialog() {
        _posState.update { it.copy(lastCreatedOrder = null) }
    }

    fun checkoutOrder(
        paidAmount: Double,
        paymentMethod: String,
        note: String
    ) {
        val dbId = _activeDbId.value
        val state = _posState.value
        if (dbId.isBlank() || state.cart.isEmpty()) return

        val defaultCustomer = state.selectedCustomer
            ?: customers.value.firstOrNull { it.customerId.endsWith("KH_LE") }
            ?: CustomerEntity("${dbId}_KH_LE", dbId, "Khách lẻ", "", "")

        viewModelScope.launch {
            val createdOrder = repository.createPosOrder(
                dbId = dbId,
                customer = defaultCustomer,
                cartItems = state.cart,
                discount = state.discountAmount,
                paidAmount = paidAmount,
                paymentMethod = paymentMethod,
                note = note
            )
            _posState.update {
                it.copy(
                    cart = emptyList(),
                    discountAmount = 0.0,
                    lastCreatedOrder = createdOrder
                )
            }
            if (repository.getWebAppUrl().isNotBlank()) {
                syncPushToGoogleSheets(silent = true)
            }
        }
    }

    // --- 4. INVENTORY & CUSTOMER ACTIONS ---
    fun saveProductWithUnits(product: ProductEntity, units: List<ProductUnitEntity>) {
        val dbId = _activeDbId.value
        if (dbId.isBlank()) return
        viewModelScope.launch {
            repository.saveProductWithUnits(dbId, product, units)
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun importStockByUnit(product: ProductEntity, unit: ProductUnitEntity, qty: Int) {
        if (qty <= 0) return
        viewModelScope.launch {
            repository.importStockByUnit(product, unit, qty)
        }
    }

    fun saveCustomer(customer: CustomerEntity) {
        val dbId = _activeDbId.value
        if (dbId.isBlank()) return
        viewModelScope.launch {
            repository.saveCustomer(dbId, customer)
        }
    }

    fun collectDebt(customer: CustomerEntity, payAmount: Double, crateNote: String, note: String) {
        val dbId = _activeDbId.value
        if (dbId.isBlank()) return
        viewModelScope.launch {
            repository.collectCustomerDebt(dbId, customer, payAmount, crateNote, note)
        }
    }

    // --- 5. GOOGLE SHEETS SYNC ACTIONS ---
    fun updateWebAppUrl(url: String) {
        repository.saveWebAppUrl(url)
        _syncState.update {
            it.copy(
                webAppUrl = url.trim(),
                statusMessage = "Đã lưu link kết nối Google Sheets!",
                isError = false
            )
        }
    }

    fun syncPushToGoogleSheets(silent: Boolean = false) {
        val dbId = _activeDbId.value
        if (dbId.isBlank()) return
        viewModelScope.launch {
            if (!silent) {
                _syncState.update { it.copy(isSyncing = true, statusMessage = "Đang lưu lên Google Sheets...", isError = false) }
            }
            val result = repository.pushAllToGoogleSheets(dbId)
            result.onSuccess { msg ->
                _syncState.update {
                    it.copy(
                        isSyncing = false,
                        statusMessage = msg,
                        isError = false,
                        lastSyncTime = repository.getLastSyncTime()
                    )
                }
            }.onFailure { err ->
                if (!silent) {
                    _syncState.update {
                        it.copy(
                            isSyncing = false,
                            statusMessage = "Lỗi: ${err.message}",
                            isError = true
                        )
                    }
                }
            }
        }
    }

    fun syncPullFromGoogleSheets() {
        val dbId = _activeDbId.value
        if (dbId.isBlank()) return
        viewModelScope.launch {
            _syncState.update { it.copy(isSyncing = true, statusMessage = "Đang tải từ Google Sheets...", isError = false) }
            val result = repository.pullAllFromGoogleSheets(dbId)
            result.onSuccess { msg ->
                _syncState.update {
                    it.copy(
                        isSyncing = false,
                        statusMessage = msg,
                        isError = false,
                        lastSyncTime = repository.getLastSyncTime()
                    )
                }
            }.onFailure { err ->
                _syncState.update {
                    it.copy(
                        isSyncing = false,
                        statusMessage = "Lỗi: ${err.message}",
                        isError = true
                    )
                }
            }
        }
    }

    fun resetSampleData() {
        val dbId = _activeDbId.value
        if (dbId.isBlank()) return
        viewModelScope.launch {
            repository.loadSampleBeverageData(dbId)
            _syncState.update {
                it.copy(
                    statusMessage = "Đã nạp lại danh sách Bia - Nước ngọt mẫu!",
                    isError = false
                )
            }
        }
    }

    class Factory(private val repository: StoreRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StoreViewModel(repository) as T
        }
    }
}

object FormatUtils {
    private val currencyFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun formatNumberPlain(amount: Double): String {
        return currencyFormat.format(amount.toLong())
    }

    fun formatVnd(amount: Double): String {
        return "${currencyFormat.format(amount.toLong())}đ"
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0L) return "Chưa lưu"
        return dateTimeFormat.format(Date(timestamp))
    }

    fun shareOrderReceipt(context: Context, orderWithItems: OrderWithItems) {
        val o = orderWithItems.order
        val sb = StringBuilder()
        sb.appendLine("====== HÓA ĐƠN BÁN HÀNG ======")
        sb.appendLine("Mã đơn: #${o.orderId}")
        sb.appendLine("Ngày: ${formatDateTime(o.orderDate)}")
        sb.appendLine("Khách hàng: ${o.customerName}")
        sb.appendLine("------------------------------")
        orderWithItems.items.forEachIndexed { idx, item ->
            sb.appendLine("${idx + 1}. ${item.productName} (${item.unitName})")
            sb.appendLine("   ${item.quantity} ${item.unitName} x ${formatVnd(item.unitPrice)} = ${formatVnd(item.lineTotal)}")
        }
        sb.appendLine("------------------------------")
        sb.appendLine("TỔNG TIỀN: ${formatVnd(o.finalAmount)}")
        sb.appendLine("Đã trả (${o.paymentMethod}): ${formatVnd(o.paidAmount)}")
        if (o.debtAmount > 0) {
            sb.appendLine("CÒN NỢ: ${formatVnd(o.debtAmount)}")
        }
        if (o.note.isNotBlank()) {
            sb.appendLine("Vỏ két / Ghi chú: ${o.note}")
        }
        sb.appendLine("==============================")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Gửi hóa đơn qua Zalo"))
    }
}
