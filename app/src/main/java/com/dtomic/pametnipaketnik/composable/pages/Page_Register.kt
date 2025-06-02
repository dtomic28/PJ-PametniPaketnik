package com.dtomic.pametnipaketnik.composable.pages

import android.R.attr.password
import android.util.Base64
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
import com.dtomic.pametnipaketnik.utils.ApiResponse
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper
import com.dtomic.pametnipaketnik.utils.extractZip
import com.dtomic.pametnipaketnik.utils.playAudio
import com.dtomic.pametnipaketnik.utils.registerUser
import com.dtomic.pametnipaketnik.utils.saveBase64ToFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// MODEL
class RegisterViewModel : ViewModel() {

    val username = mutableStateOf("")
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val repeatPassword = mutableStateOf("")

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val http = HttpClientWrapper()

    private suspend fun getUsernames(): List<String> = suspendCoroutine { cont ->
        http.get("user/getAllUsernames") { success, responseBody ->
            if (success && responseBody != null) {
                try {
                    val usernames = JSONArray(responseBody)
                    val result = mutableListOf<String>()
                    for (i in 0 until usernames.length()) {
                        result.add(usernames.getString(i))
                    }
                    cont.resume(result)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            } else {
                cont.resumeWithException(Exception("HTTP error: $responseBody"))
            }
        }
    }

    fun registerUser() {
        viewModelScope.launch {
            val emailRegex = Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")

            val allUsernames = mutableListOf<String>()
            try {
                val allUsernames = getUsernames()
                
                Log.d("TILEN", allUsernames.contains(username.value).toString())
                if (password.value != repeatPassword.value) {
                    _errorMessage.value = "Passwords don't match!"
                } else if (allUsernames.contains(username.value)) {
                    _errorMessage.value = "Username has already been taken!"
                } else if (!emailRegex.containsMatchIn(email.value)) {
                    _errorMessage.value = "Not a valid email!"
                } else {
                    _errorMessage.value = ""
                }
            }
            catch (e: Exception) {
                Log.d("TILEN", e.message ?: "error when getting usernames and verifying user")
            }
        }

    }
}

// VIEW
@Composable
fun Page_Register(navController: NavController, viewModel: RegisterViewModel = viewModel()) {
    val errorMessage by viewModel.errorMessage.collectAsState()
    val error = errorMessage.isNotEmpty()

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
                                .weight(0.333f)
                        )
                        Custom_TextField(
                            value = viewModel.email.value,
                            onValueChange = { viewModel.email.value = it },
                            placeholderText = stringResource(R.string.txt_email),
                            keyboardType = KeyboardType.Email
                        )
                        Spacer(
                            modifier = Modifier
                                .weight(0.333f)
                        )
                        Custom_TextField(
                            value = viewModel.password.value,
                            onValueChange = { viewModel.password.value = it },
                            placeholderText = stringResource(R.string.txt_password),
                            keyboardType = KeyboardType.Password
                        )
                        Spacer(
                            modifier = Modifier
                                .weight(0.333f)
                        )
                        Custom_TextField(
                            value = viewModel.repeatPassword.value,
                            onValueChange = { viewModel.repeatPassword.value = it },
                            placeholderText = stringResource(R.string.txt_repeatPassword),
                            keyboardType = KeyboardType.Password,
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
                    onClick = { viewModel.registerUser() }
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
        Page_Register(navController)
    }
}