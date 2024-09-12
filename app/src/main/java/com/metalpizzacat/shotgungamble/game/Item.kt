package com.metalpizzacat.shotgungamble.game

import com.metalpizzacat.shotgungamble.R

/**
 * Items that players can use during gameplay
 */
enum class Item(
    val resource: Int = R.drawable.missing,
    val displayNameRes: Int = R.string.missing_name,
    val descriptionRes: Int = R.string.missing_desc
) {
    HANDSAW(R.drawable.handsaw, R.string.handsaw_name, R.string.handsaw_desc),
    BEER(R.drawable.beer, R.string.beer_name, R.string.beer_desc),
    HANDCUFFS(R.drawable.handcuffs, R.string.cuff_name, R.string.cuff_desc),
    PHONE(R.drawable.phone, R.string.phone_name, R.string.phone_desc),
    ADRENALIN(R.drawable.missing, R.string.andrenalin_name, R.string.adrenalin_desc),
    INVERTER(R.drawable.inverter, R.string.inv_name, R.string.inv_desc),
    MEDICINE(R.drawable.medicine, R.string.med_name, R.string.med_desc),
    SMOKES(R.drawable.smokes, R.string.smoke_name, R.string.smoke_desc),
    GLASS(R.drawable.glass, R.string.glass_name, R.string.glass_desc)
}