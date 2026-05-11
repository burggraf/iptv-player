package com.iptvplayer.data.parser

import com.iptvplayer.domain.model.Channel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Xtream Codes API JSON parser.
 *
 * Parses auth responses, category lists, and stream lists
 * from the Xtream Codes REST API into domain models.
 */
class XtreamParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parseAuth(jsonString: String): XtreamAuthResponse {
        val root = json.parseToJsonElement(jsonString).jsonObject

        val userInfoObj = root["user_info"]?.jsonObject
        val userInfo = userInfoObj?.let { obj ->
            XtreamAuthResponse.UserInfo(
                auth = obj["auth"]?.jsonPrimitive?.boolean ?: false,
                username = obj["username"]?.jsonPrimitive?.content,
                status = obj["status"]?.jsonPrimitive?.content,
                expDate = obj["exp_date"]?.jsonPrimitive?.content,
                isTrial = obj["is_trial"]?.jsonPrimitive?.content,
                maxConnections = obj["max_connections"]?.jsonPrimitive?.content,
            )
        }

        val serverInfoObj = root["server_info"]?.jsonObject
        val serverInfo = serverInfoObj?.let { obj ->
            XtreamAuthResponse.ServerInfo(
                url = obj["url"]?.jsonPrimitive?.content,
                port = obj["port"]?.jsonPrimitive?.content,
                serverProtocol = obj["server_protocol"]?.jsonPrimitive?.content,
            )
        }

        return XtreamAuthResponse(userInfo = userInfo, serverInfo = serverInfo)
    }

    fun parseCategories(jsonString: String): List<XtreamCategory> {
        val array = json.parseToJsonElement(jsonString).jsonArray
        return array.mapNotNull { element ->
            val obj = element.jsonObject
            val id = obj["category_id"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val name = obj["category_name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            XtreamCategory(categoryId = id, categoryName = name)
        }
    }

    fun parseStreams(jsonString: String, playlistId: String, serverUrl: String): List<Channel> {
        val array = json.parseToJsonElement(jsonString).jsonArray
        return array.mapNotNull { element ->
            val obj = element.jsonObject

            val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val streamId = obj["stream_id"]?.jsonPrimitive?.int
            val num = obj["num"]?.jsonPrimitive?.int?.toString() ?: ""
            val logo = obj["stream_icon"]?.jsonPrimitive?.content
            val epgId = obj["epg_channel_id"]?.jsonPrimitive?.content
            val categoryId = obj["category_id"]?.jsonPrimitive?.content
            val tvArchive = obj["tv_archive"]?.jsonPrimitive?.int ?: 0

            val streamUrl = if (streamId != null) {
                "$serverUrl/live/$streamId.m3u8"
            } else {
                return@mapNotNull null
            }

            Channel(
                id = "xtream_${streamId}_$playlistId",
                playlistId = playlistId,
                number = num,
                name = name,
                logo = logo,
                group = categoryId,
                tvgId = epgId,
                streamUrl = streamUrl,
                catchupDays = tvArchive,
            )
        }
    }
}
