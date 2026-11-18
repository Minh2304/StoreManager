package com.storemanager.afada.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.auth.AuthScreen
import com.storemanager.afada.ui.auth.DatabaseListScreen
import com.storemanager.afada.ui.customer.CustomerScreen
import com.storemanager.afada.ui.dashboard.DashboardScreen
import com.storemanager.afada.ui.inventory.InventoryScreen
import com.storemanager.afada.ui.pos.PosScreen
import com.storemanager.afada.ui.report.OrderHistoryScreen
import com.storemanager.afada.ui.report.ReportAndSheetScreen

/**
 * Module điều hướng chính (Đã bỏ hoàn toàn thanh điều hướng dưới cùng Bottom Navigation Bar):
 * - Chỉ để lại màn hình Trang chủ làm trung tâm.
 * - Nút Cài đặt nằm ở góc trên bên phải của Trang chủ.
 * - Nhấn vào tên Cửa hàng ở góc trên bên trái để mở tùy chọn Quản lý Cửa hàng.
 */
@Composable
fun AfadaAppNavigation(viewModel: StoreViewModel) {
    val authState by viewModel.authState.collectAsState()

    when {
        authState.loggedInEmail.isBlank() -> {
            AuthScreen(
                viewModel = viewModel,
                onLoginSuccess = {}
            )
        }
        authState.currentDbId.isBlank() -> {
            DatabaseListScreen(
                viewModel = viewModel,
                onDatabaseSelected = {},
                onLogout = {}
            )
        }
        else -> {
            AfadaMainStoreHost(viewModel = viewModel)
        }
    }
}

@Composable
fun AfadaMainStoreHost(viewModel: StoreViewModel) {
    val navController = rememberNavController()

    val navigateToRoute: (String) -> Unit = { route ->
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigate = navigateToRoute,
                onSwitchDatabase = { viewModel.switchDatabaseScreen() }
            )
        }
        composable("pos") {
            PosScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("inventory") {
            InventoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("customer") {
            CustomerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("report") {
            ReportAndSheetScreen(
                viewModel = viewModel,
                initialTab = 0,
                onBack = { navController.popBackStack() }
            )
        }
        composable("database_settings") {
            ReportAndSheetScreen(
                viewModel = viewModel,
                initialTab = 2,
                onBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            OrderHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
