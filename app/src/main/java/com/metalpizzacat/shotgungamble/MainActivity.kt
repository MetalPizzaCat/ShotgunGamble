package com.metalpizzacat.shotgungamble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.metalpizzacat.shotgungamble.game.Item
import com.metalpizzacat.shotgungamble.shake.ShakeConfig
import com.metalpizzacat.shotgungamble.shake.rememberShakeController
import com.metalpizzacat.shotgungamble.shake.shake
import com.metalpizzacat.shotgungamble.ui.theme.ShotgungambleTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShotgungambleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Playfield(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LoadoutDisplay(live: Int, blank: Int, modifier: Modifier = Modifier) {
    Row(modifier) {
        for (i in 0..<live) {
            Icon(
                painterResource(id = R.drawable.shell_live),
                contentDescription = "live shell",
                tint = Color.Unspecified,
                modifier = Modifier.padding(5.dp)
            )
        }
        for (i in 0..<blank) {
            Icon(
                painterResource(id = R.drawable.shell_blank),
                contentDescription = "blank shell",
                tint = Color.Unspecified,
                modifier = Modifier.padding(5.dp)
            )
        }
    }
}

@Composable
fun Playfield(modifier: Modifier = Modifier, viewModel: GameViewModel = viewModel()) {
    var showShotgunSelection by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.shotgun.currentShell) {
        if (!viewModel.playerTurn) {
            // fake delay to avoid everything happening instantly
            delay(1000)
            viewModel.runDealerLogic()
        }
    }
    LaunchedEffect(viewModel.showingGameSetup) {
        delay(3000)
        viewModel.showingGameSetup = false
    }
    Column(modifier = modifier) {
        Column {
            Text(text = "Total shells left: ${viewModel.shotgun.shellCount}")

            if (!viewModel.shotgun.isEmpty) {
                Text(
                    text = if (viewModel.shotgun.isCurrentShellLive) {
                        "Live"
                    } else {
                        "Blank"
                    }
                )
            }
        }
        PlayerBoard(
            health = viewModel.dealer.health,
            maxHealth = viewModel.maxHealthForRound,
            items = viewModel.dealer.items
        )

        Row {
            if (viewModel.playerTurn) {
                Button(onClick = {
                    if (!showShotgunSelection) {
                        showShotgunSelection = true
                    }

                }) {
                    Text(text = "Shotgun")
                }
            } else {
                Text(text = "Dealer's turn")
            }
            viewModel.shotgun.lastShellType?.let { isLastShellLive ->
                Text(
                    text = if (isLastShellLive) {
                        "Live!"
                    } else {
                        "Blank!"
                    },
                    modifier.shake(viewModel.shakeController)
                )
            }
        }

        PlayerBoard(
            health = viewModel.player.health,
            maxHealth = viewModel.maxHealthForRound,
            items = viewModel.player.items
        )
    }
    AnimatedVisibility(visible = showShotgunSelection) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(color = colorResource(id = R.color.selection_background))
        ) {
            Button(onClick = {
                viewModel.shootDealerAsPlayer()
                showShotgunSelection = false
            }) {
                Text(text = "Dealer")
            }
            Button(onClick = {
                viewModel.shootPlayerAsPlayer()
                showShotgunSelection = false
            }) {
                Text(text = "Self")
            }
        }
    }
    AnimatedVisibility(visible = viewModel.showingGameSetup) {
        Box(
            Modifier
                .fillMaxSize()
                .background(color = colorResource(id = R.color.selection_background))
        ) {
            LoadoutDisplay(
                live = viewModel.shotgun.liveCount,
                blank = viewModel.shotgun.blankCount,
                modifier = Modifier.align(Alignment.Center)
            )
        }

    }
}

@Composable
fun PlayerBoard(modifier: Modifier = Modifier, health: Int, maxHealth: Int, items: List<Item>) {
    Column(modifier = modifier) {
        Row {
            for (i in 0..<health) {
                Icon(Icons.Default.Favorite, contentDescription = "Heart")
            }
            for (i in 0..<(maxHealth - health)) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "No heart")
            }
        }
        items.chunked(4).forEach {
            Row {
                for (item in it) {
                    Icon(
                        painterResource(id = item.resource),
                        contentDescription = stringResource(id = item.descriptionRes)
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
        items = listOf(Item.BEER, Item.BEER, Item.PHONE, Item.HANDCUFFS, Item.HANDSAW)
    )
}