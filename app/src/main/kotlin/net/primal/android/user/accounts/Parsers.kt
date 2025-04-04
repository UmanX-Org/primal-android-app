package net.primal.android.user.accounts

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.user.domain.Relay
import net.primal.core.utils.serialization.CommonJson
import timber.log.Timber

fun String.parseKind3Relays(): List<Relay> {
    val jsonContent = try {
        CommonJson.parseToJsonElement(this)
    } catch (error: SerializationException) {
        Timber.w(error)
        null
    }

    val relays = mutableListOf<Relay>()
    jsonContent?.jsonObject?.entries?.forEach {
        val relayUrl = it.key
        val permissions = it.value.jsonObject
        val read = permissions["read"]?.jsonPrimitive?.boolean ?: false
        val write = permissions["write"]?.jsonPrimitive?.boolean ?: false
        relays.add(Relay(url = relayUrl, read = read, write = write))
    }

    return relays
}

fun List<JsonArray>.parseNip65Relays(): List<Relay> {
    return this.filter { it.firstOrNull()?.jsonPrimitive?.content == "r" }
        .mapNotNull {
            it.getOrNull(1)?.jsonPrimitive?.content?.let { url ->
                val permission = it.getOrNull(2)?.jsonPrimitive?.content?.lowercase()
                Relay(
                    url = url,
                    read = permission == null || permission == "read",
                    write = permission == null || permission == "write",
                )
            }
        }
}

fun List<JsonArray>.parseFollowings(): Set<String> {
    val followings = mutableSetOf<String>()
    this.forEach {
        if (it.getOrNull(0)?.jsonPrimitive?.content == "p") {
            val pubkey = it.getOrNull(1)?.jsonPrimitive?.content
            if (pubkey != null) followings.add(pubkey)
        }
    }
    return followings
}

fun List<JsonArray>.parseInterests(): List<String> {
    val interests = mutableListOf<String>()
    this.forEach {
        if (it.getOrNull(0)?.jsonPrimitive?.content == "t") {
            val hashtag = it.getOrNull(1)?.jsonPrimitive?.content
            if (hashtag != null) interests.add(hashtag)
        }
    }
    return interests
}
