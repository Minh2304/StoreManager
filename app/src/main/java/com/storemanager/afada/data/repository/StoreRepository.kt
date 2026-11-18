package com.storemanager.afada.data.repository

import android.content.Context
import com.storemanager.afada.data.local.CustomerEntity
import com.storemanager.afada.data.local.DebtTransactionEntity
import com.storemanager.afada.data.local.OrderEntity
import com.storemanager.afada.data.local.OrderItemEntity
import com.storemanager.afada.data.local.OrderWithItems
import com.storemanager.afada.data.local.ProductEntity
import com.storemanager.afada.data.local.ProductUnitEntity
import com.storemanager.afada.data.local.ProductWithUnits
import com.storemanager.afada.data.local.SeedData
import com.storemanager.afada.data.local.StoreDao
import com.storemanager.afada.data.local.StoreDatabaseEntity
import com.storemanager.afada.data.local.UserAccountEntity
import com.storemanager.afada.data.remote.GoogleSheetsApi
import com.storemanager.afada.data.remote.RemoteAppVersionDto
import com.storemanager.afada.data.remote.SheetSyncPayload
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class CartItem(
    val product: ProductEntity,
    val selectedUnit: ProductUnitEntity,
    val availableUnits: List<ProductUnitEntity>,
    val quantity: Int
) {
    val lineTotal: Double get() = selectedUnit.sellPrice * quantity
    val totalBaseUnitsDeducted: Int get() = selectedUnit.conversionRate * quantity
}

class StoreRepository(
    private val dao: StoreDao,
    private val api: GoogleSheetsApi,
    context: Context
) {
    private val prefs = context.getSharedPreferences("afada_prefs", Context.MODE_PRIVATE)
    private val localOtpStore = mutableMapOf<String, String>()

    fun observeDatabasesByEmail(email: String): Flow<List<StoreDatabaseEntity>> =
        dao.observeDatabasesByEmail(email)

    fun observeProductsWithUnits(dbId: String): Flow<List<ProductWithUnits>> =
        dao.observeProductsWithUnits(dbId)

    fun observeCustomers(dbId: String): Flow<List<CustomerEntity>> =
        dao.observeCustomers(dbId)

    fun observeOrdersWithItems(dbId: String): Flow<List<OrderWithItems>> =
        dao.observeOrdersWithItems(dbId)

    fun observeDebtTransactions(dbId: String): Flow<List<DebtTransactionEntity>> =
        dao.observeDebtTransactions(dbId)

    // --- SESSION PREFS ---
    fun getLoggedInEmail(): String = prefs.getString("logged_in_email", "") ?: ""

    fun saveLoggedInEmail(email: String) {
        prefs.edit().putString("logged_in_email", email.trim().lowercase()).apply()
    }

    fun getCurrentDbId(): String = prefs.getString("current_db_id", "") ?: ""
    fun getCurrentDbName(): String = prefs.getString("current_db_name", "") ?: ""
    fun getCurrentDbSpreadsheetUrl(): String = prefs.getString("current_db_sheet_url", "") ?: ""

    fun saveCurrentDb(dbId: String, dbName: String, sheetUrl: String = "") {
        prefs.edit()
            .putString("current_db_id", dbId)
            .putString("current_db_name", dbName)
            .putString("current_db_sheet_url", sheetUrl)
            .apply()
    }

    fun logout() {
        prefs.edit()
            .remove("logged_in_email")
            .remove("current_db_id")
            .remove("current_db_name")
            .remove("current_db_sheet_url")
            .apply()
    }

    fun getWebAppUrl(): String = prefs.getString("sheet_webapp_url", "") ?: ""

    fun saveWebAppUrl(url: String) {
        prefs.edit().putString("sheet_webapp_url", url.trim()).apply()
    }

    fun getLastSyncTime(): Long = prefs.getLong("last_sync_time", 0L)

    private fun saveLastSyncTime(time: Long) {
        prefs.edit().putLong("last_sync_time", time).apply()
    }

    fun isManageStockEnabled(): Boolean = prefs.getBoolean("manage_stock_enabled", false)

    fun setManageStockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("manage_stock_enabled", enabled).apply()
    }

    // --- KIỂM TRA BẢN CẬP NHẬT TỪ XA TRÊN NHÁNH GIT RELEASE ---
    companion object {
        const val CURRENT_VERSION_CODE = 1
        const val CURRENT_VERSION_NAME = "1.0.0"
        const val GIT_REPO_URL = "https://github.com/Minh2304/StoreManager"
        const val GIT_RELEASE_BRANCH = "release"
        const val GIT_VERSION_JSON_URL = "https://raw.githubusercontent.com/Minh2304/StoreManager/release/version.json"
        const val DEFAULT_APK_DOWNLOAD_URL = "https://raw.githubusercontent.com/Minh2304/StoreManager/release/AFaDa-StoreManager.apk"
    }

    suspend fun fetchRemoteReleaseVersion(): Result<RemoteAppVersionDto> {
        return try {
            val urlWithCacheBuster = "$GIT_VERSION_JSON_URL?t=${System.currentTimeMillis()}"
            val remote = api.checkGitReleaseVersion(urlWithCacheBuster)
            Result.success(remote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- CẤU HÌNH MÁY IN NHIỆT BLUETOOTH ---
    fun getPrinterDeviceName(): String = prefs.getString("printer_bt_name", "Chưa chọn máy in Bluetooth") ?: "Chưa chọn máy in Bluetooth"
    fun getPrinterDeviceAddress(): String = prefs.getString("printer_bt_address", "") ?: ""
    fun getPrinterPaperSize(): String = prefs.getString("printer_paper_size", "K80 (80mm)") ?: "K80 (80mm)"
    fun getPrinterFontScale(): Int = prefs.getInt("printer_font_scale", 12)
    fun getColNameWeight(): Int = prefs.getInt("printer_col_name_weight", 44)
    fun getColQtyWeight(): Int = prefs.getInt("printer_col_qty_weight", 16)
    fun getColPriceWeight(): Int = prefs.getInt("printer_col_price_weight", 20)
    fun getColTotalWeight(): Int = prefs.getInt("printer_col_total_weight", 20)
    fun getReceiptHeaderText(): String = prefs.getString(
        "receipt_header_text",
        "ĐẠI LÝ BIA NƯỚC NGỌT\nĐC: 125 Nguyễn Văn Linh\nĐT: 0909 123 456\nHÓA ĐƠN BÁN HÀNG"
    ) ?: "ĐẠI LÝ BIA NƯỚC NGỌT\nĐC: 125 Nguyễn Văn Linh\nĐT: 0909 123 456\nHÓA ĐƠN BÁN HÀNG"
    fun getReceiptBankBin(): String = prefs.getString("receipt_bank_bin", "970436") ?: "970436"
    fun getReceiptBankName(): String = prefs.getString("receipt_bank_name", "Vietcombank") ?: "Vietcombank"
    fun getReceiptBankAccount(): String = prefs.getString("receipt_bank_account", "0123456789") ?: "0123456789"
    fun getReceiptBankOwner(): String = prefs.getString("receipt_bank_owner", "NGUYEN VAN A") ?: "NGUYEN VAN A"
    fun getCustomQrImageUri(): String = prefs.getString("receipt_custom_qr_uri", "") ?: ""
    fun isQrIncludeAmount(): Boolean = prefs.getBoolean("receipt_qr_include_amount", true)

    fun savePrinterSettings(
        btName: String,
        btAddress: String,
        paperSize: String,
        fontScale: Int,
        colNameWeight: Int,
        colQtyWeight: Int,
        colPriceWeight: Int,
        colTotalWeight: Int,
        headerText: String,
        bankBin: String,
        bankName: String,
        bankAccount: String,
        bankOwner: String,
        customQrUri: String,
        qrIncludeAmount: Boolean
    ) {
        prefs.edit()
            .putString("printer_bt_name", btName)
            .putString("printer_bt_address", btAddress)
            .putString("printer_paper_size", paperSize)
            .putInt("printer_font_scale", fontScale)
            .putInt("printer_col_name_weight", colNameWeight)
            .putInt("printer_col_qty_weight", colQtyWeight)
            .putInt("printer_col_price_weight", colPriceWeight)
            .putInt("printer_col_total_weight", colTotalWeight)
            .putString("receipt_header_text", headerText)
            .putString("receipt_bank_bin", bankBin.trim())
            .putString("receipt_bank_name", bankName.trim())
            .putString("receipt_bank_account", bankAccount.trim())
            .putString("receipt_bank_owner", bankOwner.trim().uppercase())
            .putString("receipt_custom_qr_uri", customQrUri.trim())
            .putBoolean("receipt_qr_include_amount", qrIncludeAmount)
            .apply()
    }

    // --- 1. ĐĂNG NHẬP / ĐĂNG KÝ / QUÊN MẬT KHẨU BẰNG GMAIL ---

    /**
     * Đăng nhập nhanh bằng Gmail + Mật khẩu (KHÔNG CẦN OTP để đăng nhập trên nhiều thiết bị)
     */
    suspend fun loginWithGmail(emailInput: String, passwordInput: String): Result<String> {
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập đúng địa chỉ Gmail!"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập mật khẩu!"))
        }

        val webUrl = getWebAppUrl()
        if (webUrl.isNotBlank()) {
            try {
                val res = api.postAction(
                    webUrl,
                    SheetSyncPayload(action = "login", email = email, password = password)
                )
                if (res.status == "success") {
                    dao.insertAccount(UserAccountEntity(email, password))
                    res.databases?.takeIf { it.isNotEmpty() }?.let { dbs ->
                        dao.insertDatabases(dbs)
                    }
                    saveLoggedInEmail(email)
                    return Result.success("Đăng nhập thành công!")
                }
            } catch (_: Exception) {
                // Nếu mất mạng hoặc lỗi kết nối, chuyển sang kiểm tra tài khoản Offline
            }
        }

        val localAcc = dao.getAccountByEmail(email)
        return if (localAcc != null) {
            if (localAcc.password == password) {
                saveLoggedInEmail(email)
                Result.success("Đăng nhập thành công!")
            } else {
                Result.failure(IllegalArgumentException("Sai mật khẩu! Nếu quên, hãy bấm 'Quên mật khẩu'."))
            }
        } else {
            // Cho phép đăng nhập nhanh lần đầu trên thiết bị mới mà không cần xác thực OTP
            dao.insertAccount(UserAccountEntity(email, password))
            saveLoggedInEmail(email)
            Result.success("Đăng nhập thành công!")
        }
    }

    /**
     * Gửi mã xác thực OTP 6 số (Chỉ dùng cho bước Đăng ký tài khoản mới hoặc Quên mật khẩu)
     */
    suspend fun sendVerificationOtp(emailInput: String): Result<String> {
        val email = emailInput.trim().lowercase()
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập đúng địa chỉ Gmail!"))
        }

        val generatedOtp = (100000..999999).random().toString()
        localOtpStore[email] = generatedOtp

        val webUrl = getWebAppUrl()
        if (webUrl.isNotBlank()) {
            try {
                val res = api.postAction(
                    webUrl,
                    SheetSyncPayload(action = "sendOtp", email = email)
                )
                if (res.status == "success") {
                    res.debugOtp?.let { localOtpStore[email] = it }
                    return Result.success(res.message ?: "Đã gửi mã 6 số vào Gmail $email")
                }
            } catch (_: Exception) {
                // Fallback hiển thị mã trên máy ảo nếu chưa kết nối mạng
            }
        }

        return Result.success("Mã xác thực Gmail của bạn là: $generatedOtp")
    }

    /**
     * Đăng ký Gmail mới (Cần xác thực mã OTP 6 số)
     */
    suspend fun registerWithOtp(emailInput: String, passwordInput: String, otpInput: String): Result<String> {
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()
        val otp = otpInput.trim()

        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập đúng Gmail!"))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("Mật khẩu cần ít nhất 4 ký tự!"))
        }
        if (otp.length != 6) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập mã xác thực 6 số!"))
        }

        val webUrl = getWebAppUrl()
        if (webUrl.isNotBlank()) {
            try {
                val res = api.postAction(
                    webUrl,
                    SheetSyncPayload(action = "register", email = email, password = password, otp = otp)
                )
                if (res.status == "success") {
                    dao.insertAccount(UserAccountEntity(email, password))
                    saveLoggedInEmail(email)
                    return Result.success("Đăng ký tài khoản thành công!")
                } else if (res.message != null && localOtpStore[email] != otp) {
                    return Result.failure(IllegalArgumentException(res.message))
                }
            } catch (_: Exception) {
                // Fallback xác thực OTP cục bộ
            }
        }

        val expectedOtp = localOtpStore[email]
        if (expectedOtp != null && expectedOtp != otp) {
            return Result.failure(IllegalArgumentException("Mã xác thực 6 số không đúng!"))
        }

        dao.insertAccount(UserAccountEntity(email, password))
        saveLoggedInEmail(email)
        return Result.success("Đăng ký tài khoản thành công!")
    }

    /**
     * Đặt lại mật khẩu mới khi Quên mật khẩu (Cần xác thực mã OTP 6 số)
     */
    suspend fun resetPasswordWithOtp(emailInput: String, newPasswordInput: String, otpInput: String): Result<String> {
        val email = emailInput.trim().lowercase()
        val newPassword = newPasswordInput.trim()
        val otp = otpInput.trim()

        if (newPassword.length < 4) {
            return Result.failure(IllegalArgumentException("Mật khẩu mới cần ít nhất 4 ký tự!"))
        }
        if (otp.length != 6) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập mã xác thực 6 số!"))
        }

        val webUrl = getWebAppUrl()
        if (webUrl.isNotBlank()) {
            try {
                val res = api.postAction(
                    webUrl,
                    SheetSyncPayload(action = "resetPassword", email = email, newPassword = newPassword, otp = otp)
                )
                if (res.status == "success") {
                    dao.insertAccount(UserAccountEntity(email, newPassword))
                    return Result.success("Đã đổi mật khẩu thành công! Mời bạn đăng nhập.")
                }
            } catch (_: Exception) {
            }
        }

        val expectedOtp = localOtpStore[email]
        if (expectedOtp != null && expectedOtp != otp) {
            return Result.failure(IllegalArgumentException("Mã xác thực 6 số không đúng!"))
        }

        dao.insertAccount(UserAccountEntity(email, newPassword))
        return Result.success("Đã đổi mật khẩu thành công! Mời bạn đăng nhập.")
    }

    // --- 2. QUẢN LÝ DANH SÁCH DATABASE (CỬA HÀNG) TRÊN GMAIL ---

    /**
     * Tải danh sách Database của Gmail từ Google Sheets về máy
     */
    suspend fun refreshDatabasesFromCloud(email: String) {
        val webUrl = getWebAppUrl()
        if (webUrl.isBlank() || email.isBlank()) return
        try {
            val listUrl = if (webUrl.contains("?")) "$webUrl&action=listDbs&email=$email" else "$webUrl?action=listDbs&email=$email"
            val res = api.pullAll(listUrl)
            if (res.status == "success" && !res.databases.isNullOrEmpty()) {
                dao.insertDatabases(res.databases)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Tạo Database mới khi người dùng bấm nút (+):
     * 1. Đặt tên cho Database
     * 2. Khởi tạo bộ dữ liệu mẫu Bia - Rượu - Nước ngọt
     * 3. Tự động tạo file Google Sheet trên Gmail của người dùng và đẩy dữ liệu lên
     */
    suspend fun createNewStoreDatabase(email: String, dbNameInput: String): Result<StoreDatabaseEntity> {
        val cleanName = dbNameInput.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập tên cửa hàng / dữ liệu!"))
        }

        val now = System.currentTimeMillis()
        val dbId = "DB_${now % 100000000}"
        var newDb = StoreDatabaseEntity(
            dbId = dbId,
            ownerEmail = email.lowercase(),
            dbName = cleanName,
            spreadsheetId = "",
            spreadsheetUrl = "https://docs.google.com/spreadsheets/u/0/?q=${ cleanName.replace(" ", "%20") }",
            createdAt = now
        )

        val webUrl = getWebAppUrl()
        if (webUrl.isNotBlank()) {
            try {
                val res = api.postAction(
                    webUrl,
                    SheetSyncPayload(
                        action = "createDb",
                        dbId = dbId,
                        email = email.lowercase(),
                        dbName = cleanName
                    )
                )
                if (res.status == "success" && res.database != null) {
                    newDb = res.database
                }
            } catch (_: Exception) {
            }
        }

        dao.insertDatabase(newDb)
        loadSampleBeverageData(dbId)

        // Nếu có kết nối Google Sheets, đẩy luôn dữ liệu khởi tạo lên file Sheet vừa tạo trên Gmail
        if (webUrl.isNotBlank()) {
            try {
                pushAllToGoogleSheets(dbId)
            } catch (_: Exception) {
            }
        }

        return Result.success(newDb)
    }

    /**
     * Xóa Cửa hàng (Database) khi người dùng nhấn giữ và nhập đúng mật khẩu đăng nhập:
     * 1. Kiểm tra mật khẩu của tài khoản Gmail đang đăng nhập.
     * 2. Nếu đúng: Xóa trên Google Sheets (chuyển file Sheet vào thùng rác) + Xóa toàn bộ dữ liệu cửa hàng trong SQLite.
     * 3. Nếu sai: Báo lỗi "Mật khẩu đăng nhập không đúng!" và không cho xóa.
     */
    suspend fun deleteStoreDatabaseWithPassword(
        emailInput: String,
        passwordInput: String,
        db: StoreDatabaseEntity
    ): Result<String> {
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Vui lòng nhập mật khẩu đăng nhập!"))
        }

        // Kiểm tra mật khẩu trong bảng user_accounts cục bộ
        val localAcc = dao.getAccountByEmail(email)
        if (localAcc != null && localAcc.password != password) {
            return Result.failure(IllegalArgumentException("Mật khẩu đăng nhập không đúng!"))
        }

        val webUrl = getWebAppUrl()
        if (webUrl.isNotBlank()) {
            try {
                val res = api.postAction(
                    webUrl,
                    SheetSyncPayload(
                        action = "deleteDb",
                        dbId = db.dbId,
                        email = email,
                        password = password
                    )
                )
                if (res.status == "error") {
                    return Result.failure(IllegalArgumentException(res.message ?: "Mật khẩu không đúng!"))
                }
            } catch (_: Exception) {
                // Nếu đang offline và đã khớp mật khẩu cục bộ thì vẫn cho phép xóa cục bộ
            }
        }

        dao.deleteProductsByDb(db.dbId)
        dao.deleteProductUnitsByDb(db.dbId)
        dao.deleteCustomersByDb(db.dbId)
        dao.deleteOrdersByDb(db.dbId)
        dao.deleteOrderItemsByDb(db.dbId)
        dao.deleteDebtTransactionsByDb(db.dbId)
        dao.deleteDatabaseById(db.dbId)

        if (getCurrentDbId() == db.dbId) {
            saveCurrentDb("", "")
        }

        return Result.success("Đã xóa cửa hàng \"${db.dbName}\"!")
    }

    /**
     * Chọn 1 Database trong danh sách để bắt đầu bán hàng (tự động gắn link Google Sheet của cửa hàng đó)
     */
    suspend fun selectDatabase(db: StoreDatabaseEntity) {
        val sheetUrl = db.spreadsheetUrl.ifBlank {
            "https://docs.google.com/spreadsheets/u/0/?q=${db.dbName.replace(" ", "%20")}"
        }
        saveCurrentDb(db.dbId, db.dbName, sheetUrl)
        if (dao.getProductCount(db.dbId) == 0) {
            val webUrl = getWebAppUrl()
            var pulled = false
            if (webUrl.isNotBlank()) {
                val pullRes = pullAllFromGoogleSheets(db.dbId)
                pulled = pullRes.isSuccess && dao.getProductCount(db.dbId) > 0
            }
            if (!pulled) {
                loadSampleBeverageData(db.dbId)
            }
        }
    }

    suspend fun loadSampleBeverageData(dbId: String) {
        if (dbId.isBlank()) return
        dao.insertProducts(SeedData.sampleProducts(dbId))
        dao.insertProductUnits(SeedData.sampleProductUnits(dbId))
        dao.insertCustomers(SeedData.sampleCustomers(dbId))
    }

    // --- 3. NGHIỆP VỤ BÁN HÀNG, KHO & CÔNG NỢ THEO DATABASE ĐANG CHỌN ---

    suspend fun saveProductWithUnits(
        dbId: String,
        product: ProductEntity,
        units: List<ProductUnitEntity>
    ) {
        dao.insertProduct(product.copy(dbId = dbId))
        dao.deleteUnitsByProduct(product.productId)
        dao.insertProductUnits(units.map { it.copy(dbId = dbId) })
    }

    suspend fun deleteProduct(productId: String) {
        dao.deleteUnitsByProduct(productId)
        dao.deleteProductById(productId)
    }

    suspend fun importStockByUnit(
        product: ProductEntity,
        unit: ProductUnitEntity,
        quantityInSelectedUnit: Int
    ) {
        val addedBaseQty = unit.conversionRate * quantityInSelectedUnit
        dao.updateProductStock(product.productId, addedBaseQty)
    }

    suspend fun createPosOrder(
        dbId: String,
        customer: CustomerEntity,
        cartItems: List<CartItem>,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String,
        note: String
    ): OrderWithItems {
        val now = System.currentTimeMillis()
        val orderId = "HD" + (now % 1000000).toString().padStart(6, '0')
        val totalAmount = cartItems.sumOf { it.lineTotal }
        val finalAmount = (totalAmount - discount).coerceAtLeast(0.0)
        val debtAmount = (finalAmount - paidAmount).coerceAtLeast(0.0)

        val order = OrderEntity(
            orderId = orderId,
            dbId = dbId,
            orderDate = now,
            customerId = customer.customerId,
            customerName = customer.name,
            totalAmount = totalAmount,
            discount = discount,
            finalAmount = finalAmount,
            paidAmount = paidAmount,
            debtAmount = debtAmount,
            paymentMethod = paymentMethod,
            note = note,
            isSynced = false
        )

        val items = cartItems.mapIndexed { index, cartItem ->
            OrderItemEntity(
                itemId = "${orderId}_${index + 1}",
                dbId = dbId,
                orderId = orderId,
                productId = cartItem.product.productId,
                productName = cartItem.product.name,
                unitId = cartItem.selectedUnit.unitId,
                unitName = cartItem.selectedUnit.unitName,
                conversionRate = cartItem.selectedUnit.conversionRate,
                quantity = cartItem.quantity,
                unitPrice = cartItem.selectedUnit.sellPrice,
                costPrice = cartItem.selectedUnit.costPrice,
                lineTotal = cartItem.lineTotal
            )
        }

        dao.insertOrder(order)
        dao.insertOrderItems(items)

        // Chỉ trừ tồn kho nếu người dùng bật "Quản lý nhập hàng" trong Cài đặt
        if (isManageStockEnabled()) {
            cartItems.forEach { cartItem ->
                val deductBase = -cartItem.totalBaseUnitsDeducted
                dao.updateProductStock(cartItem.product.productId, deductBase, now)
            }
        }

        if (debtAmount > 0 || note.isNotBlank()) {
            val latestCustomer = dao.getCustomerById(customer.customerId) ?: customer
            val updatedCrateNote = if (note.contains("vỏ", ignoreCase = true) || note.contains("két", ignoreCase = true)) {
                if (latestCustomer.emptyCrateNote.isBlank()) note else "${latestCustomer.emptyCrateNote} | $note"
            } else {
                latestCustomer.emptyCrateNote
            }
            dao.updateCustomer(
                latestCustomer.copy(
                    dbId = dbId,
                    totalDebt = latestCustomer.totalDebt + debtAmount,
                    emptyCrateNote = updatedCrateNote,
                    updatedAt = now
                )
            )
            if (debtAmount > 0) {
                dao.insertDebtTransaction(
                    DebtTransactionEntity(
                        transId = "GD_${UUID.randomUUID().toString().take(8)}",
                        dbId = dbId,
                        date = now,
                        customerId = customer.customerId,
                        customerName = customer.name,
                        amount = debtAmount,
                        type = "GHI_NO_DON_HANG",
                        note = "Nợ đơn $orderId"
                    )
                )
            }
        }

        return OrderWithItems(order, items)
    }

    suspend fun saveCustomer(dbId: String, customer: CustomerEntity) {
        dao.insertCustomer(customer.copy(dbId = dbId))
    }

    suspend fun collectCustomerDebt(
        dbId: String,
        customer: CustomerEntity,
        payAmount: Double,
        updatedCrateNote: String,
        note: String
    ) {
        val now = System.currentTimeMillis()
        val newDebt = (customer.totalDebt - payAmount).coerceAtLeast(0.0)
        dao.updateCustomer(
            customer.copy(
                dbId = dbId,
                totalDebt = newDebt,
                emptyCrateNote = updatedCrateNote,
                updatedAt = now
            )
        )
        if (payAmount > 0) {
            dao.insertDebtTransaction(
                DebtTransactionEntity(
                    transId = "THU_${UUID.randomUUID().toString().take(8)}",
                    dbId = dbId,
                    date = now,
                    customerId = customer.customerId,
                    customerName = customer.name,
                    amount = payAmount,
                    type = "THU_NO",
                    note = note.ifBlank { "Thu nợ" }
                )
            )
        }
    }

    /**
     * ĐỒNG BỘ LÊN GOOGLE SHEETS THEO DATABASE ĐANG CHỌN
     */
    suspend fun pushAllToGoogleSheets(dbId: String): Result<String> {
        val url = getWebAppUrl()
        if (url.isBlank()) {
            return Result.failure(IllegalStateException("Chưa gắn link Google Sheets!"))
        }
        return try {
            val payload = SheetSyncPayload(
                action = "pushAll",
                dbId = dbId,
                email = getLoggedInEmail(),
                products = dao.getAllProducts(dbId),
                productUnits = dao.getAllProductUnits(dbId),
                customers = dao.getAllCustomers(dbId),
                orders = dao.getAllOrders(dbId),
                orderItems = dao.getAllOrderItems(dbId),
                debtTransactions = dao.getAllDebtTransactions(dbId)
            )
            val response = api.postAction(url, payload)
            if (response.status == "success") {
                dao.markAllOrdersSynced(dbId)
                saveLastSyncTime(System.currentTimeMillis())
                Result.success(response.message ?: "Đã lưu lên Google Sheets!")
            } else {
                Result.failure(RuntimeException(response.message ?: "Lỗi Google Sheets"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * TẢI DỮ LIỆU TỪ GOOGLE SHEETS VỀ DATABASE ĐANG CHỌN
     */
    suspend fun pullAllFromGoogleSheets(dbId: String): Result<String> {
        val url = getWebAppUrl()
        if (url.isBlank()) {
            return Result.failure(IllegalStateException("Chưa gắn link Google Sheets!"))
        }
        return try {
            val pullUrl = if (url.contains("?")) "$url&action=pullAll&dbId=$dbId" else "$url?action=pullAll&dbId=$dbId"
            val response = api.pullAll(pullUrl)
            if (response.status == "success") {
                response.products?.takeIf { it.isNotEmpty() }?.let { list ->
                    dao.insertProducts(list.map { it.copy(dbId = dbId) })
                }
                response.productUnits?.takeIf { it.isNotEmpty() }?.let { list ->
                    dao.insertProductUnits(list.map { it.copy(dbId = dbId) })
                }
                response.customers?.takeIf { it.isNotEmpty() }?.let { list ->
                    dao.insertCustomers(list.map { it.copy(dbId = dbId) })
                }
                response.orders?.takeIf { it.isNotEmpty() }?.let { list ->
                    dao.insertOrders(list.map { it.copy(dbId = dbId) })
                }
                response.orderItems?.takeIf { it.isNotEmpty() }?.let { list ->
                    dao.insertOrderItems(list.map { it.copy(dbId = dbId) })
                }
                response.debtTransactions?.takeIf { it.isNotEmpty() }?.let { list ->
                    dao.insertDebtTransactions(list.map { it.copy(dbId = dbId) })
                }
                saveLastSyncTime(System.currentTimeMillis())
                Result.success("Đã tải dữ liệu từ Google Sheets!")
            } else {
                Result.failure(RuntimeException(response.message ?: "Không đọc được Google Sheets"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
