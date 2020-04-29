package com.tathagat.postgram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
class MessageActivity : AppCompatActivity() {
var userreference:DatabaseReference?=null
    var messageRecieverId:String?=null
    var messageRecieverName:String?=null
    var messageRecieverPic:String?=null
    var username: TextView?=null
    var user_image:CircleImageView?=null
    var user_lastseen:TextView?=null
    var mToolbar:Toolbar?=null
    var messageSenderId:String?=null
    var message_text:TextView?=null
    var send_message_btn:ImageView?=null
    var messagereference:DatabaseReference?=null
    var recyclerView:RecyclerView?=null
    var uid:String?=null
    var imagesstoragereference:StorageReference?=null
    var videostoragereference:StorageReference?=null
    var messageSenderName:String?=null
    var add_files_btn:ImageView?=null
    var image_link: Uri?=null
    var video_link:Uri?=null
    var apiService: APIService?=null
    var messageList=ArrayList<Messages>()
    var seenListener:ValueEventListener?=null
    var seenListener1:ValueEventListener?=null
    var reference:DatabaseReference?=null
    var reference1:DatabaseReference?=null
    var blockreference:DatabaseReference?=null
    var messageSection:RelativeLayout?=null
var senderstatus=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        messageRecieverId = intent.getStringExtra("reciever_id")
        apiService = Client()
            .getClient("https://fcm.googleapis.com/").create(APIService::class.java)
        userreference = FirebaseDatabase.getInstance().getReference("Users")

        mToolbar = findViewById(R.id.messageToolbar)
        recyclerView = findViewById(R.id.messageRecyclerView)
        var linearLayout = LinearLayoutManager(this)
        linearLayout?.stackFromEnd = true
        recyclerView?.layoutManager = linearLayout
        setSupportActionBar(mToolbar)
        messageSection=findViewById(R.id.messageSection)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setTitle("")
        uid = FirebaseAuth.getInstance().currentUser?.uid
        var layoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view = layoutInflater.inflate(R.layout.custom_chat_bar, null)
        supportActionBar?.setCustomView(view)
        username = findViewById(R.id.custom_bar_username)
        user_image = findViewById(R.id.custom_bar_userpic)
        user_lastseen = findViewById(R.id.custom_bar_lastseen)
        var userreference = FirebaseDatabase.getInstance().getReference("Users")
        messageSenderId = FirebaseAuth.getInstance().currentUser?.uid
        userreference?.child(messageSenderId)?.child("User Status")?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.exists())
                    senderstatus = p0?.child("status").value.toString()

}

        })

        userreference?.child(messageRecieverId)?.child("User Status")
            ?.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.exists()) {
                        var status = p0?.child("status").value.toString()
                        if (status.equals("online") || status.equals("is typing...")) {
                            user_lastseen?.text = status
                        } else {
                            var time = p0?.child("time").value.toString()
                            var date = p0?.child("date").value.toString()
                            var today = Date()
                            val format_date = SimpleDateFormat("MMM dd, yyyy")
                            val now = format_date.format(today)
                            if (now.equals(date))
                                user_lastseen?.text = "Last seen today at " + time
                            else
                                user_lastseen?.text = "Last seen " + time + " " + date
                        }
                    }
                }

            })
        blockreference=FirebaseDatabase.getInstance().getReference("Blocked")
        imagesstoragereference = FirebaseStorage.getInstance().getReference("Messages Images")
        videostoragereference = FirebaseStorage.getInstance().getReference("Messages Videos")
        userreference?.child(messageRecieverId)?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                messageRecieverName = p0?.child("username")?.value.toString()
                messageRecieverPic = p0?.child("profileimage")?.value.toString()
                Picasso.get().load(messageRecieverPic).placeholder(R.drawable.profile).into(user_image)
            }

        })
        user_image?.setOnClickListener {
            var intent = Intent(applicationContext, user_information::class.java)
            intent.putExtra("reciever_id", messageRecieverId)
            startActivity(intent)
        }
        messageSenderId = FirebaseAuth.getInstance().currentUser?.uid
        userreference.child(messageSenderId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                messageSenderName = p0?.child("username")?.value.toString()
            }

        })


        send_message_btn = findViewById(R.id.send_message_btn)
        add_files_btn = findViewById(R.id.add_files)
        add_files_btn?.setOnClickListener {
            var alertDialog = AlertDialog.Builder(this)
            var options = arrayOf("Send Image", "Send Video")
            alertDialog.setTitle("Select File")
            alertDialog.setItems(options, object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    when (p1) {
                        0 -> {
                            sendImages()

                        }
                        1 -> {
                            sendVideos()
                        }
                    }
                }

            })
            alertDialog.show()
        }
        message_text = findViewById(R.id.message_text)
        message_text?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.length > 0)
                    updateUserStatus("is typing...")
                else
                    updateUserStatus("online")

            }

        })
        messagereference = FirebaseDatabase.getInstance().getReference("Messages")
        messagereference!!.keepSynced(true)
        displayMessages()
        send_message_btn?.setOnClickListener {

            sendMessage()
        }
seenMessage()
        blockreference?.child(messageSenderId)?.child(messageRecieverId)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    messageSection!!.visibility = View.GONE
                    username?.text = messageRecieverName+"(Blocked)"
                }
                else

                {
                    username?.text = messageRecieverName
                    blockreference?.child(messageRecieverId)?.child(messageSenderId)?.addValueEventListener(object :ValueEventListener{
                        override fun onCancelled(p0: DatabaseError?) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if(p0.exists())
                                messageSection!!.visibility=View.GONE
                            else
                                messageSection!!.visibility=View.VISIBLE
                        }

                    })
                }
            }

        })
    }

    private fun currentUser(userid:String){
        var editor:SharedPreferences.Editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private fun sendVideos(){
        var gallery= Intent()
        gallery.type="video/mp4"
        gallery.action=Intent.ACTION_GET_CONTENT
        startActivityForResult(gallery,111)

    }
    private fun sendImages() {

        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this);
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            var result = CropImage . getActivityResult (data)
            if (resultCode == RESULT_OK) {
                image_link = result . getUri ()
                var progressDialog=ProgressDialog(this)
                progressDialog?.setTitle("Sending Image")
                progressDialog?.setCanceledOnTouchOutside(false)
                progressDialog?.show()
                var messageinfo=HashMap<String,Any>()
                val today = Date()
                val format_date = SimpleDateFormat("MMM dd, yyyy")
                val format_time = SimpleDateFormat("hh:mm a")
                val format = SimpleDateFormat("yyyyMMddHHmmss")
                val date = format_date.format(today)
                val time = format_time.format(today)
                val filename = format.format(today) + uid
                var filepath=imagesstoragereference?.child(filename)
                filepath?.putFile(image_link!!)?.addOnCompleteListener(object :OnCompleteListener<UploadTask.TaskSnapshot>{
                    override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                        if(p0.isSuccessful){
                            var imageLink=p0.result?.downloadUrl.toString()
                            messageinfo.put("date", date)
                            messageinfo.put("time",time)
                            messageinfo.put("message",imageLink)
                            messageinfo.put("from",messageSenderId!!)
                            messageinfo.put("to",messageRecieverId!!)
                            messageinfo.put("type","image")
                            messageinfo.put("message seen",false)
                      var ref= messagereference?.child(messageSenderId)?.child(messageRecieverId)?.push()
                              var messagesendedid=ref?.key.toString()
                            ref?.updateChildren(messageinfo)?.
                                addOnCompleteListener(object :OnCompleteListener<Void>{
                                    override fun onComplete(p0: Task<Void>) {
                                        if(p0.isSuccessful){
                                            messagereference?.child(messageRecieverId)?.child(messageSenderId)?.child(messagesendedid)?.updateChildren(messageinfo)
                                                ?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                                    override fun onComplete(p0: Task<Void>) {
                                                        if(p0.isSuccessful) {
                                                            var databasereference=messagereference
                                                            generateNotificaction(messageSenderId!!,messageRecieverId!!,messageSenderName!!,"Image")
                                                            databasereference?.child(messageSenderId)?.child(messageRecieverId)?.child("last sent")?.setValue(format.format(today))
                                                            databasereference?.child(messageRecieverId)?.child(messageSenderId)?.child("last sent")?.setValue(format.format(today))
                                                            progressDialog?.dismiss()

                                                        }
                                                    }

                                                })
                                        }
                                    }
                                })


                        }
                    }

                })?.addOnFailureListener(object:OnFailureListener{
                    override fun onFailure(p0: Exception) {
                        progressDialog.dismiss()
                        Toast.makeText(applicationContext,p0.message,Toast.LENGTH_SHORT).show()

                    }

                })?.addOnProgressListener(object :OnProgressListener<UploadTask.TaskSnapshot>{
                    override fun onProgress(p0: UploadTask.TaskSnapshot) {

                        var p=(100*(p0.bytesTransferred.toFloat()/p0.totalByteCount.toFloat())).toInt().toString()
                        progressDialog?.setMessage(p+"% Uploaded")
                    }

                })
                }}
        if(requestCode==111&& resultCode== Activity.RESULT_OK&&data!=null){
            video_link=data.data
            var progressDialog=ProgressDialog(this)
            progressDialog?.setTitle("Sending Video")
            progressDialog?.setCanceledOnTouchOutside(false)
            progressDialog?.show()
            var messageinfo=HashMap<String,Any>()
            val today = Date()
            val format_date = SimpleDateFormat("MMM dd, yyyy")
            val format_time = SimpleDateFormat("hh:mm a")
            val format = SimpleDateFormat("yyyyMMddHHmmss")
            val date = format_date.format(today)
            val time = format_time.format(today)
            val filename = format.format(today) + uid
            var filepath=videostoragereference?.child(filename)
            filepath?.putFile(video_link!!)?.addOnCompleteListener(object :OnCompleteListener<UploadTask.TaskSnapshot>{
                override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                    if(p0.isSuccessful){
                        var imageLink=p0.result?.downloadUrl.toString()
                        messageinfo.put("date", date)
                        messageinfo.put("time",time)
                        messageinfo.put("message",imageLink)
                        messageinfo.put("from",messageSenderId!!)
                        messageinfo.put("to",messageRecieverId!!)
                        messageinfo.put("type","video")
                        messageinfo.put("message seen",false)
                        messagereference?.child(messageSenderId)?.child(messageRecieverId)?.push()
                        var ref= messagereference?.child(messageSenderId)?.child(messageRecieverId)?.push()
                        var messagesendedid=ref?.key.toString()
                        ref?.updateChildren(messageinfo)?.
                            addOnCompleteListener(object :OnCompleteListener<Void>{
                                override fun onComplete(p0: Task<Void>) {
                                    if(p0.isSuccessful){
                                        messagereference?.child(messageRecieverId)?.child(messageSenderId)?.child(messagesendedid)?.updateChildren(messageinfo)
                                            ?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                                override fun onComplete(p0: Task<Void>) {
                                                    if(p0.isSuccessful) {
                                                        var databasereference=messagereference
                                                        generateNotificaction(messageSenderId!!,messageRecieverId!!,messageSenderName!!,"Video")
                                                        databasereference?.child(messageSenderId)?.child(messageRecieverId)?.child("last sent")?.setValue(format.format(today))
                                                        databasereference?.child(messageRecieverId)?.child(messageSenderId)?.child("last sent")?.setValue(format.format(today))

                                                        progressDialog?.dismiss()

                                                    }
                                                }

                                            })
                                    }
                                }
                            })


                    }
                }

            })?.addOnFailureListener(object:OnFailureListener{
                override fun onFailure(p0: Exception) {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext,p0.message,Toast.LENGTH_SHORT).show()

                }

            })?.addOnProgressListener(object :OnProgressListener<UploadTask.TaskSnapshot>{
                override fun onProgress(p0: UploadTask.TaskSnapshot) {
                    var p=(100*(p0.bytesTransferred.toFloat()/p0.totalByteCount.toFloat())).toInt().toString()
                    progressDialog?.setMessage(p+"% Uploaded")
                }

            }) }
    }
    private fun cancelCall() {

        userreference!!.child(messageSenderId).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild("Calling")) {
                    userreference!!.child(messageSenderId).child("Calling").removeValue()
                    userreference!!.child(messageRecieverId).child("Ringing").removeValue()
                }
                if(p0.hasChild("Ringing")){
                    userreference!!.child(messageSenderId).child("Ringing").removeValue()
                    userreference!!.child(messageSenderId).child("Calling").removeValue()
                }
            }})}
    private fun displayMessages() {
        messagereference?.child(messageSenderId)?.child(messageRecieverId)?.
                addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        messageList.clear()
                 if(p0!!.exists()) {

                     for (x in p0!!.children) {
                         if(!x.key.equals("last sent")) {
                             var from = x.child("from").value.toString()
                             var to= x.child("to").value.toString()
                             var messageid=x.key.toString()
                             var text = x.child("message").value.toString()
                             var type = x.child("type").value.toString()
                             var date = x.child("date").value.toString()
                             var time = x.child("time").value.toString()
                             var isseen = x.child("message seen").value.toString()
                             var story_id=x.child("story id").value.toString()
                             var story_position=x.child("story position").value.toString()
                             messageList.add(
                                 Messages(
                                     from,
                                     to,
                                     messageid,
                                     type,
                                     text,
                                     date,
                                     time,
                                     isseen,
                                     story_id,
                                     story_position
                                 )
                             )
                         }

                     }
                 }
                     var adapter= MessageAdapter(this@MessageActivity, messageList)
                     recyclerView?.adapter=adapter
                        recyclerView?.smoothScrollToPosition(adapter.itemCount)
                     adapter.notifyDataSetChanged()


                   }

                })

    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
      var item=menu.findItem(R.id.block_unblock)
        var item1=menu.findItem(R.id.call_user)
        blockreference?.child(messageSenderId)?.child(messageRecieverId)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(!p0.exists()) {
                    item.setTitle("Block")
                    item1.setEnabled(true)
                }
                else {
                    item.setTitle("Unblock")
                    item1.setEnabled(false)
                }
             }

        })
        blockreference?.child(messageRecieverId)?.child(messageSenderId)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(!p0.exists())
                    item1.setEnabled(true)
                else
                    item1.setEnabled(false)
            }

        })
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.messagechatmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId==android.R.id.home)
            finish()
        if(item?.itemId== R.id.call_user)
            callUser(messageRecieverId!!)
        if(item?.itemId==R.id.block_unblock){
            if(item.title.equals("Block"))
                blockUser()
            else
                unblockUser()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun blockUser() {
        blockreference?.child(messageSenderId)?.child(messageRecieverId)?.setValue("blocked")
    }
    private fun unblockUser() {
        blockreference?.child(messageSenderId)?.child(messageRecieverId)?.removeValue()    }


    fun callUser(user:String){
        userreference?.child(user)?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (!p0!!.hasChild("Calling") && !p0!!.hasChild("Ringing")) {
                    userreference?.child(messageSenderId)?.child("Calling")?.child("calling")?.setValue(user)
                        ?.addOnCompleteListener(object : OnCompleteListener<Void> {
                            override fun onComplete(p0: Task<Void>) {
                                if (p0.isSuccessful) {
                                    userreference?.child(user)?.child("Ringing")?.child("ringing")
                                        ?.setValue(messageSenderId)
                                    generateNotificactionVideoCall(messageSenderId!!,messageRecieverId!!,messageSenderName!!)
                                    var intent=Intent(this@MessageActivity, CallingActivity::class.java)
                                    intent.putExtra("reciever_id",user)
                                    startActivity(intent)

                                }
                            }

                        })
                }
                else
                    Toast.makeText(applicationContext,messageRecieverName+" is busy on another call or there is some issue.",Toast.LENGTH_SHORT)
                        .show()

            }

        })

    }
    var message:String?=null
    private fun sendMessage() {
        var text=message_text?.text.toString().trim()
        message_text?.text = ""
        if(TextUtils.isEmpty(text))
            Toast.makeText(applicationContext,"Please type some message...",Toast.LENGTH_SHORT).show()
        else
        {
            var messageinfo=HashMap<String,Any>()
            val today = Date()
            val format_date = SimpleDateFormat("MMM dd, yyyy")
            val format_time = SimpleDateFormat("hh:mm a")
            val format = SimpleDateFormat("yyyyMMddHHmmss")
            val date = format_date.format(today)
            val time = format_time.format(today)
            message=text
            messageinfo.put("date", date)
            messageinfo.put("time",time)
            messageinfo.put("message",text)
            messageinfo.put("from",messageSenderId!!)
            messageinfo.put("to",messageRecieverId!!)
            messageinfo.put("type","text")
            messageinfo.put("message seen",false)
            messagereference?.child(messageSenderId)?.child(messageRecieverId)?.push()
            messagereference?.child(messageSenderId)?.child(messageRecieverId)?.push()
            var ref= messagereference?.child(messageSenderId)?.child(messageRecieverId)?.push()
            var messagesendedid=ref?.key.toString()
                ref?.updateChildren(messageinfo)?.
                addOnCompleteListener(object :OnCompleteListener<Void>{
                    override fun onComplete(p0: Task<Void>) {
                        if(p0.isSuccessful){
                            messagereference?.child(messageRecieverId)?.child(messageSenderId)?.child(messagesendedid)?.updateChildren(messageinfo)
                                ?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                    override fun onComplete(p0: Task<Void>) {
                                        if(p0.isSuccessful) {
                                            var databasereference=messagereference
                                            generateNotificaction(messageSenderId!!,messageRecieverId!!,messageSenderName!!,message!!)
                                            databasereference?.child(messageSenderId)?.child(messageRecieverId)?.child("last sent")?.setValue(format.format(today))
                                            databasereference?.child(messageRecieverId)?.child(messageSenderId)?.child("last sent")?.setValue(format.format(today))
                                        }
                                        else{
                                            Toast.makeText(applicationContext,"Message delivery failed",Toast.LENGTH_SHORT).show()
                                        }
                                     }

                                })
                        }
                    }
                })

        }
    }

    override fun onStart() {
        super.onStart()
        cancelCall()

    }


    override fun onResume() {
        super.onResume()
        updateUserStatus("online")
        currentUser(messageRecieverId!!);
    }


    override fun onPause() {
        super.onPause()
        updateUserStatus("offline")
        currentUser("none");
reference!!.removeEventListener(seenListener)
        reference1!!.removeEventListener(seenListener1)
    }

    override fun onStop() {
        super.onStop()
        updateUserStatus("offline")
        currentUser("none");
    }

    override fun onDestroy() {
        super.onDestroy()
        updateUserStatus("offline")
        currentUser("none");
    }

    private fun updateUserStatus(status:String){
        val today = Date()
        val format_date = SimpleDateFormat("MMM dd, yyyy")
        val format_time = SimpleDateFormat("hh:mm a")
        val date = format_date.format(today)
        val time = format_time.format(today)
        var statusDetails=HashMap<String,Any>()
        statusDetails.put("date",date)
        statusDetails.put("time",time)
        statusDetails.put("status",status)
        userreference?.child(uid)?.child("User Status")?.updateChildren(statusDetails)
    }

    private fun seenMessage(){
        reference=messagereference?.child(messageRecieverId)?.child(messageSenderId)
           seenListener= reference?.addValueEventListener(
            object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0!!.exists()) {


                        for (x in p0!!.children) {
                            var from=x.child("from").value.toString()
                            if(from.equals(messageRecieverId))
                            {
                               x.ref.child("message seen").setValue(true)
                            }
                        }

                    }
                 }

            }
        )
        reference1= messagereference?.child(messageSenderId)?.child(messageRecieverId)
 seenListener1= reference1?.addValueEventListener(
            object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0!!.exists()) {
                        for (x in p0!!.children) {
                            var from=x.child("from").value.toString()
                            if(from.equals(messageRecieverId))
                            {
                                x.ref.child("message seen").setValue(true)
                            }
                        }

                    }
                }

            }
        )
    }
    fun generateNotificaction(senderId:String,recieverId:String, username:String, message:String) {
        var tokenreference = FirebaseDatabase.getInstance().getReference("Tokens").child(recieverId)
        tokenreference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                var token =p0!!.value.toString()
                var data= notification(
                    senderId,
                    R.drawable.logo,
                    username + ": " + message,
                    "New Message",
                    recieverId
                )
                var sender= Sender(data, token)
                apiService?.sendNotification(sender)?.enqueue(object : Callback<MyResponse> {
                    override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                        if (response.code() == 200){
                            if (response.body()?.success != 1){
                                Toast.makeText(applicationContext, "Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }  }

                })
            }

        })



    }
    fun generateNotificactionVideoCall(senderId:String,recieverId:String, username:String) {
        var tokenreference = FirebaseDatabase.getInstance().getReference("Tokens").child(recieverId)
        tokenreference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                var token =p0!!.value.toString()
                var data= notification(
                    senderId,
                    R.drawable.logo,
                    username + " is calling you.",
                    "Incoming Call",
                    recieverId
                )
                var sender= Sender(data, token)
                apiService?.sendNotification(sender)?.enqueue(object : Callback<MyResponse> {
                    override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                        if (response.code() == 200){
                            if (response.body()?.success != 1){
                                Toast.makeText(applicationContext, "Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }  }

                })
            }

        })



    }
}
