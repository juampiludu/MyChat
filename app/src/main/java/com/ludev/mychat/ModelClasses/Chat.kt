package com.ludev.mychat.ModelClasses

data class Chat(
    var sender: String,
    var message: String,
    var receiver: String,
    var seen: Boolean,
    var url: String,
    var messageId: String,
    var time: String,
    var showTime: String
) {

    constructor() : this("", "", "", false, "", "", "", "")

}