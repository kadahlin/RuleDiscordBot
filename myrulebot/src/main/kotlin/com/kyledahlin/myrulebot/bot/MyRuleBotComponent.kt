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
package com.kyledahlin.myrulebot.bot

import com.kyledahlin.myrulebot.bot.corona.CoronaRule
import com.kyledahlin.myrulebot.bot.jojorule.JojoMemeRule
import com.kyledahlin.myrulebot.bot.keyvalue.KeyValueRule
import com.kyledahlin.myrulebot.bot.keyvalue.KeyValueRuleStorage
import com.kyledahlin.myrulebot.bot.keyvalue.KeyValueRuleStorageImpl
import com.kyledahlin.myrulebot.bot.marxrule.MarxPassageRule
import com.kyledahlin.myrulebot.bot.reaction.ReactionRule
import com.kyledahlin.myrulebot.bot.reaction.ReactionStorage
import com.kyledahlin.myrulebot.bot.reaction.ReactionStorageImpl
import com.kyledahlin.myrulebot.bot.rockpaperscissorsrule.RockPaperScissorsRule
import com.kyledahlin.myrulebot.bot.timeoutrule.TimeoutRule
import com.kyledahlin.rulebot.bot.BotComponent
import com.kyledahlin.rulebot.bot.Rule
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import javax.inject.Scope

@Scope
@Retention
annotation class MyRuleBotScope

@Component(modules = [MyRuleBotModule::class, StorageModule::class], dependencies = [BotComponent::class])
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
            marxPassageRule: MarxPassageRule,
            rockPaperScissorsRule: RockPaperScissorsRule,
            timeoutRule: TimeoutRule,
            coronaRule: CoronaRule,
            reactionRule: ReactionRule,
            keyValueRule: KeyValueRule
        ): Set<Rule> {
            return setOf(
                jojoMemeRule, marxPassageRule, rockPaperScissorsRule,
                timeoutRule, coronaRule, reactionRule, keyValueRule
            )
        }
    }
}

@Module
internal abstract class StorageModule {
    @MyRuleBotScope
    @Binds
    abstract fun bindsReactionStorage(reactionStorageImpl: ReactionStorageImpl): ReactionStorage

    @MyRuleBotScope
    @Binds
    abstract fun bindsKeyValueStorage(keyValueRuleStorageImpl: KeyValueRuleStorageImpl): KeyValueRuleStorage
}