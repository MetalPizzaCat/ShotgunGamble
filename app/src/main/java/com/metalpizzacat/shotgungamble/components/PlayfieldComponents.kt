package com.metalpizzacat.shotgungamble.components

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.metalpizzacat.shotgungamble.GameViewModel
import com.metalpizzacat.shotgungamble.R
import com.metalpizzacat.shotgungamble.game.GameState
import com.metalpizzacat.shotgungamble.game.Item
import com.metalpizzacat.shotgungamble.shake.shake
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(
                text = if (viewModel.dealer.health == 0) {
                    "Player won!"
                } else {
                    "Dealer won!"
                }
            )
            Button(onClick = { viewModel.restartGame() }) {
                Text(text = "Start a new game")
            }
        }
    }
}

@Composable
fun ShowToast(text: String) {
    val context = LocalContext.current
    LaunchedEffect(text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayField(modifier: Modifier = Modifier, viewModel: GameViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(viewModel.shotgun.currentShell) {
        if (!viewModel.playerTurn) {
            // fake delay to avoid everything happening instantly
            delay(1000)
            viewModel.runDealerLogic()
        }
    }

    viewModel.lastRevealedShell?.let {
        ShowToast(
            text = "${stringResource(id = it.number.textResId)} ${
                if (it.isLive) {
                    "Live"
                } else {
                    "Blank"
                }
            }"
        )
    }

    LaunchedEffect(viewModel.lastRevealedShell) {
        delay(10)
        viewModel.lastRevealedShell = null
    }
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
            items = viewModel.dealer.items,
            handcuffed = viewModel.dealer.handcuffed,
            onItemSelected = { /*this is dealer board we do nothing*/ }
        )

        Row {
            if (viewModel.playerTurn) {
                Button(onClick = {
                    viewModel.isChoosingShootingTarget = true
                }) {
                    Text(
                        text = if (viewModel.shotgun.isSawedOff) {
                            "Sawed-off Shotgun"
                        } else {
                            "Shotgun"
                        }
                    )
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
            items = viewModel.player.items,
            handcuffed = viewModel.player.handcuffed,
            onItemSelected = { viewModel.startChoosingItem(it) }
        )
    }
}

@Composable
fun GameDisplay(modifier: Modifier = Modifier, viewModel: GameViewModel = viewModel()) {
    val context = LocalContext.current

    LaunchedEffect(viewModel.currentGameState) {
        if (viewModel.isShowingGameSetup) {
            if (viewModel.currentGameState == GameState.RESTOCKING) {
                Toast.makeText(context, "Out of ammo! Restocking...", Toast.LENGTH_LONG).show()
                delay(3000)
                viewModel.finishShowingOutofAmmoScreen()
            }
            delay(3000)
            viewModel.finishShowingGameSetup()
        }
    }

    AnimatedContent(targetState = viewModel.currentGameState, label = "Gamefield") { state ->
        when (state) {
            GameState.RESTOCKING -> {
                Box(modifier = modifier.fillMaxSize()) {
                    Column(modifier = Modifier.align(Alignment.Center)) {
                        Text(text = "Out of ammo!")
                        Row {
                            Text(text = "Dealer: ")
                            HealthDisplay(
                                health = viewModel.dealer.health,
                                maxHealth = viewModel.dealer.maxHealth
                            )
                        }
                        Row {
                            Text(text = "Player: ")
                            HealthDisplay(
                                health = viewModel.player.health,
                                maxHealth = viewModel.player.maxHealth
                            )
                        }
                    }
                }
            }

            GameState.NORMAL -> {
                PlayField(modifier)
            }

            GameState.SHOWING_GAME_SETUP -> {
                Box(
                    modifier
                        .fillMaxSize()
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

            GameState.USING_SHOTGUN -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(text = "Who do you want to shoot?")
                        Row {
                            Text(text = "Possible damage: ")
                            Text(text = "${viewModel.shotgun.damage} points")
                        }
                        Row {
                            Button(onClick = {
                                viewModel.shoot(
                                    target = viewModel.dealer,
                                    shooter = viewModel.player
                                )
                            }) {
                                Text(text = "Dealer")
                            }
                            Button(onClick = {
                                viewModel.shoot(
                                    target = viewModel.player,
                                    shooter = viewModel.player
                                )
                            }) {
                                Text(text = "Self")
                            }
                        }
                        Button(onClick = { viewModel.isChoosingShootingTarget = false }) {
                            Text(text = "Cancel")
                        }
                    }
                }
            }

            GameState.USING_ITEM -> {
                viewModel.currentItem?.let {
                    ItemUsageScreen(
                        modifier = modifier.fillMaxSize(),
                        item = it,
                        onAccepted = { viewModel.useItem(it, viewModel.player) },
                        onCanceled = { viewModel.stopChoosingItem() })
                }
            }

            GameState.GAME_OVER -> {
                GameOverScreen(modifier = modifier)
            }
        }
    }
}

@Composable
fun ItemUsageScreen(
    modifier: Modifier = Modifier,
    item: Item,
    onAccepted: () -> Unit,
    onCanceled: () -> Unit
) {
    Box(modifier = modifier) {
        Column(modifier = Modifier.align(alignment = Alignment.Center)) {
            Icon(painterResource(id = item.resource), contentDescription = "Item icon")
            Text(text = stringResource(id = item.displayNameRes), textAlign = TextAlign.Center)
            Text(text = stringResource(id = item.descriptionRes), textAlign = TextAlign.Left)
            Row(modifier.fillMaxWidth()) {
                Button(onClick = { onAccepted() }) {
                    Text(text = "Use")
                }
                Spacer(modifier = modifier.weight(1f))
                Button(onClick = { onCanceled() }) {
                    Text(text = "Don't use")
                }
            }
        }
    }
}

