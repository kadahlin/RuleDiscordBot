package com.kyledahlin.rulebot.config

import androidx.core.os.BuildCompat
import javax.inject.Inject

/**
 * Abstract away the need to refernce the static Build file
 */
open class BuildValues @Inject constructor() {
    open val serverAddress: String = BuildConfig.SERVER_ADDRESS
}