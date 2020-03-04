/*
*Copyright 2019 Kyle Dahlin
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/

package com.kyledahlin.rulebot.bot

enum class LogLevel(val value: Int) {
    DEBUG(1), INFO(2), ERROR(2)
}

/**
 * Handle logging to stdout and stderr
 */
internal object Logger {

    private var logLevel = LogLevel.DEBUG

    fun setLogLevel(logLevel: LogLevel) {
        Logger.logLevel = logLevel
        logDebug("setting log level to $logLevel")
    }

    fun logDebug(message: String) = log(message, LogLevel.DEBUG)

    fun logInfo(message: String) = log(message, LogLevel.INFO)

    fun logError(message: String) = log(message, LogLevel.ERROR)

    private fun log(message: String, level: LogLevel) {
        if (level >= logLevel) {
            println(message)
        }
    }
}

