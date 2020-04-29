package com.tathagat.postgram
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import android.media.RingtoneManager
import android.media.Ringtone


class CallingActivity : AppCompatActivity() {
var recieverusername:TextView?=null
    var recieverpic:ImageView?=null
    var make_call:ImageView?=null
    var cancel_call:ImageView?=null
    var userreference:DatabaseReference?=null
    var senderid:String?=null
    var recieverid:String?=null
    var username:String?=null
    var apiService: APIService?=null
    var sendername:String?=null
    var mediaPlayer:MediaPlayer?=null
    var defaultRingtone:Ringtone?=null
    var callring:TextView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calling)
        recieverusername= findViewById(R.id.calling_username)
        recieverpic=findViewById(R.id.calling_profilepic)
        make_call=findViewById(R.id.make_call)
        cancel_call=findViewById(R.id.cancel_call)
        recieverid=intent.getStringExtra("reciever_id")
        apiService = Client()
            .getClient("https://fcm.googleapis.com/").create(APIService::class.java)
        senderid=FirebaseAuth.getInstance().currentUser!!.uid
        userreference=FirebaseDatabase.getInstance().getReference("Users")
        mediaPlayer=MediaPlayer.create(applicationContext, R.raw.ringing)
        mediaPlayer?.isLooping=true
        setProfileInfo(recieverid)
        val defaultRintoneUri =
            RingtoneManager.getActualDefaultRingtoneUri(applicationContext, RingtoneManager.TYPE_RINGTONE)
        defaultRingtone = RingtoneManager.getRingtone(applicationContext, defaultRintoneUri)
        userreference?.child(senderid)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }
            override fun onDataChange(p0: DataSnapshot?) {
           sendername=p0?.child("username")?.value?.toString()
            }

        })
        cancel_call?.setOnClickListener {
            cancelCall()
        }
        make_call?.setOnClickListener {
acceptCall()
        }
        callring=findViewById(R.id.callring)
    }
    private fun acceptCall() {
        defaultRingtone!!.stop()
        var callingPickUpMap=HashMap<String,Any>()
        callingPickUpMap.put("picked","picked")
        userreference?.child(senderid)?.child("Ringing")?.updateChildren(callingPickUpMap)?.addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful){
                    var intent=Intent(this@CallingActivity, VideoChatActivity::class.java)
                    intent.putExtra("reciever_id",recieverid)
                    startActivity(intent)
                }
            }

        })
    }


    private fun cancelCall() {

        userreference!!.child(senderid).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.hasChild("Calling")){
                    userreference!!.child(senderid).child("Calling").removeValue()
                    userreference!!.child(recieverid).child("Ringing").removeValue()
                }
                if(p0.hasChild("Ringing")){
                    userreference!!.child(senderid).child("Ringing").removeValue()
                    userreference!!.child(recieverid).child("Calling").removeValue()
                }

            }

        })

    }

    private fun setProfileInfo(recieverid: String?) {
        userreference?.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0?.child(recieverid)!!.exists()){
                    var userpic=p0?.child(recieverid)?.child("profileimage")?.value.toString()
                    username=p0?.child(recieverid)?.child("username")?.value.toString()
                    Picasso.get().load(userpic).placeholder(R.drawable.profile).into(recieverpic)
                    recieverusername?.text=username
                }
        }
    })
}

    override fun onStart() {
        super.onStart()
       userreference?.child(senderid)?.addValueEventListener(object :ValueEventListener{
           override fun onCancelled(p0: DatabaseError?) {

           }

           override fun onDataChange(p0: DataSnapshot) {
               if(!p0.hasChild("Calling")&&!p0.hasChild("Ringing"))
               { mediaPlayer!!.stop()
defaultRingtone!!.stop()
                   finish()
               }
          if(p0.hasChild("Ringing")&&!p0.child("Ringing").hasChild("picked")){

              make_call!!.visibility=View.VISIBLE
              cancel_call!!.visibility=View.VISIBLE
          defaultRingtone!!.play()
              mediaPlayer!!.stop()
              callring?.text="Ringing..."
          }
               else {
              make_call!!.visibility = View.GONE
              cancel_call!!.visibility=View.VISIBLE
              mediaPlayer!!.start()
              defaultRingtone!!.stop()
              callring?.text="Calling..."
          }

           }

       })
        userreference?.child(recieverid)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.child("Ringing").hasChild("picked")){
                    mediaPlayer!!.stop()
                    var intent=Intent(this@CallingActivity, VideoChatActivity::class.java)
                    intent.putExtra("reciever_id",recieverid)
                    startActivity(intent)
                }

            }

        })
    }
}