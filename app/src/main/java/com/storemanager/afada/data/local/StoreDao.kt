package com.storemanager.afada.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    // --- ACCOUNTS & DATABASES (MULTI-DATABASE BY GMAIL) ---
    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getAccountByEmail(email: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccountEntity)

    @Query("SELECT * FROM store_databases WHERE LOWER(ownerEmail) = LOWER(:email) ORDER BY createdAt DESC")
    fun observeDatabasesByEmail(email: String): Flow<List<StoreDatabaseEntity>>

    @Query("SELECT * FROM store_databases WHERE LOWER(ownerEmail) = LOWER(:email) ORDER BY createdAt DESC")
    suspend fun getDatabasesByEmail(email: String): List<StoreDatabaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDatabase(db: StoreDatabaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDatabases(dbs: List<StoreDatabaseEntity>)

    @Query("DELETE FROM store_databases WHERE dbId = :dbId")
    suspend fun deleteDatabaseById(dbId: String)

    @Query("DELETE FROM products WHERE dbId = :dbId")
    suspend fun deleteProductsByDb(dbId: String)

    @Query("DELETE FROM product_units WHERE dbId = :dbId")
    suspend fun deleteProductUnitsByDb(dbId: String)

    @Query("DELETE FROM customers WHERE dbId = :dbId")
    suspend fun deleteCustomersByDb(dbId: String)

    @Query("DELETE FROM orders WHERE dbId = :dbId")
    suspend fun deleteOrdersByDb(dbId: String)

    @Query("DELETE FROM order_items WHERE dbId = :dbId")
    suspend fun deleteOrderItemsByDb(dbId: String)

    @Query("DELETE FROM debt_transactions WHERE dbId = :dbId")
    suspend fun deleteDebtTransactionsByDb(dbId: String)

    // --- PRODUCTS & UNITS (THEO DATABASE ĐANG CHỌN) ---
    @Transaction
    @Query("SELECT * FROM products WHERE dbId = :dbId ORDER BY category ASC, name ASC")
    fun observeProductsWithUnits(dbId: String): Flow<List<ProductWithUnits>>

    @Query("SELECT * FROM products WHERE dbId = :dbId")
    suspend fun getAllProducts(dbId: String): List<ProductEntity>

    @Query("SELECT * FROM product_units WHERE dbId = :dbId")
    suspend fun getAllProductUnits(dbId: String): List<ProductUnitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductUnits(units: List<ProductUnitEntity>)

    @Query("DELETE FROM product_units WHERE productId = :productId")
    suspend fun deleteUnitsByProduct(productId: String)

    @Query("DELETE FROM products WHERE productId = :productId")
    suspend fun deleteProductById(productId: String)

    @Query("UPDATE products SET stockBaseQty = stockBaseQty + :deltaBaseQty, updatedAt = :timestamp WHERE productId = :productId")
    suspend fun updateProductStock(productId: String, deltaBaseQty: Int, timestamp: Long = System.currentTimeMillis())

    // --- CUSTOMERS ---
    @Query("SELECT * FROM customers WHERE dbId = :dbId ORDER BY totalDebt DESC, name ASC")
    fun observeCustomers(dbId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE dbId = :dbId")
    suspend fun getAllCustomers(dbId: String): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE customerId = :customerId LIMIT 1")
    suspend fun getCustomerById(customerId: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    // --- ORDERS ---
    @Transaction
    @Query("SELECT * FROM orders WHERE dbId = :dbId ORDER BY orderDate DESC")
    fun observeOrdersWithItems(dbId: String): Flow<List<OrderWithItems>>

    @Query("SELECT * FROM orders WHERE dbId = :dbId ORDER BY orderDate DESC")
    suspend fun getAllOrders(dbId: String): List<OrderEntity>

    @Query("SELECT * FROM order_items WHERE dbId = :dbId")
    suspend fun getAllOrderItems(dbId: String): List<OrderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("UPDATE orders SET isSynced = 1 WHERE dbId = :dbId")
    suspend fun markAllOrdersSynced(dbId: String)

    // --- DEBT TRANSACTIONS ---
    @Query("SELECT * FROM debt_transactions WHERE dbId = :dbId ORDER BY date DESC")
    fun observeDebtTransactions(dbId: String): Flow<List<DebtTransactionEntity>>

    @Query("SELECT * FROM debt_transactions WHERE dbId = :dbId ORDER BY date DESC")
    suspend fun getAllDebtTransactions(dbId: String): List<DebtTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtTransaction(trans: DebtTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtTransactions(list: List<DebtTransactionEntity>)

    @Query("SELECT COUNT(*) FROM products WHERE dbId = :dbId")
    suspend fun getProductCount(dbId: String): Int
}
