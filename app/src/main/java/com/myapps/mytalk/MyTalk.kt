package com.myapps.mytalk

import android.app.Application

class MyTalk : Application() {
    var service: ConnService? = null

    companion object {
        var Connected: Boolean = false
        var daeomenLaunched = false
        var Username: String = ""
    }
}
