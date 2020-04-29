package com.tathagat.postgram

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper {
    var CHANNEL_ID="atul"
    var CHANNEL_NAME="tathagat"
    var CHANNEL_DESP="atul tathagat"

public fun displayNotification(context: Context,title:String,body:String){
    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    val pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
        PendingIntent.FLAG_ONE_SHOT)
    val notificationBuilder = NotificationCompat.Builder(context,CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(body).setSmallIcon(R.drawable.logo).setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setSound(defaultSoundUri)
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(1 /* ID of notification */, notificationBuilder.build())

    }
}