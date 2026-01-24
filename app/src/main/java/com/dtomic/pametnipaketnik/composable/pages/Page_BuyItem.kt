package com.dtomic.pametnipaketnik.composable.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.dotlottie.dlplayer.Mode
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.composable.parts.Custom_Button
import com.dtomic.pametnipaketnik.composable.parts.Custom_ErrorBox
import com.dtomic.pametnipaketnik.composable.parts.Custom_Text
import com.dtomic.pametnipaketnik.ui.theme.AppTheme
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper
import com.dtomic.pametnipaketnik.utils.playToken
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieEventListener
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
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
    val pathToImage = mutableStateOf("")

    private val _moveToMainMenu = MutableStateFlow(false)
    val moveToMainMenu: StateFlow<Boolean> = _moveToMainMenu

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _playToken = MutableStateFlow(false)
    val playToken: StateFlow<Boolean> = _playToken

    // NEW: controls animation overlay
    private val _showBuyAnimation = MutableStateFlow(false)
    val showBuyAnimation: StateFlow<Boolean> = _showBuyAnimation

    private suspend fun getItemByID(itemID: String): Boolean = suspendCoroutine { cont ->
        HttpClientWrapper.get("item/getItemByID/${itemID}") { success, responseBody ->
            if (success && responseBody != null) {
                try {
                    val item = JSONObject(responseBody)
                    itemName.value = item.getString("itemName")
                    itemDescription.value = item.getString("itemDescription")
                    itemPrice.value = item.getString("itemPrice") + "€"
                    boxID.value = item.getString("boxID")
                    pathToImage.value = HttpClientWrapper.getBaseUrl() + item.getString("pathToImage")
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
        _showBuyAnimation.value = true
    }

    fun confirmBuy() {
        viewModelScope.launch {
            val jsonBody = JSONObject().apply {
                put("itemID", itemId)
            }.toString()

            Log.d("TILEN", "json: $jsonBody")

            HttpClientWrapper.postJson("item/buyItem", jsonBody) { success, responseBody ->
                if (success && responseBody != null) {
                    _playToken.value = true
                    _moveToMainMenu.value = true
                } else {
                    _errorMessage.value = responseBody.toString()
                }

                _showBuyAnimation.value = false
            }
        }
    }

    fun resetNavigation() {
        _moveToMainMenu.value = false
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

    val navTrigger by viewModel.moveToMainMenu.collectAsState()
    LaunchedEffect(navTrigger) {
        if (navTrigger) {
            navController.popBackStack()
            viewModel.resetNavigation()
        }
    }

    val showBuyAnimation by viewModel.showBuyAnimation.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.weight(0.2f),
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.3f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Log.d("TILEN", viewModel.pathToImage.value)
                            AsyncImage(
                                model = viewModel.pathToImage.value,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        }

                        Column(
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
                        text = stringResource(R.string.btn_back),
                        onClick = { navController.popBackStack() },
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )

                    Spacer(modifier = Modifier.weight(0.1f))

                    Custom_Button(
                        modifier = Modifier
                            .height(60.dp)
                            .weight(0.45f),
                        text = stringResource(R.string.btn_buy),
                        onClick = { viewModel.buyItem() }
                    )
                }
            }
        }


        if (showBuyAnimation) {

            val eventListener: DotLottieEventListener = remember {
                object : DotLottieEventListener {
                    override fun onPlay() {}
                    override fun onPause() {}
                    override fun onStop() {}
                    override fun onFrame(frame: Float) {}
                    override fun onDestroy() {}
                    override fun onFreeze() {}
                    override fun onLoad() {}
                    override fun onLoop(loopCount: Int) {}
                    override fun onUnFreeze() {}

                    override fun onComplete() {
                        Log.i("TILEN", "Animation complete")
                        viewModel.confirmBuy()
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                DotLottieAnimation(
                    source = DotLottieSource.Url(
                        "https://lottie.host/efc84eee-77d7-4e39-b327-b42f0228d0a1/h3K8uq3dvo.lottie"
                    ),
                    autoplay = true,
                    loop = false,
                    speed = 3f,
                    useFrameInterpolation = false,
                    playMode = Mode.FORWARD,
                    modifier = Modifier.size(1000.dp),
                    eventListeners = listOf(eventListener)
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