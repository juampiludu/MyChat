package com.ludev.mychat.Notifications

data class Data(
    var user: String,
    var icon: Int,
    var body: String,
    var title: String,
    var sent: String
) {

    constructor() : this("", 0, "", "", "")

}