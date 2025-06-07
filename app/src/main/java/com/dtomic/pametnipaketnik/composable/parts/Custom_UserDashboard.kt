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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Custom_UserDashboard(onClose: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.8f),
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Profile")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { /* Settings */ }) {
                Text("Settings")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onClose }) {
                Text("Close")
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Custom_UserDashboard(onClose = {  })
    }
}