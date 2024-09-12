package com.metalpizzacat.shotgungamble.message

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.metalpizzacat.shotgungamble.game.Gambler

class MessageController {
    var currentMessage by mutableStateOf<MessageConfig?>(null)
        private set

    fun displayMessage(source: Gambler, messageType: MessageType) {
        currentMessage = MessageConfig(source, messageType)
    }

    fun clearMessage() {
        currentMessage = null
    }
}