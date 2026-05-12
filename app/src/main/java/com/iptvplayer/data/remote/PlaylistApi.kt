package com.iptvplayer.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url

class PlaylistApi(
    private val client: HttpClient
) {
    suspend fun fetchM3u(url: String): String =
        client.get(url).body()

    suspend fun xtreamAuthRaw(serverUrl: String, username: String, password: String): String =
        client.get {
            url("$serverUrl/player_api.php")
            parameter("username", username)
            parameter("password", password)
        }.body()

    suspend fun xtreamLiveStreams(
        serverUrl: String,
        username: String,
        password: String,
        categoryId: String? = null
    ): String {
        val params = mutableMapOf(
            "username" to username,
            "password" to password,
            "action" to "get_live_streams"
        )
        categoryId?.let { params["category_id"] = it }

        return client.get {
            url("$serverUrl/player_api.php")
            params.forEach { (k, v) -> parameter(k, v) }
        }.body()
    }

    suspend fun xtreamLiveCategories(
        serverUrl: String,
        username: String,
        password: String
    ): String =
        client.get {
            url("$serverUrl/player_api.php")
            parameter("username", username)
            parameter("password", password)
            parameter("action", "get_live_categories")
        }.body()
}
