package com.storemanager.afada.data.remote

import com.storemanager.afada.data.local.CustomerEntity
import com.storemanager.afada.data.local.DebtTransactionEntity
import com.storemanager.afada.data.local.OrderEntity
import com.storemanager.afada.data.local.OrderItemEntity
import com.storemanager.afada.data.local.ProductEntity
import com.storemanager.afada.data.local.ProductUnitEntity
import com.storemanager.afada.data.local.StoreDatabaseEntity
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

data class SheetSyncPayload(
    val action: String = "pushAll",
    val dbId: String = "",
    val email: String = "",
    val password: String = "",
    val newPassword: String = "",
    val otp: String = "",
    val dbName: String = "",
    val products: List<ProductEntity>? = null,
    val productUnits: List<ProductUnitEntity>? = null,
    val customers: List<CustomerEntity>? = null,
    val orders: List<OrderEntity>? = null,
    val orderItems: List<OrderItemEntity>? = null,
    val debtTransactions: List<DebtTransactionEntity>? = null
)

data class SheetPullResponse(
    val status: String = "",
    val message: String? = null,
    val spreadsheetName: String? = null,
    val databases: List<StoreDatabaseEntity>? = null,
    val products: List<ProductEntity>? = null,
    val productUnits: List<ProductUnitEntity>? = null,
    val customers: List<CustomerEntity>? = null,
    val orders: List<OrderEntity>? = null,
    val orderItems: List<OrderItemEntity>? = null,
    val debtTransactions: List<DebtTransactionEntity>? = null
)

data class SheetPostResponse(
    val status: String = "",
    val message: String? = null,
    val debugOtp: String? = null,
    val syncedAt: Long? = null,
    val database: StoreDatabaseEntity? = null,
    val databases: List<StoreDatabaseEntity>? = null
)

data class RemoteAppVersionDto(
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val releaseDate: String = "18/11/2026",
    val apkUrl: String = "https://raw.githubusercontent.com/Minh2304/StoreManager/release/AFaDa-StoreManager.apk",
    val releasePageUrl: String = "https://github.com/Minh2304/StoreManager/tree/release",
    val releaseNotes: String = ""
)

interface GoogleSheetsApi {
    @GET
    suspend fun pullAll(@Url webAppUrl: String): SheetPullResponse

    @GET
    suspend fun checkGitReleaseVersion(@Url versionJsonUrl: String): RemoteAppVersionDto

    @POST
    suspend fun postAction(
        @Url webAppUrl: String,
        @Body payload: SheetSyncPayload
    ): SheetPostResponse

    companion object {
        fun create(): GoogleSheetsApi {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://script.google.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GoogleSheetsApi::class.java)
        }
    }
}
