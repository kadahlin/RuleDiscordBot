/*
*Copyright 2020 Kyle Dahlin
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

import com.kyledahlin.rulebot.DiscordCache
import com.kyledahlin.rulebot.RuleBot
import com.kyledahlin.rulebot.analytics.Analytics
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import org.litote.kmongo.coroutine.CoroutineDatabase
import javax.inject.Named
import javax.inject.Scope

@Module
internal class FunctionModule {

    @Provides
    @RuleBotScope
    fun providesGetDiscordWrapper(cache: DiscordCache): GetDiscordWrapperForEvent = cache::getWrapperForEvent

    @Provides
    @RuleBotScope
    fun providesGetBotIds(cache: DiscordCache): GetBotIds = cache::getBotIds

    @ObsoleteCoroutinesApi
    @Provides
    @RuleBotScope
    @Named("storage")
    fun providesStorageDispatcher(): CoroutineDispatcher {
        return newSingleThreadContext("storage")
    }
}

@RuleBotScope
@Component(modules = [FunctionModule::class])
interface BotComponent {

    fun botBuilder(): RuleBot.Builder

    @Named("storage")
    fun storageDispatcher(): CoroutineDispatcher
    fun cache(): DiscordCache
    fun discordWrapper(): GetDiscordWrapperForEvent
    fun botIds(): GetBotIds
    fun analytics(): Analytics
    fun database(): CoroutineDatabase

    @Component.Builder
    interface Builder {
        fun build(): BotComponent

        @BindsInstance
        fun setToken(token: String): Builder

        @BindsInstance
        fun setDatabase(database: CoroutineDatabase): Builder

        @BindsInstance
        fun setAnalytics(analytics: Analytics): Builder
    }
}

@Scope
@Retention
internal annotation class RuleBotScope