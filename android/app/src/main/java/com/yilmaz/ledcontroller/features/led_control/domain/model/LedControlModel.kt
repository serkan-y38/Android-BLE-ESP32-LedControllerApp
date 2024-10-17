package com.yilmaz.ledcontroller.features.led_control.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LedControlModel(
    val mode: String,
    val isFlashEnabled: Boolean? = null
)
