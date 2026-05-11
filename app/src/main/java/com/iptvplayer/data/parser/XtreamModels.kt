package com.iptvplayer.data.parser

/**
 * Xtream Codes API response models.
 */
data class XtreamAuthResponse(
    val userInfo: UserInfo?,
    val serverInfo: ServerInfo?,
) {
    val success: Boolean get() = userInfo?.auth == true

    data class UserInfo(
        val auth: Boolean,
        val username: String? = null,
        val status: String? = null,
        val expDate: String? = null,
        val isTrial: String? = null,
        val maxConnections: String? = null,
    )

    data class ServerInfo(
        val url: String? = null,
        val port: String? = null,
        val serverProtocol: String? = null,
    )
}

data class XtreamCategory(
    val categoryId: String,
    val categoryName: String,
)

data class XtreamStream(
    val num: Int,
    val name: String,
    val streamType: String? = null,
    val streamId: Int? = null,
    val streamIcon: String? = null,
    val tvArchive: Int = 0,
    val epgChannelId: String? = null,
    val categoryId: String? = null,
)
