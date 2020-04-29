package com.tathagat.postgram

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MyFirebaseMessagingService: FirebaseMessagingService() {
    private val ADMIN_CHANNEL_ID = "admin_channel"
    var currentUserId=FirebaseAuth.getInstance().currentUser!!.uid
    var recieveNotification=true
    var userreference=FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("User Status")
    override fun onMessageReceived(p0: RemoteMessage?) {
        userreference?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
            if(p0!!.exists()){
                var status=p0.child("status").value.toString()

                if(status.equals("online"))
                    recieveNotification=false
                else
                    recieveNotification=true
            }
             }
        })

        var intent:Intent?=null
        val user = p0?.data?.get("user")
        if(p0?.data?.get("title").equals("New Message")) {
            intent = Intent(this, MessageActivity::class.java)
        intent.putExtra("reciever_id",user)
        }
        else if(p0?.data?.get("title").equals("Friend Request")) {
            intent = Intent(this, friends_request::class.java)


        }
        else{
            if(p0?.data?.get("title").equals("Incoming Call")){
                intent=Intent(this, CallingActivity::class.java)
                intent.putExtra("reciever_id",user)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            else
            intent = Intent(this, Groups::class.java)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt(3000)

        val preferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUser = preferences.getString("currentuser", "none")

        /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them.
      */var firebaseuser=FirebaseAuth.getInstance().currentUser
        if(firebaseuser!=null && firebaseuser?.uid.equals( p0?.data?.get("sented"))){
        if(!currentUser.equals(user)){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val largeIcon = BitmapFactory.decodeResource(
            resources,
            R.drawable.logo
        )

        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(largeIcon)
            .setContentTitle(p0?.data?.get("title"))
            .setContentText(p0?.data?.get("body"))
            .setAutoCancel(true)
            .setSound(notificationSoundUri)
            .setContentIntent(pendingIntent)

        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color = resources.getColor(R.color.colorPrimaryDark)
        }
            if(recieveNotification&&!p0?.data?.get("title").equals("Incoming Call"))
        notificationManager.notify(notificationID, notificationBuilder.build())
    }}}

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to devie notification"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }
}