package com.iptvplayer.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url

class EpgApi(
    private val client: HttpClient
) {
    suspend fun fetchXmlTv(url: String): String =
        client.get(url).body()

    suspend fun fetchXtreamEpg(serverUrl: String, username: String, password: String): String =
        client.get {
            url("$serverUrl/xmltv.php")
            parameter("username", username)
            parameter("password", password)
        }.body()
}
