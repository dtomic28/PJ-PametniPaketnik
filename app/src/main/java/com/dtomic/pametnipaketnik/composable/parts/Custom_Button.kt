package com.dtomic.pametnipaketnik.composable.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dtomic.pametnipaketnik.ui.theme.AppTheme

@Composable
fun Custom_Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = RoundedCornerShape(8.dp),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(
        defaultElevation = 6.dp,
        pressedElevation = 2.dp,
        disabledElevation = 0.dp
    ),
    padding: Dp = 16.dp
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = shape,
        elevation = elevation
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = padding),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomButtonPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Custom_Button(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                text = "Primary Button",
                onClick = {}
            )

            Custom_Button(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                text = "Disabled Button",
                onClick = {},
                enabled = false
            )

            Custom_Button(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                text = "Secondary Button",
                onClick = {},
                backgroundColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )

            Custom_Button(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                text = "Tertiary Button",
                onClick = {},
                backgroundColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            )

        }
    }
}