package com.dtomic.pametnipaketnik.composable.parts

import android.R.attr.onClick
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserDashboardViewModel : ViewModel() {
    private val _logout = MutableStateFlow(false)
    val logout: StateFlow<Boolean> = _logout


    fun logout() {
        HttpClientWrapper.clearBearerToken()
        _logout.value = true
    }

    fun resetNavigation() {
        _logout.value = false
    }
}


@Composable
fun Custom_UserDashboard(onClose: () -> Unit, navController: NavController, viewModel : UserDashboardViewModel = viewModel()) {
    val navTrigger by viewModel.logout.collectAsState()
    LaunchedEffect(navTrigger) {
        if (navTrigger) {
            navController.navigate("TitlePage") {
                popUpTo(0) { inclusive = true }
            }
            viewModel.resetNavigation()
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(1f),
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box( // logo box
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Custom_Logo(
                    size = 100.dp
                )
            }
            Spacer(Modifier.height(8.dp))
            Custom_Button(
                text = stringResource(R.string.btn_changePfp),
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )
            Spacer(Modifier.height(8.dp))
            Custom_Button(
                text = stringResource(R.string.btn_changeUsername),
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )
            Spacer(Modifier.height(8.dp))
            Custom_Button(
                text = stringResource(R.string.btn_changePassword),
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )
            Spacer(Modifier.height(8.dp))
            Custom_Button(
                text = stringResource(R.string.btn_changeEmail),
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )
            Spacer(Modifier.weight(1f))
            Custom_Button(
                text = stringResource(R.string.btn_back),
                onClick = { onClose() },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )
            Spacer(Modifier.height(8.dp))
            Custom_Button(
                text = stringResource(R.string.btn_logout),
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val navController = rememberNavController()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Custom_UserDashboard(onClose = {  }, navController = navController)
    }
}