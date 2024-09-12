package com.metalpizzacat.shotgungamble.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min

class Gambler {
    val items = mutableStateListOf<Item>()

    val lastDrawnItems: ArrayList<Item> = ArrayList()

    var health by mutableIntStateOf(0)
        private set

    var handcuffed by mutableStateOf(false)

    var maxHealth by mutableIntStateOf(3)
        private set

    /**
     * Apply damage to the gambler or heal gambler if damage is negative
     * @param damage How points to apply
     */
    fun dealDamage(damage: Int) {
        health = min(maxHealth, max(0, health - damage))
    }

    fun addItem(item: Item) {
        if (items.size < 8) {
            items.add(item)
            lastDrawnItems.add(item)
        }
    }

    fun reset(roundHealth: Int) {
        maxHealth = roundHealth
        health = roundHealth

        items.clear()
        lastDrawnItems.clear()
    }
}