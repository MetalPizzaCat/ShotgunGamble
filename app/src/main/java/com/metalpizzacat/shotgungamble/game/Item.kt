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
    HANDSAW(R.drawable.handsaw, R.string.handsaw_name, R.string.handsaw_desc),
    BEER(R.drawable.beer, R.string.beer_name, R.string.beer_desc),
    HANDCUFFS(R.drawable.handcuffs, R.string.cuff_name, R.string.cuff_desc),
    PHONE(R.drawable.phone, R.string.phone_name, R.string.phone_desc)
}