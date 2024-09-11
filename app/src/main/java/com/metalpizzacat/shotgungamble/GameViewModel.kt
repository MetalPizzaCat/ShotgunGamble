package com.metalpizzacat.shotgungamble

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.metalpizzacat.shotgungamble.game.Gambler
import com.metalpizzacat.shotgungamble.game.GameState
import com.metalpizzacat.shotgungamble.game.Item
import com.metalpizzacat.shotgungamble.game.Shotgun
import com.metalpizzacat.shotgungamble.shake.ShakeConfig
import com.metalpizzacat.shotgungamble.shake.ShakeController
import kotlin.random.Random

class GameViewModel : ViewModel() {

    val player: Gambler = Gambler()
    val dealer: Gambler = Gambler()

    val shakeController = ShakeController()

    var currentGameState by mutableStateOf(GameState.NORMAL)
        private set

    /**
     * If true neither player or dealer will receive actual damage from the shot
     */
    var isUsingImmortalityCheat by mutableStateOf(false)

    /**
     * Which item is player currently trying to use
     */
    var currentItem by mutableStateOf<Item?>(null)
        private set

    /**
     * If true then the setup screen should be visible
     */
    val isShowingGameSetup: Boolean
        get() = currentGameState == GameState.RESTOCKING || currentGameState == GameState.SHOWING_GAME_SETUP


    var playerTurn by mutableStateOf(true)
        private set

    var maxHealthForRound by mutableIntStateOf(5)
        private set


    val shotgun: Shotgun = Shotgun()


    fun restartGame() {
        maxHealthForRound = Random.nextInt(2, 6)
        player.reset(maxHealthForRound)
        dealer.reset(maxHealthForRound)
        currentGameState = GameState.SHOWING_GAME_SETUP
        softRestartRound()
    }

    fun startChoosingShootingTarget() {
        currentGameState = GameState.USING_SHOTGUN
    }

    fun stopChoosingShootingTarget() {
        currentGameState = GameState.NORMAL
    }

    fun startChoosingItem(item: Item) {
        currentGameState = GameState.USING_ITEM
        currentItem = item
    }


    fun stopChoosingItem() {
        currentGameState = GameState.NORMAL
        currentItem = null
    }


    private fun endRound() {
        currentGameState = GameState.GAME_OVER
    }

    /**
     * Soft restart the game putting new ammo in the shotgun and giving players new items
     * This does not change players health
     */
    private fun softRestartRound() {
        shotgun.generateShells()
        playerTurn = true
        val itemsForRound = Random.nextInt(1, 5)
        for (i in 0..<itemsForRound) {
            player.addItem(Item.entries.random())
        }
        for (i in 0..<itemsForRound) {
            dealer.addItem(Item.entries.random())
        }
    }

    private fun displayShell() {
        shakeController.shake(
            ShakeConfig(
                5,
                intensity = 50000f,
                translateX = 10f,
                translateY = 10f
            )
        )
    }

    fun finishShowingGameSetup() {
        currentGameState = GameState.NORMAL
    }

    fun shoot(target: Gambler, shooter: Gambler) {
        if (currentGameState != GameState.USING_SHOTGUN && currentGameState != GameState.NORMAL) {
            return
        }
        currentGameState = GameState.NORMAL
        displayShell()
        shotgun.shoot()
        shotgun.lastShellType?.let { live ->
            if (live) {
                if (!isUsingImmortalityCheat) {
                    target.dealDamage(shotgun.damage)
                }
                playerTurn = !playerTurn
            } else if (target == shooter) {
                // skip a turn
            } else {
                // if we shot with a blank we loose turn
                playerTurn = !playerTurn
            }
            if (target.health <= 0) {
                endRound()
            } else if (shotgun.isEmpty) {
                currentGameState = GameState.RESTOCKING
                softRestartRound()
            }
        }
    }

    fun runDealerLogic() {
        val chanceOfLive =
            (shotgun.liveCount - shotgun.shotShells.count { it }).toFloat() / shotgun.liveCount.toFloat()
        val chanceOfBlank =
            (shotgun.blankCount - shotgun.shotShells.count { !it }).toFloat() / shotgun.blankCount.toFloat()

        if (chanceOfBlank > chanceOfLive) {
            shoot(target = dealer, shooter = dealer)
        } else {
            shoot(target = player, shooter = dealer)
        }
    }

    init {
        restartGame()
    }
}