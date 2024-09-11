package com.metalpizzacat.shotgungamble.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max

class Gambler {
    val items = mutableStateListOf<Item>()

    val lastDrawnItems : ArrayList<Item> = ArrayList()

    var health by mutableIntStateOf(0)
        private

    var handcuffed by mutableStateOf(false)

    var maxHealth by mutableIntStateOf(3)
        private set

    val canDoATurn: Boolean
        get() = !handcuffed

    fun dealDamage(damage : Int){
        health = max(0, health - damage)
    }

    fun addItem(item : Item){
        if(items.size < 8){
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