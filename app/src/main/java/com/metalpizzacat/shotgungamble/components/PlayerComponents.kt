package com.metalpizzacat.shotgungamble.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.metalpizzacat.shotgungamble.game.Item

@Composable
fun HealthDisplay(modifier: Modifier = Modifier, health: Int, maxHealth: Int) {
    Row(modifier) {
        for (i in 0..<health) {
            Icon(Icons.Default.Favorite, contentDescription = "Heart")
        }
        for (i in 0..<(maxHealth - health)) {
            Icon(Icons.Default.FavoriteBorder, contentDescription = "No heart")
        }
    }
}

@Composable
fun PlayerBoard(
    modifier: Modifier = Modifier,
    handcuffed: Boolean,
    health: Int,
    maxHealth: Int,
    items: List<Item>,
    onItemSelected: (item: Item) -> Unit,
) {
    Column(modifier = modifier) {
        HealthDisplay(health = health, maxHealth = maxHealth)
        if (handcuffed) {
            Text(text = "Handcuffed")
        }
        items.chunked(4).forEach {
            Row {
                for (item in it) {
                    Icon(
                        painterResource(id = item.resource),
                        contentDescription = stringResource(id = item.descriptionRes),
                        tint = Color.Unspecified,
                        modifier = Modifier.clickable { onItemSelected(item) }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun BoardPreview() {
    PlayerBoard(
        health = 3,
        maxHealth = 5,
        handcuffed = true,
        items = listOf(Item.BEER, Item.BEER, Item.PHONE, Item.HANDCUFFS, Item.HANDSAW)
    ) {}
}