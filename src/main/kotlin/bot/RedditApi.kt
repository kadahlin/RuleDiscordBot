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
package bot

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

internal const val JOJO_REDDIT = "https://www.reddit.com/r/ShitPostcrusaders/top.json?sort=top&t=month"

@Serializable
internal data class RedditResponse(
    val data: RedditData
)

@Serializable
internal data class RedditData(
    val children: ChildList
)

@Serializable
internal data class ChildList(
    val children: List<RedditChild>
) {
    @Serializer(ChildList::class)
    companion object : KSerializer<ChildList> {

        override val descriptor = StringDescriptor.withName("ChildList")

        override fun serialize(encoder: Encoder, obj: ChildList) {
            RedditChild.serializer().list.serialize(encoder, obj.children)
        }

        override fun deserialize(decoder: Decoder): ChildList {
            return ChildList(RedditChild.serializer().list.deserialize(decoder))
        }
    }
}

@Serializable
internal data class RedditChild(
    val data: RedditChildData
)

@Serializable
internal data class RedditChildData(
    val title: String,
    val is_video: Boolean,
    val url: String,
    val id: String
)