package com.metalpizzacat.shotgungamble

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.metalpizzacat.shotgungamble.game.Gambler
import com.metalpizzacat.shotgungamble.game.Item
import com.metalpizzacat.shotgungamble.game.Shotgun
import com.metalpizzacat.shotgungamble.shake.ShakeConfig
import com.metalpizzacat.shotgungamble.shake.ShakeController
import kotlin.math.max
import kotlin.math.min

class GameViewModel : ViewModel() {

    val player: Gambler = Gambler()
    val dealer: Gambler = Gambler()

    val shakeController = ShakeController()

    var gameOver by mutableStateOf(false)
        private set

    var playerTurn by mutableStateOf(true)
        private set

    var maxHealthForRound by mutableIntStateOf(5)
        private set

    val shotgun: Shotgun = Shotgun()


    fun resetGame() {
        maxHealthForRound = 5
        player.reset(maxHealthForRound)
        dealer.reset(maxHealthForRound)
        shotgun.generateShells()
        gameOver = false
    }


    fun endRound() {
        gameOver = true
    }

    fun displayShell() {
        shakeController.shake(
            ShakeConfig(
                5,
                intensity = 50000f,
                translateX = 10f,
                translateY = 10f
            )
        )
    }

    fun shootPlayerAsPlayer() {
        displayShell()
        if (!shotgun.shoot()) {
            return
        }
        shotgun.lastShellType?.let {
            if (it) {
                player.dealDamage(shotgun.damage)
                playerTurn = false
                return
            }
        }
    }

    /**
     * Shoot dealer as player
     * @return True if successfully shot, false if shell was blank
     */
    fun shootDealerAsPlayer() {
        displayShell()
        if (!shotgun.shoot()) {
            return
        }
        shotgun.lastShellType?.let {
            playerTurn = false
            if (!it) {
                return
            }
            dealer.dealDamage(shotgun.damage)
            if (dealer.health <= 0) {
                endRound()
            }
        }
    }

    fun shootPlayerAsDealer(){
        displayShell()
        if (!shotgun.shoot()) {
            return
        }
        shotgun.lastShellType?.let {
            playerTurn = true
            if (!it) {
                return
            }
            player.dealDamage(shotgun.damage)
            if (player.health <= 0) {
                endRound()
            }
        }
    }

    fun shootDealerAsDealer(){
        displayShell()
        if (!shotgun.shoot()) {
            return
        }
        shotgun.lastShellType?.let {
            if (it) {
                dealer.dealDamage(shotgun.damage)
                playerTurn = true
                return
            }
        }
    }

    fun runDealerLogic() {
        val chanceOfLive =  (shotgun.liveCount - shotgun.shotShells.count { it }).toFloat() /  shotgun.liveCount.toFloat()
        val chanceOfBlank =  (shotgun.blankCount - shotgun.shotShells.count { !it }).toFloat() /  shotgun.blankCount.toFloat()

        if(chanceOfBlank > chanceOfLive){
            shootDealerAsDealer()
        }
        else{
            shootPlayerAsDealer()
        }
    }

    init {
        resetGame()
    }
}