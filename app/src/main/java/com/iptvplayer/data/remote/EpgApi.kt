package com.iptvplayer.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class EpgApi(
    private val client: HttpClient
) {
    suspend fun fetchXmlTv(url: String): String =
        client.get(url).body()
}
