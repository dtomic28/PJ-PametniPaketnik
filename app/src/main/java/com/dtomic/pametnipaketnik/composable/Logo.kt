package com.dtomic.pametnipaketnik.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dtomic.pametnipaketnik.utils.percentHeight
import com.dtomic.pametnipaketnik.utils.percentWidth

@Composable
fun CustomLogo(
    img: String,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.error, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = img,
            modifier = Modifier.wrapContentSize(),
            textAlign = TextAlign.Center
        )
    }
}