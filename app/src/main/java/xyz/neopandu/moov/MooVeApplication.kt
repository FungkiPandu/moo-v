package xyz.neopandu.moov

import android.app.Application
import com.androidnetworking.AndroidNetworking

class MooVeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidNetworking.initialize(applicationContext)
    }
}