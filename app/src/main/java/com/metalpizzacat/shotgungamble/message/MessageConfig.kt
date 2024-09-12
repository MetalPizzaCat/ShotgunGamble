package com.metalpizzacat.shotgungamble.message

import com.metalpizzacat.shotgungamble.game.Gambler

data class MessageConfig(val source: Gambler, val messageType: MessageType)
