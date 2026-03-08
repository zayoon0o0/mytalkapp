package com.myapps.mytalk

import android.app.Application

class MyTalk : Application() {
    var service: ConnService? = null
    var onMessageReceived: ((ByteArray) -> Unit)? = null

    companion object {
        var onMessageReceived: ((ByteArray) -> Unit)? = null
        var Connected: Boolean = false
        var daeomenLaunched = false
        var Username: String = ""
    }

}
