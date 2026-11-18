package com.storemanager.afada.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Bảng lưu Tài khoản Gmail của người dùng
 */
@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val email: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Bảng lưu Danh sách Database (Cửa hàng) thuộc về từng Gmail
 */
@Entity(tableName = "store_databases")
data class StoreDatabaseEntity(
    @PrimaryKey val dbId: String,
    val ownerEmail: String,
    val dbName: String,
    val spreadsheetId: String = "",
    val spreadsheetUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val productId: String,
    val dbId: String = "DEFAULT_DB",
    val name: String,
    val category: String, // "Bia", "Rượu", "Nước ngọt", "Nước suối"
    val baseUnit: String, // "Lon", "Chai"
    val baseCostPrice: Double = 0.0,
    val baseSellPrice: Double = 0.0,
    val stockBaseQty: Int = 0, // Tổng tồn kho tính theo đơn vị nhỏ nhất (Lon/Chai)
    val minStockAlert: Int = 48,
    val imageUri: String = "", // Đường dẫn ảnh sản phẩm (URI từ thư viện máy hoặc mã màu/preset)
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "product_units")
data class ProductUnitEntity(
    @PrimaryKey val unitId: String,
    val dbId: String = "DEFAULT_DB",
    val productId: String,
    val unitName: String, // "Két", "Thùng", "Lốc", "Lon", "Chai"
    val conversionRate: Int, // 20, 24, 6, 1
    val sellPrice: Double,
    val costPrice: Double = 0.0,
    val barcode: String = "",
    val isDefault: Boolean = false
)

data class ProductWithUnits(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "productId"
    )
    val units: List<ProductUnitEntity>
) {
    /**
     * Chỉ lấy các đơn vị tính (Két / Thùng / Lốc / Lon) đã được người dùng nhập giá bán > 0
     */
    fun activePricedUnits(): List<ProductUnitEntity> {
        return units.filter { it.sellPrice > 0.0 }.sortedByDescending { it.conversionRate }
    }

    /**
     * Quy đổi tồn kho ngắn gọn (chỉ hiện khi bật "Quản lý nhập hàng" trong Cài đặt)
     */
    fun formattedStock(): String {
        val total = product.stockBaseQty
        val largestUnit = units.filter { it.conversionRate > 1 && it.sellPrice > 0 }
            .maxByOrNull { it.conversionRate }
            ?: units.filter { it.conversionRate > 1 }.maxByOrNull { it.conversionRate }
        return if (largestUnit != null && largestUnit.conversionRate > 1) {
            val rate = largestUnit.conversionRate
            val majorCount = total / rate
            val remain = total % rate
            val shortMajorName = largestUnit.unitName.substringBefore("(").trim()
            when {
                majorCount > 0 && remain > 0 -> "$majorCount $shortMajorName + $remain ${product.baseUnit}"
                majorCount > 0 && remain == 0 -> "$majorCount $shortMajorName"
                else -> "$remain ${product.baseUnit}"
            }
        } else {
            "$total ${product.baseUnit}"
        }
    }

    fun defaultUnit(): ProductUnitEntity {
        val priced = activePricedUnits()
        return priced.firstOrNull { it.isDefault }
            ?: priced.firstOrNull()
            ?: units.firstOrNull()
            ?: ProductUnitEntity(
                unitId = "${product.productId}_THUNG",
                dbId = product.dbId,
                productId = product.productId,
                unitName = "Thùng",
                conversionRate = 24,
                sellPrice = product.baseSellPrice,
                isDefault = true
            )
    }
}

/**
 * Mỗi đơn vị tính có giá bán > 0 sẽ được tách thành 1 dòng mặt hàng độc lập ở màn hình Bán hàng (POS).
 * Ví dụ: "Bia Hà Nội lon" có giá Thùng = 250.000, giá Lốc = 65.000
 * -> Tạo ra 2 dòng:
 *    1) Bia Hà Nội lon (Đơn vị: Thùng - 250.000đ)
 *    2) Bia Hà Nội lon (Đơn vị: Lốc - 65.000đ)
 */
data class PosSellableUnitItem(
    val product: ProductEntity,
    val unit: ProductUnitEntity,
    val formattedStockText: String
) {
    val uniqueKey: String get() = "${product.productId}_${unit.unitId}"
}

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val customerId: String,
    val dbId: String = "DEFAULT_DB",
    val name: String,
    val phone: String,
    val address: String,
    val totalDebt: Double = 0.0,
    val emptyCrateNote: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val dbId: String = "DEFAULT_DB",
    val orderDate: Long = System.currentTimeMillis(),
    val customerId: String,
    val customerName: String,
    val totalAmount: Double,
    val discount: Double = 0.0,
    val finalAmount: Double,
    val paidAmount: Double,
    val debtAmount: Double,
    val paymentMethod: String = "Tiền mặt",
    val note: String = "",
    val isSynced: Boolean = false
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val itemId: String,
    val dbId: String = "DEFAULT_DB",
    val orderId: String,
    val productId: String,
    val productName: String,
    val unitId: String,
    val unitName: String,
    val conversionRate: Int,
    val quantity: Int,
    val unitPrice: Double,
    val costPrice: Double = 0.0,
    val lineTotal: Double
)

data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "orderId",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)

@Entity(tableName = "debt_transactions")
data class DebtTransactionEntity(
    @PrimaryKey val transId: String,
    val dbId: String = "DEFAULT_DB",
    val date: Long = System.currentTimeMillis(),
    val customerId: String,
    val customerName: String,
    val amount: Double,
    val type: String, // "THU_NO", "GHI_NO_DON_HANG"
    val note: String = ""
)
