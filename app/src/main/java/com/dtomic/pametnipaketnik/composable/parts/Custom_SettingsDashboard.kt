package com.dtomic.pametnipaketnik.composable.parts

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.utils.globalStorage

@Composable
fun Custom_SettingsDashboard(onClose: () -> Unit, changeLayout: () -> Unit, navController: NavController) {
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
            Custom_Button(
                text = stringResource(R.string.btn_toggleLayout),
                onClick = { changeLayout() },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )
            Spacer(Modifier.height(8.dp))
            Custom_Button(
                text = stringResource(R.string.btn_toggleDarkMode),
                onClick = { globalStorage.toggleTheme() },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )
            Spacer(Modifier.height(8.dp))
            Custom_Button(
                text = stringResource(R.string.btn_toggleTransactionHistory),
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )
            Spacer(Modifier.height(8.dp))
            Custom_Button(
                text = stringResource(R.string.btn_moveToMapPage),
                onClick = { navController.navigate("MapPage") },
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
        Custom_SettingsDashboard(onClose = {}, changeLayout = {}, navController)
    }
}