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
package com.kyledahlin.myrulebot

import com.kyledahlin.myrulebot.corona.CoronaRule
import com.kyledahlin.myrulebot.jojorule.JojoMemeRule
import com.kyledahlin.myrulebot.leaguerule.LeagueRule
import com.kyledahlin.myrulebot.marxrule.MarxPassageRule
import com.kyledahlin.myrulebot.rockpaperscissorsrule.RockPaperScissorsRule
import com.kyledahlin.myrulebot.scoreboard.ScoreboardRule
import com.kyledahlin.myrulebot.soundboard.SoundboardRule
import com.kyledahlin.myrulebot.timeoutrule.TimeoutRule
import com.kyledahlin.rulebot.bot.BotComponent
import com.kyledahlin.rulebot.bot.Rule
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import javax.inject.Scope

@Scope
@Retention
annotation class MyRuleBotScope

@Component(modules = [MyRuleBotModule::class], dependencies = [BotComponent::class])
@MyRuleBotScope
interface MyRuleBotComponent {
    fun rules(): Set<Rule>
}

@Module
internal class MyRuleBotModule {
    companion object {
        @JvmStatic
        @Provides
        @MyRuleBotScope
        @ElementsIntoSet
        fun providesRules(
            jojoMemeRule: JojoMemeRule,
            leagueRule: LeagueRule,
            marxPassageRule: MarxPassageRule,
            rockPaperScissorsRule: RockPaperScissorsRule,
            scoreboardRule: ScoreboardRule,
            soundboardRule: SoundboardRule,
            timeoutRule: TimeoutRule,
            coronaRule: CoronaRule
        ): Set<Rule> {
            return setOf(
                jojoMemeRule, leagueRule, marxPassageRule, rockPaperScissorsRule, scoreboardRule, soundboardRule,
                timeoutRule, coronaRule
            )
        }
    }
}