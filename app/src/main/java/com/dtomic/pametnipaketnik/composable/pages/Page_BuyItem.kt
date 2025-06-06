package com.dtomic.pametnipaketnik.composable.pages

import android.R.attr.password
import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.composable.parts.Custom_Button
import com.dtomic.pametnipaketnik.composable.parts.Custom_ErrorBox
import com.dtomic.pametnipaketnik.composable.parts.Custom_Logo
import com.dtomic.pametnipaketnik.composable.parts.Custom_Text
import com.dtomic.pametnipaketnik.composable.parts.Custom_TextField
import com.dtomic.pametnipaketnik.ui.theme.AppTheme
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper
import com.dtomic.pametnipaketnik.utils.playToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.file.Files.exists
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PageViewModelFactory(private val itemId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PageViewModel(itemId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PageViewModel(private val itemId: String) : ViewModel() {

    val itemName = mutableStateOf("Error!")
    val itemDescription = mutableStateOf("Error!")
    val itemPrice = mutableStateOf("Error!")
    val boxID = mutableStateOf("Error!")

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _playToken = MutableStateFlow(false)
    val playToken: StateFlow<Boolean> = _playToken

    private suspend fun getItemByID(itemID: String): Boolean = suspendCoroutine { cont ->
        HttpClientWrapper.get("item/getItemByID/${itemID}") { success, responseBody ->
            if (success && responseBody != null) {
                try {
                    val item = JSONObject(responseBody)
                    itemName.value = item.getString("itemName")
                    itemDescription.value = item.getString("itemDescription")
                    itemPrice.value = item.getString("itemPrice") + "€"
                    boxID.value = item.getString("boxID")
                    cont.resume(true)
                } catch (e: Exception) {
                    _errorMessage.value = e.message.toString()
                    cont.resumeWithException(e)
                }
            } else {
                _errorMessage.value = "Item not found!"
                cont.resumeWithException(Exception("HTTP error: $responseBody"))
            }
        }
    }

    fun buyItem() {
        viewModelScope.launch {
            val jsonBody = JSONObject().apply {
                put("itemID", itemId)
            }.toString()

            HttpClientWrapper.postJson("item/buyItem/${itemId}", jsonBody) { success, responseBody ->
                if (success && responseBody != null) {
                    _playToken.value = true
                }
                else {
                    _errorMessage.value = responseBody.toString()
                }
            }
        }

    }

    init {
        viewModelScope.launch {
            try {
                getItemByID(itemId)
            } catch (e: Exception) {
                Log.e("TILEN", "Failed to load items", e)
            }
        }
    }
}

@Composable
fun Page_BuyItem(navController: NavController, itemId: String) {
    val viewModel: PageViewModel = viewModel(factory = PageViewModelFactory(itemId))
    val errorMessage by viewModel.errorMessage.collectAsState()
    val error = errorMessage.isNotEmpty()
    val context = LocalContext.current
    val playTokenFlag by viewModel.playToken.collectAsState()

    LaunchedEffect(playTokenFlag) {
        if (playTokenFlag && viewModel.boxID.value != "Error!") {
            playToken(viewModel.boxID.value, context)
        }
    }

    Box( // whole screen
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box(
                modifier = Modifier
                    .weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                if (error) Custom_ErrorBox(errorMessage)
            }
            Surface( // Center Island
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(0.6f),
                shadowElevation = 6.dp,

                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column( // logo/input devision
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box( // logo box
                        modifier = Modifier
                            .weight(0.3f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Custom_Logo(
                            size = 100.dp
                        )
                    }

                    Column( // buttons column
                        modifier = Modifier
                            .weight(0.7f)
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Custom_Text(
                            textLeft = stringResource(R.string.txt_itemName),
                            textRight = viewModel.itemName.value
                        )
                        Custom_Text(
                            textLeft = stringResource(R.string.txt_itemDescription),
                            textRight = viewModel.itemDescription.value
                        )
                        Custom_Text(
                            textLeft = stringResource(R.string.txt_itemPrice),
                            textRight = viewModel.itemPrice.value
                        )
                        Custom_Text(
                            textLeft = stringResource(R.string.txt_boxID),
                            textRight = viewModel.boxID.value
                        )
                    }
                }
            }
            Row( // Buttons
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(0.2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Custom_Button( // back
                    modifier = Modifier
                        .height(60.dp)
                        .weight(0.45f),
                    text = stringResource(R.string.btn_back),
                    onClick = {navController.popBackStack()},
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(
                    modifier = Modifier
                        .weight(0.1f)
                )
                Custom_Button(
                    modifier = Modifier.height(60.dp).weight(0.45f),
                    text = stringResource(R.string.btn_next),
                    onClick = { viewModel.buyItem() }
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val navController = rememberNavController()
    AppTheme {
        Page_BuyItem(navController, itemId = "a")
    }
}