package com.metalpizzacat.shotgungamble.game.dealer

import com.metalpizzacat.shotgungamble.game.Gambler

class Dealer(nameResId : Int) : Gambler(nameResId) {
    /**
     * All shells that are currently known to dealer
     */
    val knownShells : MutableMap<Int, Boolean> = HashMap()

    override fun reset(roundHealth: Int) {
        super.reset(roundHealth)
        knownShells.clear()
    }
}