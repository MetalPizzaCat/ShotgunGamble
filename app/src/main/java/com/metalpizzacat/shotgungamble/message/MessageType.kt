package com.metalpizzacat.shotgungamble.message

import com.metalpizzacat.shotgungamble.R

enum class MessageType(val stringResId : Int = R.string.missing_desc) {
    SHOT_BLANK(R.string.shot_blank),
    SHOT_LIVE(R.string.shot_live),
    USED_BEER(R.string.used_beer),
    USED_SMOKE(R.string.used_smoke),
    USED_SAW(R.string.used_saw),
    USED_PILLS_SUCCESS(R.string.used_pill_success),
    USED_PILLS_FAIL(R.string.used_pill_fail),
    USED_CUFFS(R.string.used_handcuffs),
    USED_ADRENALIN(R.string.used_adrenalin),
    USED_INVERTER(R.string.used_inverter),
}