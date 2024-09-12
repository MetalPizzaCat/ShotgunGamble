package com.metalpizzacat.shotgungamble

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.metalpizzacat.shotgungamble.game.Gambler
import com.metalpizzacat.shotgungamble.game.GameState
import com.metalpizzacat.shotgungamble.game.Item
import com.metalpizzacat.shotgungamble.game.RevealedShellData
import com.metalpizzacat.shotgungamble.game.RevealedShellNaming
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

    var lastRevealedShell by mutableStateOf<RevealedShellData?>(null)


    /**
     * If true then the setup screen should be visible
     */
    val isShowingGameSetup: Boolean
        get() = currentGameState == GameState.RESTOCKING || currentGameState == GameState.SHOWING_GAME_SETUP


    var playerTurn by mutableStateOf(true)
        private set

    var maxHealthForRound by mutableIntStateOf(5)
        private set


    /**
     * Is player currently using shotgun and determining who to shoot?
     */
    var isChoosingShootingTarget: Boolean
        get() = currentGameState == GameState.USING_SHOTGUN
        set(value) {
            currentGameState = if (value) {
                GameState.USING_SHOTGUN
            } else {
                GameState.NORMAL
            }
        }

    /**
     * Item that was currently selected to be considered to be used
     */
    var selectedItem: Item? = null
        set(value) {
            currentGameState = if (value != null) {
                GameState.USING_ITEM
            } else {
                GameState.NORMAL
            }
            field = value
        }

    val shotgun: Shotgun = Shotgun()

    /**
     * Switches to the opponent turn of it the opponent has to skip a turn, remains the same
     */
    private fun advanceTurn() {
        val nextGambler: Gambler = if (!playerTurn) {
            player
        } else {
            dealer
        }

        if (!nextGambler.handcuffed) {
            playerTurn = !playerTurn
        } else {
            nextGambler.handcuffed = false
        }
    }

    fun restartGame() {
        maxHealthForRound = Random.nextInt(2, 6)
        player.reset(maxHealthForRound)
        dealer.reset(maxHealthForRound)
        currentGameState = GameState.SHOWING_GAME_SETUP
        softRestartRound()
    }

    fun finishShowingOutOfAmmoScreen() {
        currentGameState = GameState.SHOWING_GAME_SETUP
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
        player.lastDrawnItems.clear()
        dealer.lastDrawnItems.clear()
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

    /**
     * Additional function that will trigger end of round events if health of any player drops below 0
     */
    private fun damageGambler(gambler: Gambler, damage: Int) {
        if (!isUsingImmortalityCheat) {
            gambler.dealDamage(damage)
        }
        if (gambler.health <= 0) {
            endRound()
        }
    }

    /**
     * Shoot given target, this will cause state of the shell to be displayed and round to end if one of the players dies
     * @param target Who to apply damage to
     * @param shooter Who is shooting
     */
    fun shoot(target: Gambler, shooter: Gambler) {
        if (currentGameState != GameState.USING_SHOTGUN && currentGameState != GameState.NORMAL || shotgun.currentShell >= shotgun.shellCount) {
            return
        }
        currentGameState = GameState.NORMAL


        if (shotgun.isCurrentShellLive) {
            damageGambler(target, shotgun.damage)
            advanceTurn()
        } else if (target == shooter) {
            // skip a turn
        } else {
            // if we shot with a blank we loose turn
            advanceTurn()
        }

        shotgun.shoot()
        displayShell()
        if (shotgun.isEmpty) {
            currentGameState = GameState.RESTOCKING
            softRestartRound()
        }

    }

    private fun ejectShell() {
        displayShell()
        shotgun.shoot()
        if (shotgun.isEmpty) {
            currentGameState = GameState.RESTOCKING
            softRestartRound()
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

    private fun applyItem(item: Item, user: Gambler, target: Gambler): Boolean {
        when (item) {
            Item.HANDSAW -> {
                if (!shotgun.isSawedOff) {
                    shotgun.isSawedOff = true
                } else {
                    return false
                }
            }

            Item.BEER -> ejectShell()
            Item.HANDCUFFS -> {
                if (target.handcuffed) {
                    return false
                }
                target.handcuffed = true
            }

            Item.PHONE -> {
                if (shotgun.currentShell == shotgun.shellCount - 1) {
                    lastRevealedShell = RevealedShellData(
                        RevealedShellNaming.TOUGH_LUCK,
                        false
                    )
                } else {
                    val shellNumber = Random.nextInt(shotgun.currentShell, shotgun.shellCount)
                    lastRevealedShell = RevealedShellData(
                        RevealedShellNaming.entries[shellNumber - shotgun.currentShell],
                        shotgun[shellNumber]
                    )
                }
            }

            Item.ADRENALIN -> TODO()
            Item.INVERTER -> shotgun.isShellInverted = !shotgun.isShellInverted
            Item.MEDICINE -> {
                if (Random.nextBoolean()) {
                    user.dealDamage(-2)
                } else {
                    damageGambler(user, 1)
                }
            }

            Item.SMOKES -> user.dealDamage(-1)
            Item.GLASS -> {
                lastRevealedShell = RevealedShellData(
                    RevealedShellNaming.CURRENT,
                    shotgun.isCurrentShellLive
                )
            }
        }
        return true
    }

    /**
     * Use item and remove it from the inventory of the holder
     * If item can not be used(For example, if opponent is already using handcuffs and you are trying to use handcuffs) nothing will happen
     *
     * @param item Which item to use
     * @param user Who is using the item(Depending on the item itself the effect will apply either to the user or the opposite of the user)
     * @param holder Player that is holding the item and who will use the item on the successful use
     */
    fun useItem(item: Item, user: Gambler, holder: Gambler) {
        applyItem(
            item, if (user != player) {
                dealer
            } else {
                player
            }, if (user == player) {
                dealer
            } else {
                player
            }
        )
        currentGameState = GameState.NORMAL
        holder.items.remove(item)
    }


    init {
        restartGame()
    }
}