package com.storemanager.afada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.storemanager.afada.data.local.AppDatabase
import com.storemanager.afada.data.remote.GoogleSheetsApi
import com.storemanager.afada.data.repository.StoreRepository
import com.storemanager.afada.ui.StoreViewModel
import com.storemanager.afada.ui.navigation.AfadaAppNavigation
import com.storemanager.afada.ui.theme.AfadaStoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getInstance(applicationContext)
        val sheetsApi = GoogleSheetsApi.create()
        val repository = StoreRepository(database.storeDao(), sheetsApi, applicationContext)
        val viewModelFactory = StoreViewModel.Factory(repository)

        setContent {
            AfadaStoreTheme {
                val storeViewModel: StoreViewModel = viewModel(factory = viewModelFactory)
                AfadaAppNavigation(viewModel = storeViewModel)
            }
        }
    }
}
