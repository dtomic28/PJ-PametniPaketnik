package com.dtomic.pametnipaketnik.composable.parts

import android.R.attr.singleLine
import android.R.attr.text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.ui.theme.AppTheme

@Composable
fun Custom_Text(
    textLeft: String,
    textRight: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)) // ensures inner clipping
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.3f)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = textLeft, // or any static/label text
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.7f)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = textRight,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .padding(start = 16.dp)
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun CustomButtonPreview() {
    AppTheme {
        Custom_Text(
            textLeft = "Label",
            textRight = "Description",
            modifier = Modifier
                .height(64.dp)
        )
    }
}