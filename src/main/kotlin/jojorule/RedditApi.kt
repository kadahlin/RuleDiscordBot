package jojorule

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