package com.tathagat.postgram

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

class postgram: Application() {
    override fun onCreate() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        var builder= Picasso.Builder(this)
        builder.downloader(OkHttp3Downloader(this, Long.MAX_VALUE))
        var built=builder.build()
        built.setIndicatorsEnabled(false)
        built.isLoggingEnabled=false
        Picasso.setSingletonInstance(built)
        super.onCreate()
    }
}