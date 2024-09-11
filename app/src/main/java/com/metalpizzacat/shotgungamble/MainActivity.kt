package com.metalpizzacat.shotgungamble

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.metalpizzacat.shotgungamble.game.GameState
import com.metalpizzacat.shotgungamble.game.Item
import com.metalpizzacat.shotgungamble.shake.ShakeConfig
import com.metalpizzacat.shotgungamble.shake.rememberShakeController
import com.metalpizzacat.shotgungamble.shake.shake
import com.metalpizzacat.shotgungamble.ui.theme.ShotgungambleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShotgungambleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameDisplay(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Function to display current round starting loadout
 */
@Composable
fun LoadoutDisplay(
    live: Int,
    blank: Int,
    newPlayerItems: List<Item>,
    newDealerItems: List<Item>,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(text = "Items drawn by dealer")
        Row {
            for (item in newDealerItems) {
                Icon(
                    painterResource(id = item.resource),
                    contentDescription = stringResource(item.descriptionRes)
                )
            }
        }
        Row {
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
        Text(text = "Items drawn by player")
        Row {
            for (item in newPlayerItems) {
                Icon(
                    painterResource(id = item.resource),
                    contentDescription = stringResource(item.descriptionRes)
                )
            }
        }
    }
}

@Composable
fun GameOverScreen(modifier: Modifier = Modifier, viewModel: GameViewModel = viewModel()) {
    Box(modifier) {
        Text(
            text = if (viewModel.dealer.health == 0) {
                "Player won!"
            } else {
                "Dealer won!"
            },
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameDisplay(modifier: Modifier = Modifier, viewModel: GameViewModel = viewModel()) {
    var showShotgunSelection by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(viewModel.shotgun.currentShell) {
        if (!viewModel.playerTurn) {
            // fake delay to avoid everything happening instantly
            delay(1000)
            viewModel.runDealerLogic()
        }
    }
    LaunchedEffect(viewModel.currentGameState) {
        if (viewModel.isShowingGameSetup) {
            if (viewModel.currentGameState == GameState.RESTOCKING) {
                Toast.makeText(context, "Out of ammo! Restocking...", Toast.LENGTH_LONG).show()
            }
            delay(3000)
            viewModel.finishShowingGameSetup()
        }
    }
    if (viewModel.currentGameState != GameState.GAME_OVER) {
        Column(modifier = modifier) {
            Column {
                Text(
                    text = "Total shells left: ${viewModel.shotgun.shellCount}", modifier = Modifier
                        .combinedClickable(
                            onLongClickLabel = "Enable immortality",
                            onClick = {},
                            onLongClick = {
                                scope.launch {
                                    Toast
                                        .makeText(
                                            context,
                                            if (viewModel.isUsingImmortalityCheat) {
                                                "Disabled immortality"
                                            } else {
                                                "Enabled immortality"
                                            },
                                            Toast.LENGTH_LONG
                                        )
                                        .show()
                                    viewModel.isUsingImmortalityCheat =
                                        !viewModel.isUsingImmortalityCheat
                                }
                            })
                )

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
                        modifier
                            .shake(viewModel.shakeController)

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
                    viewModel.shoot(target = viewModel.dealer, shooter = viewModel.player)
                    showShotgunSelection = false
                }) {
                    Text(text = "Dealer")
                }
                Button(onClick = {
                    viewModel.shoot(target = viewModel.player, shooter = viewModel.player)
                    showShotgunSelection = false
                }) {
                    Text(text = "Self")
                }
            }
        }
        AnimatedVisibility(visible = viewModel.currentGameState == GameState.SHOWING_GAME_SETUP || viewModel.currentGameState == GameState.RESTOCKING) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color = colorResource(id = R.color.loadout_bg))
            ) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    LoadoutDisplay(
                        live = viewModel.shotgun.liveCount,
                        blank = viewModel.shotgun.blankCount,
                        newPlayerItems = viewModel.player.lastDrawnItems,
                        newDealerItems = viewModel.dealer.lastDrawnItems,
                    )
                }
            }

        }
    } else {
        GameOverScreen(modifier)
    }

}

@Composable
fun PlayerBoard(
    modifier: Modifier = Modifier,
    health: Int,
    maxHealth: Int,
    items: List<Item>
) {
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