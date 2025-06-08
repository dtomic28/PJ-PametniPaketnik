package com.dtomic.pametnipaketnik.composable.parts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dtomic.pametnipaketnik.R

@Composable
fun Custom_Logo(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.inversePrimary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.pp_logo_foreground),
            contentDescription = null,
            modifier = Modifier.size(size * 1.5f)
        )
    }
}