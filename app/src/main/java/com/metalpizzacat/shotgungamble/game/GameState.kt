package com.metalpizzacat.shotgungamble.game

enum class GameState {
    /**
     * Players are currently doing their stuff
     */
    NORMAL,

    /**
     * Game just started so we are showing current setup
     */
    SHOWING_GAME_SETUP,

    /**
     * There is no more ammo left, but both players are still alive
     */
    RESTOCKING,
    USING_SHOTGUN,
    USING_ITEM,
    /**
     * One of the players died
     */
    GAME_OVER
}