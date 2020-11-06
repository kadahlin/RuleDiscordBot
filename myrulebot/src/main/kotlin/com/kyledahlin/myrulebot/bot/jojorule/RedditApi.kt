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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal const val JOJO_REDDIT = "https://www.reddit.com/r/ShitPostcrusaders/top.json?sort=top&t=week"

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