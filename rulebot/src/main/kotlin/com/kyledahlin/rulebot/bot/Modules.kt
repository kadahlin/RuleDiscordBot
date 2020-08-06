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
import com.kyledahlin.rulebot.bot.java.JavaEventStorage
import com.kyledahlin.rulebot.bot.java.JavaEventStorageImpl
import com.kyledahlin.rulebot.bot.java.JavaLocalStorage
import com.kyledahlin.rulebot.bot.java.JavaLocalStorageImpl
import dagger.*
import dagger.multibindings.IntoSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Scope

@Module
internal abstract class StorageModule {
    @Binds
    @RuleBotScope
    abstract fun bindsLocalStorage(localStorageImpl: LocalStorageImpl): LocalStorage

    @Binds
    @RuleBotScope
    abstract fun bindsJavaLocalStorage(localStorageImpl: JavaLocalStorageImpl): JavaLocalStorage

    @Binds
    @RuleBotScope
    abstract fun bindsJavaEventStorage(localStorageImpl: JavaEventStorageImpl): JavaEventStorage
}

@Qualifier
@Retention
internal annotation class CoreRules

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

    @Provides
    @IntoSet
    @RuleBotScope
    fun providesConfigureBotRule(
        cache: DiscordCache,
        storage: LocalStorage,
        getDiscordWrapperForEvent: GetDiscordWrapperForEvent
    ): Rule {
        return ConfigureBotRule(cache, storage, getDiscordWrapperForEvent)
    }
}

@RuleBotScope
@Component(modules = [StorageModule::class, FunctionModule::class])
interface BotComponent {

    fun botBuilder(): RuleBot.Builder

    @Named("storage")
    fun storageDispatcher(): CoroutineDispatcher
    fun localStorage(): LocalStorage
    fun javaLocalStorage(): JavaLocalStorage
    fun javaEventStorage(): JavaEventStorage
    fun cache(): DiscordCache
    fun discordWrapper(): GetDiscordWrapperForEvent
    fun botIds(): GetBotIds
    fun analytics(): Analytics

    @Component.Builder
    interface Builder {
        fun build(): BotComponent

        @BindsInstance
        fun setToken(token: String): Builder

        @BindsInstance
        fun setAnalytics(analytics: Analytics): Builder
    }
}

@Scope
@Retention
internal annotation class RuleBotScope