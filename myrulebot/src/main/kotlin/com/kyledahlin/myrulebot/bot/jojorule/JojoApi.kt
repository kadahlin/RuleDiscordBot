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
package com.kyledahlin.myrulebot.bot.jojorule

import com.kyledahlin.rulebot.bot.client
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

internal const val JOJO_REDDIT = "https://www.reddit.com/r/ShitPostcrusaders/top.json?sort=top&t=week"

open class JojoApi @Inject constructor() {
    internal open suspend fun getPosts(): List<RedditChild> {
        val redditResponse = client.get<RedditResponse>(
            JOJO_REDDIT
        ) {
            header(
                "User-Agent",
                "JoJoMeme"
            )    //see https://www.reddit.com/r/redditdev/comments/5w60r1/error_429_too_many_requests_i_havent_made_many/
        }
        return redditResponse.data.children   //TODO: figure out kotlinx list deserialization a bit better
    }
}

@Serializable
internal data class RedditResponse(
    val data: RedditData
)

@Serializable
internal data class RedditData(
    val children: List<RedditChild>
)

@Serializable
internal data class RedditChild(
    val kind: String,
    val data: RedditChildData
)

@Serializable
internal data class RedditChildData(
    val title: String,
    @SerialName("is_video") val isVideo: Boolean,
    val url: String,
    val id: String
)