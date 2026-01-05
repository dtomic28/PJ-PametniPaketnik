package com.dtomic.pametnipaketnik.composable.pages

import android.R.attr.password
import android.util.Log
import android.util.Log.e
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.composable.parts.Custom_Button
import com.dtomic.pametnipaketnik.composable.parts.Custom_ErrorBox
import com.dtomic.pametnipaketnik.composable.parts.Custom_Logo
import com.dtomic.pametnipaketnik.composable.parts.Custom_TextField
import com.dtomic.pametnipaketnik.ui.theme.AppTheme
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// MODEL
class LoginViewModel : ViewModel() {

    val username = mutableStateOf("")
    val password = mutableStateOf("")

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _moveTo2FA = MutableStateFlow(false)
    val moveTo2FA: StateFlow<Boolean> = _moveTo2FA

    private suspend fun sendLogin(username: String, password: String) : Boolean = suspendCoroutine { cont ->
        val jsonBody = JSONObject().apply {
            put("username", username)
            put("password", password)
        }.toString()

        HttpClientWrapper.postJson("user/login", jsonBody) { success, responseBody ->
            if (success && responseBody != null) {
                try {
                    val json = JSONObject(responseBody)
                    if (json.has("token")) {
                        val token = json.getString("token")
                        HttpClientWrapper.setBearerToken(token)
                        Log.i("Page_Login", "Login success, token set")
                        cont.resume(true)
                    } else {
                        Log.e("Page_Login", "Token missing in response: $responseBody")
                        cont.resumeWithException(Exception("Token missing in response"))
                    }
                } catch (e: Exception) {
                    Log.e("Page_Login", "JSON parsing error: ${e.message}")
                    cont.resumeWithException(e)
                }
            } else {
                Log.e("Page_Login", "Login fail $responseBody")
                cont.resumeWithException(Exception("HTTP error: $responseBody"))
            }
        }

    }

    fun loginUser() {
        viewModelScope.launch {
            try {
                if (sendLogin(username.value, password.value)) {
                    _moveTo2FA.value = true
                }
            }
            catch (e: Exception) {
                _errorMessage.value = "Error while trying to log in."
            }
        }
    }
    fun resetNavigation() {
        _moveTo2FA.value = false
    }
}

//VIEW
@Composable
fun Page_Login(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    val errorMessage by viewModel.errorMessage.collectAsState()
    val error = errorMessage.isNotEmpty()

    val navTrigger by viewModel.moveTo2FA.collectAsState()

    LaunchedEffect(navTrigger) {
        if (navTrigger) {
            navController.navigate("MainMenuPage/${viewModel.username.value}") {
                popUpTo("TitlePage") { inclusive = false }
            }
            // Optionally reset the trigger to prevent repeated navigation
            viewModel.resetNavigation()
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
            Surface(
                // island
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(0.6f),
                shadowElevation = 6.dp,

                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column( // logo/buttons devision
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box( // logo box
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Custom_Logo(
                            size = 100.dp
                        )
                    }

                    Column( // buttons column
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Custom_TextField(
                            value = viewModel.username.value,
                            onValueChange = { viewModel.username.value = it },
                            placeholderText = stringResource(R.string.txt_username),
                            keyboardType = KeyboardType.Text
                        )
                        Spacer(
                            modifier = Modifier
                                .height(10.dp)
                        )
                        Custom_TextField(
                            password = true,
                            value = viewModel.password.value,
                            onValueChange = { viewModel.password.value = it },
                            placeholderText = stringResource(R.string.txt_password),
                            keyboardType = KeyboardType.Password
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
                Custom_Button( // Next
                    modifier = Modifier
                        .height(60.dp)
                        .weight(0.45f),
                    text = stringResource(R.string.btn_next),
                    onClick = { viewModel.loginUser() },
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
        Page_Login(navController)
    }
}