package com.dtomic.pametnipaketnik.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import com.dtomic.pametnipaketnik.utils.percentHeight
import com.dtomic.pametnipaketnik.utils.percentWidth

@Preview
@Composable
fun TitlePage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .percentWidth(0.9f)
                .percentHeight(0.6f),
            shadowElevation = 6.dp,

            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CustomLogo(
                    img = "placeholder",
                    size = 250.dp
                )
                CustomButton(
                    modifier = Modifier
                        .percentWidth(0.9f)
                        .height(60.dp),
                    text = "Login",
                    onClick = {},
                )

                CustomButton(
                    modifier = Modifier
                        .percentWidth(0.9f)
                        .height(60.dp),
                    text = "Register",
                    onClick = {},
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}