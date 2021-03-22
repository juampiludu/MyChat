package com.ludev.mychat.ModelClasses

data class Users(
    val uid: String,
    var username: String,
    val email: String,
    var phone: String,
    var profile: String,
    var status: String,
    var search: String,
    var facebook: String,
    var instagram: String,
    var twitter: String,
    var name: String,
    ) {

    constructor() : this("", "", "", "", "", "", "", "", "", "", "")

}

