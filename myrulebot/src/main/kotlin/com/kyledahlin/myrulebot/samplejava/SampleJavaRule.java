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
package com.kyledahlin.myrulebot.samplejava;

import com.kyledahlin.myrulebot.MyRuleBotScope;
import com.kyledahlin.rulebot.bot.MessageCreated;
import com.kyledahlin.rulebot.bot.RuleBotEvent;
import com.kyledahlin.rulebot.bot.java.JavaDiscordWrapper;
import com.kyledahlin.rulebot.bot.java.JavaEventStorage;
import com.kyledahlin.rulebot.bot.java.JavaLocalStorage;
import com.kyledahlin.rulebot.bot.java.JavaRule;
import discord4j.core.object.util.Snowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.Collections;

@MyRuleBotScope
public class SampleJavaRule extends JavaRule {

    private static final String PING = "ping";

    @Inject
    public SampleJavaRule(JavaLocalStorage javaLocalStorage, JavaEventStorage javaEventStorage) {
        super(javaLocalStorage, javaEventStorage, "SampleJava");
    }

    @Override
    public boolean blockHandleEvent(@NotNull RuleBotEvent event) {
        JavaDiscordWrapper wrapper = getEventStorage().getDiscordWrapperForEvent(event);
        if (wrapper != null && event instanceof MessageCreated && ((MessageCreated) event).getContent().toLowerCase().contains(PING)) {
            logDebug("sending pong");
            wrapper.sendMessage("PONG");
            return true;
        } else {
            logDebug("not sending pong");
            return false;
        }
    }

    @Nullable
    @Override
    public String getExplanation() {
        return "Type a message with the phrase " + PING + " to get a response";
    }

    @NotNull
    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }

    /**
     * Get an event that should trigger a response for this rule
     *
     * @return an event that will cause a message to be sent through a wrapper if present
     */
    static RuleBotEvent getValidTestEvent() {
        return new MessageCreated(Snowflake.of(1L), PING, Snowflake.of(1L), Collections.emptySet(), Collections.emptySet());
    }

    /**
     * Get an event that will not trigger a response for this rule
     *
     * @return an event that will not cause a message to be sent through a wrapper
     */
    static RuleBotEvent getInvalidTestEvent() {
        return new MessageCreated();
    }
}
