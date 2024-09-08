package com.metalpizzacat.shotgungamble.game

import com.metalpizzacat.shotgungamble.R

/**
 * Items that players can use during gameplay
 */
enum class Item(
    val resource: Int,
    val displayNameRes: Int = R.string.missing_name,
    val descriptionRes: Int = R.string.missing_desc
) {
    HANDSAW(R.drawable.missing, R.string.handsaw),
    BEER(R.drawable.missing),
    HANDCUFFS(R.drawable.missing),
    PHONE(R.drawable.missing)
}