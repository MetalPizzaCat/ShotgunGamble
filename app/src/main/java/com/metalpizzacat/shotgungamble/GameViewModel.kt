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
import com.metalpizzacat.shotgungamble.message.MessageController
import com.metalpizzacat.shotgungamble.message.MessageType
import com.metalpizzacat.shotgungamble.shake.ShakeConfig
import com.metalpizzacat.shotgungamble.shake.ShakeController
import kotlinx.coroutines.delay
import kotlin.random.Random

class GameViewModel : ViewModel() {

    val player: Gambler = Gambler(R.string.player)
    val dealer: Gambler = Gambler(R.string.dealer)

    val shotgun: Shotgun = Shotgun()

    val shakeController = ShakeController()
    val messageController = MessageController()

    var currentGameState by mutableStateOf(GameState.NORMAL)
        private set

    var lastRevealedShell by mutableStateOf<RevealedShellData?>(null)

    var isPlayerStealingFromDealer by mutableStateOf(false)
        private set

    /**
     * If true then the setup screen should be visible
     */
    val isShowingGameSetup: Boolean
        get() = currentGameState == GameState.RESTOCKING || currentGameState == GameState.SHOWING_GAME_SETUP


    var isPlayerTurn by mutableStateOf(true)
        private set

    /**
     * Which gambler is currently doing stuff
     */
    val currentPlayer: Gambler
        get() = if (isPlayerTurn) {
            player
        } else {
            dealer
        }

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



    /**
     * Switches to the opponent turn of it the opponent has to skip a turn, remains the same
     */
    private fun advanceTurn() {
        val nextGambler: Gambler = if (!isPlayerTurn) {
            player
        } else {
            dealer
        }

        if (!nextGambler.handcuffed) {
            isPlayerTurn = !isPlayerTurn
        } else {
            nextGambler.handcuffed = false
        }
    }

    private fun getOppositeGambler(gambler: Gambler): Gambler =
        if (gambler == player) {
            dealer
        } else {
            player
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
        isPlayerTurn = true
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
        gambler.dealDamage(damage)
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


        messageController.displayMessage(
            currentPlayer,
            if (shotgun.isCurrentShellLive) {
                MessageType.SHOT_LIVE
            } else {
                MessageType.SHOT_BLANK
            }
        )
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

    suspend fun runDealerLogic() {
        delay(100)
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

    /**
     * Blindly apply the item logic
     * This function performs no checks
     * @param item Which item to use
     * @param user Who is using the item
     * @param target Who is the target of the item effect(if effect doesn't apply to user)
     */
    private fun applyItem(item: Item, user: Gambler, target: Gambler) {
        when (item) {
            Item.HANDSAW -> {
                shotgun.isSawedOff = true
                messageController.displayMessage(currentPlayer, MessageType.USED_SAW)
            }

            Item.BEER -> {
                ejectShell()
                messageController.displayMessage(currentPlayer, MessageType.USED_BEER)
            }

            Item.HANDCUFFS -> {
                target.handcuffed = true
                messageController.displayMessage(currentPlayer, MessageType.USED_CUFFS)
            }

            Item.PHONE -> {
                if (shotgun.currentShell == shotgun.shellCount - 1) {
                    lastRevealedShell = RevealedShellData(
                        RevealedShellNaming.TOUGH_LUCK,
                        false
                    )
                } else {
                    val shellNumber =
                        Random.nextInt(shotgun.currentShell + 1, shotgun.shellCount)
                    lastRevealedShell = RevealedShellData(
                        RevealedShellNaming.entries[shellNumber - shotgun.currentShell],
                        shotgun[shellNumber]
                    )
                }
            }

            Item.ADRENALIN -> {
                isPlayerStealingFromDealer = true
                messageController.displayMessage(currentPlayer, MessageType.USED_ADRENALIN)
            }

            Item.INVERTER -> {
                shotgun.isShellInverted = !shotgun.isShellInverted
                messageController.displayMessage(currentPlayer, MessageType.USED_INVERTER)
            }

            Item.MEDICINE -> {
                if (Random.nextBoolean()) {
                    user.dealDamage(-2)
                    messageController.displayMessage(currentPlayer, MessageType.USED_PILLS_SUCCESS)
                } else {
                    damageGambler(user, 1)
                    messageController.displayMessage(currentPlayer, MessageType.USED_PILLS_FAIL)
                }
            }

            Item.SMOKES -> {
                user.dealDamage(-1)
                messageController.displayMessage(currentPlayer, MessageType.USED_SMOKE)
            }

            Item.GLASS -> {
                lastRevealedShell = RevealedShellData(
                    RevealedShellNaming.CURRENT,
                    shotgun.isCurrentShellLive
                )
            }
        }
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
        applyItem(item, user, getOppositeGambler(user))
        currentGameState = GameState.NORMAL
        holder.items.remove(item)
        if (item != Item.ADRENALIN) {
            isPlayerStealingFromDealer = false
        }
    }

    fun canUseItem(item: Item, user: Gambler): Boolean {
        when (item) {
            Item.HANDSAW -> return !shotgun.isSawedOff
            Item.MEDICINE, Item.GLASS, Item.SMOKES, Item.INVERTER, Item.PHONE, Item.BEER -> return true
            Item.HANDCUFFS -> return !getOppositeGambler(user).handcuffed
            Item.ADRENALIN -> {
                val excludedItems: ArrayList<Item> = ArrayList()
                excludedItems.add(Item.ADRENALIN)
                if (!shotgun.isSawedOff) {
                    excludedItems.add(Item.HANDSAW)
                }
                if (!getOppositeGambler(user).handcuffed) {
                    excludedItems.add(Item.HANDCUFFS)
                }
                return getOppositeGambler(user).canItemsBeStolen(excludedItems)
            }
        }
    }


    init {
        restartGame()
    }
}