package com.dtomic.pametnipaketnik.composable.parts

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.dtomic.pametnipaketnik.composable.pages.MainMenuViewModel
import com.dtomic.pametnipaketnik.R
import com.dtomic.pametnipaketnik.utils.HttpClientWrapper

class ItemCardViewModel: ViewModel() {
}

@Composable
fun Custom_ItemCardRow(item: MainMenuViewModel.MenuItem, onClick: () -> Unit, viewModel: ItemCardViewModel = ItemCardViewModel()) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                /*

                   TODO kjrkol:
                        fix ta image tuki, v item.image link je shranjen ceu link do slike na backend
                        (v mojm primeri localhost:3001/images/default.jpg) in ta link dela prek
                        browserja tud n seperate device, v tem imageLink pa ne dela - namest tega
                        defaulta na error state (R.drawable.ic_launcher_background)

                 */
                model = item.imageLink,
                contentDescription = "",
                contentScale = ContentScale.FillBounds,
                error = painterResource(R.drawable.ic_launcher_background),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(64.dp)
            )

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview
@Composable
private fun PreviewItemCardRow() {
    val mockItem = MainMenuViewModel.MenuItem(
        name = "Sample Item",
        imageLink = "http://192.168.1.213:3001/images/default.jpg",
        id = "id",
        description = "description",
        price = 5
    )

    Box(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
    ) {
        Custom_ItemCardRow(
            onClick = {},
            item = mockItem
        )
    }

}