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

import com.kyledahlin.rulebot.DiscordCache
import com.kyledahlin.rulebot.RuleManager
import com.kyledahlin.rulebot.bot.java.*
import com.kyledahlin.rulebot.bot.jojorule.JojoMemeRule
import com.kyledahlin.rulebot.bot.leaguerule.LeagueRule
import com.kyledahlin.rulebot.bot.marxrule.MarxPassageRule
import com.kyledahlin.rulebot.bot.rockpaperscissorsrule.RockPaperScissorsRule
import com.kyledahlin.rulebot.bot.scoreboard.ScoreboardRule
import com.kyledahlin.rulebot.bot.soundboard.SoundboardRule
import com.kyledahlin.rulebot.bot.timeoutrule.TimeoutRule
import dagger.*
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import javax.inject.Named
import javax.inject.Singleton

@Module
internal abstract class StorageModule {
    @Binds
    @Singleton
    abstract fun bindsLocalStorage(localStorageImpl: LocalStorageImpl): LocalStorage

    @Binds
    @Singleton
    abstract fun bindsJavaLocalStorage(localStorageImpl: JavaLocalStorageImpl): JavaLocalStorage

    @Binds
    @Singleton
    abstract fun bindsJavaEventStorage(localStorageImpl: JavaEventStorageImpl): JavaEventStorage
}

@Module
internal class FunctionModule {

    @Provides
    @Singleton
    fun providesGetMetaData(cache: DiscordCache): GetDiscordWrapperForEvent = cache::getMetadataForEvent

    @ObsoleteCoroutinesApi
    @Provides
    @Singleton
    @Named("storage")
    fun providesStorageDispatcher(): CoroutineDispatcher {
        return newSingleThreadContext("storage")
    }
}

@Module
internal class RuleModule {
    @Provides
    @ElementsIntoSet
    fun provideRules(
        jojoRule: JojoMemeRule,
        leagueRule: LeagueRule,
        scoreboardRule: ScoreboardRule,
        soundboardRule: SoundboardRule,
        configureBotRule: ConfigureBotRule,
        rockPaperScissorsRule: RockPaperScissorsRule,
        marxPassageRule: MarxPassageRule,
        timeoutRule: TimeoutRule
    ): Set<Rule> {
        return setOf(
            jojoRule,
            leagueRule,
            scoreboardRule,
            soundboardRule,
            configureBotRule,
            rockPaperScissorsRule,
            timeoutRule,
            marxPassageRule
        )
    }

    @Provides
    @IntoSet
    fun providesJavaRule(sampleJavaRule: SampleJavaRule): Rule {
        return sampleJavaRule
    }
}

@Singleton
@Component(modules = [StorageModule::class, FunctionModule::class, RuleModule::class])
internal interface BotComponent {

    fun ruleManager(): RuleManager

    @Component.Builder
    interface Builder {
        fun build(): BotComponent

        @BindsInstance
        fun setBotIds(snowflakes: Set<Snowflake>): Builder
    }
}