package com.kyledahlin.rulebot.bot

import com.kyledahlin.rulebot.DiscordCache
import com.kyledahlin.rulebot.RuleManager
import com.kyledahlin.rulebot.bot.jojorule.JojoMemeRule
import com.kyledahlin.rulebot.bot.leaguerule.LeagueRule
import com.kyledahlin.rulebot.bot.marxrule.MarxPassageRule
import com.kyledahlin.rulebot.bot.rockpaperscissorsrule.RockPaperScissorsRule
import com.kyledahlin.rulebot.bot.scoreboard.ScoreboardRule
import com.kyledahlin.rulebot.bot.soundboard.SoundboardRule
import com.kyledahlin.rulebot.bot.timeoutrule.TimeoutRule
import dagger.*
import dagger.multibindings.ElementsIntoSet
import discord4j.core.`object`.util.Snowflake
import javax.inject.Singleton

@Module
internal abstract class StorageModule {
    @Binds
    @Singleton
    abstract fun bindsLocalStorage(localStorageImpl: LocalStorageImpl): LocalStorage
}

@Module
internal class FunctionModule {

    @Provides
    @Singleton
    fun providesGetMetaData(cache: DiscordCache): GetDiscordWrapperForEvent = cache::getMetadataForEvent
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