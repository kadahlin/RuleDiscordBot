package com.kyledahlin.rulebot.bot

import com.kyledahlin.rulebot.RuleManager
import com.kyledahlin.rulebot.bot.jojorule.JojoMemeRule
import com.kyledahlin.rulebot.bot.leaguerule.LeagueRule
import com.kyledahlin.rulebot.bot.scoreboard.ScoreboardRule
import com.kyledahlin.rulebot.bot.soundboard.SoundboardRule
import dagger.*
import dagger.multibindings.ElementsIntoSet
import discord4j.core.`object`.util.Snowflake

@Module
internal abstract class StorageModule {
    @Binds
    abstract fun bindsLocalStorage(localStorageImpl: LocalStorageImpl): LocalStorage
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
        timeoutRule: TimeoutRule
    ): Set<Rule> {
        return setOf(
            jojoRule,
            leagueRule,
            scoreboardRule,
            soundboardRule,
            configureBotRule,
            rockPaperScissorsRule,
            timeoutRule
        )
    }
}

@Component(modules = [StorageModule::class, RuleModule::class])
internal interface BotComponent {

    fun ruleManager(): RuleManager

    @Component.Builder
    interface Builder {
        fun build(): BotComponent

        @BindsInstance
        fun setBotIds(snowflakes: Set<Snowflake>): Builder
    }
}