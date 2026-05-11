package com.iptvplayer.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url

class EpgApi(
    private val client: HttpClient
) {
    suspend fun fetchXmlTv(url: String): String =
        client.get { url(url) }.bodyAsText()

    // Placeholder — real implementation in Phase 2
    @Suppress("unused")
    private suspend fun io.ktor.client.statement.HttpResponse.bodyAsText(): String =
        ""
}
