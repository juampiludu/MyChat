package com.ludev.mychat.ModelClasses

data class Chat(
    var sender: String,
    var message: String,
    var receiver: String,
    var seen: Boolean,
    var url: String,
    var messageId: String,
    var time: String,
    var timeInMillis: Long,
    var showTime: String,
    var category: Int
) {

    constructor() : this("", "", "", false, "", "", "", 0, "", 0)

}