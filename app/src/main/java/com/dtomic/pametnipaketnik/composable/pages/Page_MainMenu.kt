package com.dtomic.pametnipaketnik.composable.pages

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.util.Log
import android.view.MenuItem
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.composable.parts.Custom_Button
import com.dtomic.pametnipaketnik.composable.parts.Custom_ErrorBox
import com.dtomic.pametnipaketnik.composable.parts.Custom_ItemCardRow
import com.dtomic.pametnipaketnik.ui.theme.AppTheme
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.dtomic.pametnipaketnik.composable.parts.Custom_UserDashboard
import androidx.compose.animation.core.tween
import androidx.compose.animation.*
import androidx.compose.foundation.layout.offset
import com.dtomic.pametnipaketnik.composable.parts.Custom_SettingsDashboard
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.setValue
import com.dtomic.pametnipaketnik.composable.parts.Custom_ItemCardBox
import com.dtomic.pametnipaketnik.utils.globalStorage

enum class MenuView {
    ITEMS,
    ACTIVE,
    HISTORY
}

class MainMenuViewModel : ViewModel() {
    var username = ""
    data class MenuItem(
        val id: String,
        val name: String,
        val description: String,
        val price: Int,
        val imageLink: String
    )
    init {
        viewModelScope.launch {
            try {
                loadItems()
            } catch (e: Exception) {
                Log.e("TILEN", "Failed to load items", e)
            }
        }
    }

    private val _currentView = MutableStateFlow(MenuView.ITEMS)
    val currentView: StateFlow<MenuView> = _currentView

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _moveToItemSell = MutableStateFlow(false)
    val moveToItemSell: StateFlow<Boolean> = _moveToItemSell

    private val _items = MutableStateFlow<List<MenuItem>>(emptyList())
    val items: StateFlow<List<MenuItem>> = _items

    fun moveToItemSell() {
        _moveToItemSell.value = true
    }

    fun toggleActiveView() {
        val targetView = if (_currentView.value == MenuView.ACTIVE) MenuView.ITEMS else MenuView.ACTIVE
        switchView(targetView)
    }

    private suspend fun loadItems(): Boolean = suspendCoroutine { cont ->
        HttpClientWrapper.get("item/getSellingItems") { success, responseBody ->
            if (success && responseBody != null) {
                try {
                    val jsonArray = JSONArray(responseBody)
                    val resultList = mutableListOf<MenuItem>()
                    for (i in 0 until jsonArray.length()) {
                        val jsonItem = jsonArray.getJSONObject(i)
                        val item = MenuItem(
                            id = jsonItem.getString("_id"),
                            name = jsonItem.getString("name"),
                            description = jsonItem.getString("description"),
                            price = jsonItem.getInt("price"),
                            imageLink = HttpClientWrapper.getBaseUrl()+jsonItem.getString("imageLink")
                        )
                        resultList.add(item)
                    }
                    _items.value = resultList
                    cont.resume(true)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            } else {
                cont.resumeWithException(Exception("HTTP error: $responseBody"))
            }
        }
    }
    private suspend fun loadHistoryItems(): Boolean = suspendCoroutine { cont ->
        HttpClientWrapper.get("transaction/historyByUsername/${username}") { success, responseBody ->
            if (success && responseBody != null) {
                try {
                    val jsonArray = JSONArray(responseBody)
                    val resultList = mutableListOf<MenuItem>()
                    for (i in 0 until jsonArray.length()) {
                        val jsonItem = jsonArray.getJSONObject(i)
                        val item = MenuItem(
                            id = jsonItem.getString("_id"),
                            name = jsonItem.getString("name"),
                            description = jsonItem.getString("description"),
                            price = jsonItem.getInt("price"),
                            imageLink = HttpClientWrapper.getBaseUrl()+jsonItem.getString("imageLink")
                        )
                        resultList.add(item)
                    }
                    _items.value = resultList
                    cont.resume(true)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            } else {
                cont.resumeWithException(Exception("HTTP error: $responseBody"))
            }
        }
    }
    private suspend fun loadActiveItems(): Boolean = suspendCoroutine { cont ->
        HttpClientWrapper.get("transaction/activeByUsername/${username}") { success, responseBody ->
            if (success && responseBody != null) {
                try {
                    val jsonArray = JSONArray(responseBody)
                    val resultList = mutableListOf<MenuItem>()
                    for (i in 0 until jsonArray.length()) {
                        val jsonItem = jsonArray.getJSONObject(i)
                        val item = MenuItem(
                            id = jsonItem.getString("_id"),
                            name = jsonItem.getString("name"),
                            description = jsonItem.getString("description"),
                            price = jsonItem.getInt("price"),
                            imageLink = HttpClientWrapper.getBaseUrl()+jsonItem.getString("imageLink")
                        )
                        resultList.add(item)
                    }
                    _items.value = resultList
                    cont.resume(true)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            } else {
                cont.resumeWithException(Exception("HTTP error: $responseBody"))
            }
        }
    }

    fun switchView(newView: MenuView) {
        viewModelScope.launch {
            try {
                when (newView) {
                    MenuView.ITEMS -> loadItems()
                    MenuView.ACTIVE -> loadActiveItems()
                    MenuView.HISTORY -> loadHistoryItems()
                }
                _currentView.value = newView
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load $newView"
                Log.e("MainMenuViewModel", "Failed to load $newView", e)
            }
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            try {
                loadItems()
            } catch (e: Exception) {
                Log.e("TILEN", "Failed to refresh items", e)
            }
        }
    }

    fun resetNavigation() {
        _moveToItemSell.value = false
    }

}

@Composable
fun Page_MainMenu(navController: NavController, viewModel: MainMenuViewModel = viewModel(), username: String) {
    viewModel.username = username
    val showProfileMenu = remember { mutableStateOf(false) }
    val showSettingsMenu = remember { mutableStateOf(false) }
    val currentView by viewModel.currentView.collectAsState()

    val errorMessage by viewModel.errorMessage.collectAsState()
    val error = errorMessage.isNotEmpty()

    val itemList by viewModel.items.collectAsState()
    val isGridView by globalStorage.isGridView.collectAsState()

    val navTrigger by viewModel.moveToItemSell.collectAsState()
    LaunchedEffect(navTrigger) {
        if (navTrigger) {
            navController.navigate("ItemSell")
            viewModel.resetNavigation()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentViewModel = rememberUpdatedState(viewModel)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentViewModel.value.refreshItems()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    Box( // whole screen
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        //contentAlignment = Alignment.Center
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row( //burger and profile
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxHeight()
                    .fillMaxWidth(0.9f)
                    .padding(10.dp)
            ) {
                Box( // Burger menu
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    IconButton(
                        onClick = { showSettingsMenu.value = true },
                        modifier = Modifier
                            .background(Color.Transparent)
                            .fillMaxHeight()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
                Box( //Profile
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        onClick = { showProfileMenu.value = true },
                        modifier = Modifier
                            .background(Color.Transparent)
                            .fillMaxHeight()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }
            Box( // error box
                modifier = Modifier
                    .weight(0.1f),
                contentAlignment = Alignment.Center
            ) {
                if (error) Custom_ErrorBox(errorMessage)
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(0.6f),
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(itemList) { item ->
                            Custom_ItemCardBox(
                                item = item,
                                onClick = {
                                    navController.navigate("Page_BuyItem/${item.id}")
                                }
                            )
                        }

                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(itemList) { item ->
                            Custom_ItemCardRow(
                                item = item,
                                onClick = {
                                    navController.navigate("Page_BuyItem/${item.id}")
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(0.2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Custom_Button(
                    modifier = Modifier
                        .height(60.dp)
                        .weight(0.45f),
                    text = if (currentView == MenuView.ACTIVE)
                        stringResource(R.string.btn_items)
                    else
                        stringResource(R.string.btn_activeTransactions),
                    onClick = { viewModel.toggleActiveView() },
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(
                    modifier = Modifier
                        .weight(0.1f)
                )
                Custom_Button( // Next
                    modifier = Modifier
                        .height(60.dp)
                        .weight(0.45f),
                    text = stringResource(R.string.btn_sell),
                    onClick = { viewModel.moveToItemSell() },
                )
            }
        }

        val profileDrawerWidth = 300.dp
        val isProfileOpen = showProfileMenu.value
        val profileOffsetX by animateDpAsState(
            targetValue = if (isProfileOpen) 0.dp else profileDrawerWidth,
            animationSpec = tween(durationMillis = 300)
        )

        if (showProfileMenu.value || profileOffsetX != profileDrawerWidth) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .blur(10.dp)
                    .clickable { showProfileMenu.value = false }
            )
            Box(
                Modifier
                    .fillMaxSize()
            ) {
                Box(
                    Modifier
                        .offset(x = profileOffsetX)
                        .width(profileDrawerWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                        .align(Alignment.CenterEnd)
                ) {
                    Custom_UserDashboard(onClose = { showProfileMenu.value = false }, navController)
                }
            }
        }



        val drawerWidth = 300.dp
        val isOpen = showSettingsMenu.value
        val offsetX by animateDpAsState(
            targetValue = if (isOpen) 0.dp else -drawerWidth,
            animationSpec = tween(durationMillis = 300)
        )

        if (showSettingsMenu.value || offsetX != -drawerWidth) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .blur(10.dp)
                    .clickable { showSettingsMenu.value = false }
            )
            Box(
                Modifier
                    .fillMaxSize()
            ) {
                Box(
                    Modifier
                        .offset(x = offsetX)
                        .width(drawerWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                        .align(Alignment.CenterStart)
                ) {
                    Custom_SettingsDashboard(onClose = { showSettingsMenu.value = false }, changeLayout = { globalStorage.toggleGridView() }, navController)
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val navController = rememberNavController()
    AppTheme {
        Page_MainMenu(navController, username = "")
    }
}