package com.metalpizzacat.shotgungamble.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

class Shotgun {
    /**
     * Current contents of the shotgun
     */
    private var shells: ArrayList<Boolean> = ArrayList()

    /**
     * Which shells were already shot
     */
    val shotShells = mutableStateListOf<Boolean>()

    /**
     * How many live shells there were at the start of the round
     */
    var liveCount by mutableIntStateOf(0)
        private set

    /**
     * How many blank shells there were at the start of the round
     */
    var blankCount by mutableIntStateOf(0)
        private set

    /**
     * Number of the current shell that will be shot. Also can be thought of as the counter to tell how many shells were shot
     */
    var currentShell by mutableIntStateOf(0)
        private set

    /**
     * If shell is inverted then when shot it will do the opposite
     * This has no effect on the actual load out and will get reset once shotgun is used
     */
    var isShellInverted: Boolean = false

    /**
     * Is the shell that would be used if shotgun is shot live
     */
    val isCurrentShellLive: Boolean
        get() = if (isShellInverted) {
            !shells[currentShell]
        } else {
            shells[currentShell]
        }

    /**
     * If true that means that there are no shells left to shoot
     */
    val isEmpty: Boolean
        get() = currentShell >= shells.size

    /**
     * What kind of shell was used in the last shot
     */
    var lastShellType by mutableStateOf<Boolean?>(null)
        private set

    /**
     * Will the shotgun deal double damage or not
     */
    var isSawedOff by mutableStateOf(false)

    /**
     * Total amount of shells used in this round
     */
    val shellCount: Int
        get() = shells.size

    /**
     * How many unused shells are left in the gun
     */
    val remainingShellCount: Int
        get() = shells.size - currentShell

    /**
     * How many points of damage should the shotgun deal.
     * * 1 if normal
     * * 2 is sawed off
     */
    val damage: Int
        get() = if (isSawedOff) {
            2
        } else {
            1
        }


    operator fun get(i: Int) = if (i == currentShell) {
        isCurrentShellLive
    } else {
        shells[i]
    }

    /**
     * Advance the shell counter by one and update the state based on the used shell
     * @return false if there are no shells left
     */
    fun shoot() {
        if (currentShell >= shells.size) {
            return
        }
        // shotgun is restored after each shot no matter what
        isSawedOff = false
        isShellInverted = false
        shotShells.add(isCurrentShellLive)
        lastShellType = isCurrentShellLive
        currentShell++
    }

    fun generateShells(testLayout: ShotgunTestLayout? = null) {
        shells = ArrayList()
        if (testLayout != null) {
            liveCount = testLayout.liveCount
            blankCount = testLayout.blankCount
            for (shell in testLayout.shells) {
                shells.add(shell)
            }
        } else {
            liveCount = Random.nextInt(1, 5)
            blankCount = Random.nextInt(1, 5)

            for (i in 0..<liveCount) {
                shells.add(true)
            }
            for (i in 0..<blankCount) {
                shells.add(false)
            }
            shells.shuffle()
        }
        currentShell = 0
        shotShells.clear()
        lastShellType = null
    }
}