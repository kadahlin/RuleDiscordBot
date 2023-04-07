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

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.kyledahlin.destiny.Destiny2Rule
import com.kyledahlin.mtgrule.MtgRule
import com.kyledahlin.myrulebot.app.LocalAnalytics
import com.kyledahlin.myrulebot.bot.corona.CoronaRule
import com.kyledahlin.myrulebot.bot.jojorule.JojoMemeRule
import com.kyledahlin.myrulebot.bot.keyvalue.KeyValueRule
import com.kyledahlin.myrulebot.bot.keyvalue.KeyValueRuleStorage
import com.kyledahlin.myrulebot.bot.keyvalue.KeyValueRuleStorageImpl
import com.kyledahlin.myrulebot.bot.rockpaperscissorsrule.RockPaperScissorsRule
import com.kyledahlin.raidrule.RaidRule
import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.skryfall.SkryfallClient
import com.kyledahlin.wellnessrule.WellnessRule
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext
import javax.inject.Named
import javax.inject.Scope

@Scope
@Retention
annotation class MyRuleBotScope

@Component(modules = [MyRuleBotModule::class, StorageModule::class])
@MyRuleBotScope
interface MyRuleBotComponent {
    fun rules(): Set<Rule>
}

@Module
internal abstract class MyRuleBotModule {

    @Binds
    abstract fun providesAnalytics(local: LocalAnalytics): Analytics

    companion object {
        @JvmStatic
        @Provides
        @MyRuleBotScope
        @ElementsIntoSet
        fun providesRules(
            jojoMemeRule: JojoMemeRule,
            rockPaperScissorsRule: RockPaperScissorsRule,
            coronaRule: CoronaRule,
            keyValueRule: KeyValueRule,
            wellnessRule: WellnessRule,
            mtgRule: MtgRule,
            destinyRule: Destiny2Rule,
            raidRule: RaidRule
        ): Set<Rule> {
            return setOf(
                jojoMemeRule, rockPaperScissorsRule,
                coronaRule, keyValueRule, wellnessRule, mtgRule, destinyRule, raidRule
            )
        }

        @JvmStatic
        @Provides
        @MyRuleBotScope
        fun bindsFirestore(): Firestore = FirestoreClient.getFirestore()

        @JvmStatic
        @Provides
        @MyRuleBotScope
        fun bindsSkryfallClient(): SkryfallClient = SkryfallClient.createClient(logCalls = true)

        @JvmStatic
        @Provides
        @MyRuleBotScope
        @Named("storage")
        fun providesStorageDispatcher(): CoroutineDispatcher = newSingleThreadContext("storage")
    }
}

@Module
internal abstract class StorageModule {

    @MyRuleBotScope
    @Binds
    abstract fun bindsKeyValueStorage(keyValueRuleStorageImpl: KeyValueRuleStorageImpl): KeyValueRuleStorage
}