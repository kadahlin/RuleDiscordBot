/*
*Copyright 2021 Kyle Dahlin
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
package com.kyledahlin.myrulebot

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.kyledahlin.rulebot.GuildEmojiWrapper
import discord4j.common.util.Snowflake
import org.assertj.core.api.Assertions

internal fun guildEmoji(id: Snowflake = Snowflake.of(1L), name: String = "", isAnimated: Boolean = false) =
    GuildEmojiWrapper(id, name, isAnimated)

internal fun <T> Either<T, *>.assertLeft(value: T) {
    Assertions.assertThat(this).isEqualTo(value.left())
}

internal fun <T> Either<*, T>.assertRight(value: T) {
    Assertions.assertThat(this).isEqualTo(value.right())
}