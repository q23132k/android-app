package me.echeung.moemoekyun.client.socket.response

open class NotificationResponse : BaseResponse() {
    val t: String? = null
    open val d: Details? = null

    open class Details {
        val type: String? = null
    }
}
