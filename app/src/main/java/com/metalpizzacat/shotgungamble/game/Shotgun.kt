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
     * Is the shell that would be used if shotgun is shot live
     */
    val isCurrentShellLive: Boolean
        get() = shells[currentShell]

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
        private set

    /**
     * Total amount of shells used in this round
     */
    val shellCount: Int
        get() = shells.size

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

    /**
     * Advance the shell counter by one and update the state based on the used shell
     * @return false if there are no shells left
     */
    fun shoot(): Boolean {
        if (currentShell >= shells.size) {
            return false
        }
        // shotgun is restored after each shot no matter what
        isSawedOff = false
        shotShells.add(isCurrentShellLive)
        lastShellType = isCurrentShellLive
        currentShell++
        return true
    }

    init {
        generateShells()
    }

    fun generateShells() {
        liveCount = 1//Random.nextInt(1, 5)
        blankCount = 1//Random.nextInt(1, 5)
        currentShell = 0
        shells = ArrayList()
        for (i in 0..<liveCount) {
            shells.add(true)
        }
        for (i in 0..<blankCount) {
            shells.add(false)
        }
        shells.shuffle()
        shotShells.clear()
        lastShellType = null
    }
}