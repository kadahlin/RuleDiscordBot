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

import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.RuleBot
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext
import javax.inject.Named
import javax.inject.Scope

@Module
internal class FunctionModule {

    @Provides
    @RuleBotScope
    @Named("storage")
    fun providesStorageDispatcher(): CoroutineDispatcher {
        return newSingleThreadContext("storage")
    }
}

@RuleBotScope
@Component(modules = [FunctionModule::class])
internal interface BotComponent {

    fun botBuilder(): RuleBot.Builder

    @Named("storage")
    fun storageDispatcher(): CoroutineDispatcher
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