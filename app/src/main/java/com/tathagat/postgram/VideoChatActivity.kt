package com.tathagat.postgram

import android.Manifest
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import pub.devrel.easypermissions.EasyPermissions
import android.opengl.GLSurfaceView
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.opentok.android.*
import pub.devrel.easypermissions.AfterPermissionGranted
class VideoChatActivity : AppCompatActivity(), Session.SessionListener,PublisherKit.PublisherListener {
    override fun onError(p0: PublisherKit?, p1: OpentokError?) {
        Toast.makeText(applicationContext,p1.toString(),Toast.LENGTH_SHORT).show()
        alertDialog.dismiss()
    }

    override fun onStreamCreated(p0: PublisherKit?, p1: Stream?) {
    }

    override fun onStreamDestroyed(p0: PublisherKit?, p1: Stream?) {
        }

    override fun onStreamDropped(p0: Session?, p1: Stream?) {
       if(mSubscriber!=null){
           mSubscriber=null
           mSubscriberViewController!!.removeAllViews()
           cancelCall()
       }
    }

    override fun onStreamReceived(p0: Session?, stream: Stream?) {
if(mSubscriber==null){
    mSubscriber=Subscriber.Builder(this,stream).build()
    mSession!!.subscribe(mSubscriber)
    mSubscriberViewController!!.addView(mSubscriber!!.view)
}
     }

    override fun onConnected(p0: Session?) {
mPublisher=Publisher.Builder(applicationContext).build()
        mPublisher!!.setPublisherListener(this@VideoChatActivity)
        mPublisherViewController!!.addView(mPublisher!!.view)
        if(mPublisher!!.view is GLSurfaceView){
            var view=mPublisher!!.view as GLSurfaceView
            view.setZOrderOnTop(true)
        }
        mSession!!.publish(mPublisher)
        alertDialog.dismiss()
    }

    override fun onDisconnected(p0: Session?) {
   }

    override fun onError(p0: Session?, p1: OpentokError?) {
Toast.makeText(applicationContext,p1.toString(),Toast.LENGTH_SHORT).show()
        alertDialog.dismiss()
    }

    var API_KEY = "46512942"
    var SESSION_ID = "2_MX40NjUxMjk0Mn5-MTU4NTA1NTY5ODA0NX4zbW96SVVSVU9xalh6ZjVlSWRFT3VGWlB-fg"
    var TOKEN = "T1==cGFydG5lcl9pZD00NjUxMjk0MiZzaWc9MzVmNDI1YTBhZTZhNjQ0ODk2M2ZkNThhMTU3MTJkZTZjYTQxYzc3NjpzZXNzaW9uX2lkPTJfTVg0ME5qVXhNamswTW41LU1UVTROVEExTlRZNU9EQTBOWDR6Ylc5NlNWVlNWVTl4YWxoNlpqVmxTV1JGVDNWR1dsQi1mZyZjcmVhdGVfdGltZT0xNTg1MDU1Nzg4Jm5vbmNlPTAuMTE0MDY0NjIwMTk4NjIzNTEmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU4NzY0Nzc4NyZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ=="
    var LOG_TAG = ""
    var closeVideoChatButton: ImageView? = null
    var userreference: DatabaseReference? = null
    var mPublisherViewController:FrameLayout?=null
    var mSubscriberViewController:FrameLayout?=null
    var mSession:Session?=null
    var mPublisher: Publisher?=null
    var mSubscriber:Subscriber?=null
    var senderid:String=""
    var recieverid:String=""
    var flip_camera:Button?=null
    var cancellcall=false
   lateinit var alertDialog:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat)
        LOG_TAG = VideoChatActivity::class.java.simpleName
        closeVideoChatButton = findViewById(R.id.cancel_call_video)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        userreference = FirebaseDatabase.getInstance().getReference("Users")
        closeVideoChatButton!!.setOnClickListener {
          cancelCall()
        }
        alertDialog=ProgressDialog(this@VideoChatActivity)
        alertDialog.setMessage("Please while we are connecting your call...")
        alertDialog.show()
        senderid=FirebaseAuth.getInstance().currentUser!!.uid
        recieverid=intent.getStringExtra("reciever_id")
        requestPermissions()
    }
    private fun cancelCall() {
        cancellcall=true;
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

        if(mSubscriber!=null)
            mSubscriber!!.destroy()
        if(mPublisher!=null)
            mPublisher!!.destroy()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    @AfterPermissionGranted(123)
    private fun requestPermissions() {
var perms= arrayOf(Manifest.permission.CAMERA,Manifest.permission.INTERNET,Manifest.permission.RECORD_AUDIO)
        if(EasyPermissions.hasPermissions(applicationContext,*perms)&&!cancellcall){
            userreference?.child(senderid)?.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.hasChild("Calling") || p0.hasChild("Ringing")){
                        mPublisherViewController=findViewById(R.id.publisher_container)
                        mSubscriberViewController=findViewById(R.id.subscriber_container)
                        mSession= Session(applicationContext,API_KEY,SESSION_ID)
                        mSession!!.setSessionListener(this@VideoChatActivity)
                        mSession!!.connect(TOKEN)
                        }
                }

            })
            }
        else{
            EasyPermissions.requestPermissions(this@VideoChatActivity,
                "Postgram needs access to your camera and mic to make video calls", 123,*perms)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        cancelCall()
    }

    override fun onStop() {
        super.onStop()
        cancelCall()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelCall()
    }

    override fun onPause() {
        super.onPause()
        cancelCall()
    }

    override fun onStart() {
        super.onStart()
        userreference?.child(senderid)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.hasChild("Calling") && !p0.hasChild("Ringing")){

                    finish()
            }
            }

        })
    }
}