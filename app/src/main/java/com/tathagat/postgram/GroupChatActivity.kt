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
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
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


class GroupChatActivity : AppCompatActivity() {
    var mToolbar:Toolbar?=null
    lateinit var groupid:String
    var groupreference:DatabaseReference?=null
    var groupchatreference:DatabaseReference?=null
    var send_message_btn: ImageView?=null
    var recyclerView:RecyclerView?=null
    var message_text:TextView?=null
    var uid:String?=null
    var add_files_btn:ImageView?=null
    var messageSenderName:String?=null
    var image_link: Uri?=null
    lateinit var group_name:String
    var userreference:DatabaseReference?=null
    var apiService: APIService?=null
    var imagesstoragereference:StorageReference?=null
    var messageList=ArrayList<Messages>()
    var recieversId=ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)
        mToolbar=findViewById(R.id.groupChatToolbar)
        apiService = Client()
            .getClient("https://fcm.googleapis.com/").create(APIService::class.java)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setTitle("")
        recyclerView=findViewById(R.id.groupchatRecyclerView)
        var linearLayout= LinearLayoutManager(this)
        linearLayout?.stackFromEnd=true
        recyclerView?.layoutManager=linearLayout
        imagesstoragereference= FirebaseStorage.getInstance().getReference("Messages Images")
        userreference=FirebaseDatabase.getInstance().getReference("Users")
        send_message_btn=findViewById(R.id.send_message_btn_group)
        uid=FirebaseAuth.getInstance().currentUser?.uid
        var layoutInflater=this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view=layoutInflater.inflate(R.layout.custom_chat_bar,null)
        supportActionBar?.setCustomView(view)
        var groupname=findViewById<TextView>(R.id.custom_bar_username)
        var groupimage=findViewById<CircleImageView>(R.id.custom_bar_userpic)
        message_text=findViewById(R.id.message_text_group)
      groupid=intent.getStringExtra("group_id")
        mToolbar?.setOnClickListener {
            var intent=Intent(this, GroupMembers::class.java)
            intent.putExtra("group_id",groupid)
            startActivity(intent)
        }
        var userreference=FirebaseDatabase.getInstance().getReference("Users")
        groupreference= FirebaseDatabase.getInstance().getReference("Groups").child(groupid)
        groupreference?.child("Group Members")?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
        for(id in p0!!.children)
            if(id.key.toString()!=uid)
            recieversId.add(id.key.toString())
            }

        })
        groupchatreference= FirebaseDatabase.getInstance().getReference("Group Messages").child(groupid)
        groupreference?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                group_name=p0?.child("Group Name")?.value.toString()
                var group_image=p0?.child("Group Image")?.value.toString()
                groupname?.text=group_name
                Picasso.get().load(group_image).placeholder(R.drawable.group_icon).into(groupimage)
            }

        })
        userreference.child(uid).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                messageSenderName=p0?.child("username")?.value.toString() }

        })
        send_message_btn=findViewById(R.id.send_message_btn_group)
        add_files_btn=findViewById(R.id.add_files_group)
        add_files_btn?.setOnClickListener {
            var alertDialog=AlertDialog.Builder(this)
            var options= arrayOf("Send Image")
            alertDialog.setTitle("Select File")
            alertDialog.setItems(options,object :DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    when(p1){
                        0->{
                            sendImages()

                        }

                    }
                }

            })
            alertDialog.show()
        }
        send_message_btn?.setOnClickListener {

            sendMessage()
        }
        displayMessages()
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
                image_link = result.getUri()
                var progressDialog = ProgressDialog(this)
                progressDialog?.setTitle("Sending Image")
                progressDialog?.setCanceledOnTouchOutside(false)
                progressDialog?.show()
                var messageinfo = HashMap<String, Any>()
                val today = Date()
                val format_date = SimpleDateFormat("MMM dd, yyyy")
                val format_time = SimpleDateFormat("hh:mm a")
                val format = SimpleDateFormat("yyyyMMddHHmmss")
                val date = format_date.format(today)
                val time = format_time.format(today)
                val filename = format.format(today) + uid
                var filepath = imagesstoragereference?.child(filename)
                filepath?.putFile(image_link!!)
                    ?.addOnCompleteListener(object : OnCompleteListener<UploadTask.TaskSnapshot> {
                        override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                            if (p0.isSuccessful) {
                                var imageLink = p0.result?.downloadUrl.toString()
                                messageinfo.put("date", date)
                                messageinfo.put("time", time)
                                messageinfo.put("message", imageLink)
                                messageinfo.put("from", uid!!)
                                messageinfo.put("type", "image")
                                var ref = groupchatreference?.push()
                                ref?.updateChildren(messageinfo)
                                    ?.addOnCompleteListener(object : OnCompleteListener<Void> {
                                        override fun onComplete(p0: Task<Void>) {
                                            if (p0.isSuccessful) {
                                                progressDialog.dismiss()
                                                groupreference?.child("last sent time:")?.setValue((format.format(today)))
                                                groupreference?.child("last sent by:")?.setValue(uid!!)
                                                groupreference?.child("last sent message:")?.setValue("Image")
                                                for(recieverId in recieversId){
                                                    generateNotificaction(uid!!,recieverId,messageSenderName!!,"Image")
                                                }


                                            }
                        }})

                    }}})?.addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(p0: Exception) {
                        progressDialog.dismiss()
                        Toast.makeText(applicationContext, p0.message, Toast.LENGTH_SHORT).show()

                    }

                })?.addOnProgressListener(object : OnProgressListener<UploadTask.TaskSnapshot> {
                    override fun onProgress(p0: UploadTask.TaskSnapshot) {

                        var p = (100 * (p0.bytesTransferred / p0.totalByteCount)).toString()
                        progressDialog?.setMessage(p + "% Uploaded")
                    }

                })
            }}
        if(requestCode==111&& resultCode== Activity.RESULT_OK&&data!=null){
            var icon_link=data.data
            var progressDialog=ProgressDialog(this)
            progressDialog?.setTitle("Changing Icon")
            progressDialog?.setMessage("Please wait while we are changing group icon...")
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
            filepath?.putFile(icon_link!!)?.addOnCompleteListener(object :OnCompleteListener<UploadTask.TaskSnapshot>{
                override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                    var imageLink=p0.result?.downloadUrl.toString()
                    groupreference?.child("Group Image")?.setValue(imageLink)?.addOnCompleteListener(object :OnCompleteListener<Void>{
                        override fun onComplete(p0: Task<Void>) {
                            if(p0.isSuccessful) {
                                Toast.makeText(applicationContext,"Group Icon Changed",Toast.LENGTH_SHORT).show()
                                progressDialog.dismiss()
                            }
                        }




















                    })
                }

            })
    }}
                private fun sendMessage() {
        var text=message_text?.text.toString().trim()
        message_text?.text = ""
        if(TextUtils.isEmpty(text))
            Toast.makeText(applicationContext,"Please type some message...",Toast.LENGTH_SHORT).show()
        else {
            var messageinfo = HashMap<String, Any>()
            val today = Date()
            val format_date = SimpleDateFormat("MMM dd, yyyy")
            val format_time = SimpleDateFormat("hh:mm a")
            val format = SimpleDateFormat("yyyyMMddHHmmss")
            val date = format_date.format(today)
            val time = format_time.format(today)
            messageinfo.put("date", date)
            messageinfo.put("time", time)
            messageinfo.put("message", text)
            messageinfo.put("from", uid!!)
            messageinfo.put("type", "text")
            groupchatreference?.push()?.updateChildren(messageinfo)?.addOnCompleteListener(object :OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    if(p0?.isSuccessful){
                       groupreference?.child("last sent time:")?.setValue((format.format(today)))
                        groupreference?.child("last sent by:")?.setValue(uid!!)
                        groupreference?.child("last sent message:")?.setValue(text)
                        for(recieverId in recieversId){
                            generateNotificaction(uid!!,recieverId,messageSenderName!!,text)
                        }


                    }
                }

            })
        }
    }
    private fun currentUser(userid:String){
        var editor: SharedPreferences.Editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    override fun onResume() {
        super.onResume()
        updateUserStatus("online")
        for(messageRecieverId in recieversId)
        currentUser(messageRecieverId!!);
    }


    override fun onPause() {
        super.onPause()
        updateUserStatus("offline")
        for(messageRecieverId in recieversId)
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
                    group_name,
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

                            }
                        }  }

                })
            }

        })



    }
    private fun displayMessages() {
   groupchatreference?.
            addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    messageList.clear()
                    if(p0!!.exists()) {

                        for (x in p0!!.children) {
                            if(!x.key.equals("last sent")) {
                                var from = x.child("from").value.toString()
                                var messageid=x.key.toString()
                                var text = x.child("message").value.toString()
                                var type = x.child("type").value.toString()
                                var date = x.child("date").value.toString()
                                var time = x.child("time").value.toString()
                                messageList.add(
                                    Messages(
                                        from,
                                        groupid,
                                        messageid,
                                        type,
                                        text,
                                        date,
                                        time,
                                        "",
                                        "",
                                        ""
                                    )
                                )
                            }

                        }
                    }
                    var adapter= GroupMessageAdapter(this@GroupChatActivity, messageList)
                    recyclerView?.adapter=adapter
                    recyclerView?.smoothScrollToPosition(adapter.itemCount)
                    adapter.notifyDataSetChanged()


                }

            })

    }

    override fun onCreateOptionsMenu(menu: Menu):Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.groupchatmenu, menu);
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem):Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml
        if(item?.itemId==android.R.id.home)
            finish()
        //noinspection SimplifiableIfStatement
        if (item.itemId== R.id.add_member) {
var intent= Intent(applicationContext, AddMember::class.java)
            intent.putExtra("group_id",groupid)
            startActivity(intent)
        }
        if(item.itemId== R.id.change_name)
            changeGroupName()
        if(item.itemId== R.id.exit_group){
            var alertDialog=AlertDialog.Builder(this)
            alertDialog.setMessage("Do you want to exit this group?")
            alertDialog.setPositiveButton("Yes",object :DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    exitGroup()
                }

            })
            alertDialog.setNegativeButton("No",object :DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {

                 }

            })
            alertDialog.show()
        }
        if(item.itemId== R.id.change_icon){
            var alertDialog = AlertDialog.Builder(this)
                var options = arrayOf("Change Icon", "Remove Icon")
                alertDialog.setItems(options, object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        when (p1) {
                            0 -> {

                                changeGroupIcon()
                            }
                            1 -> {
                                removeIcon()
                            }
                        }
                    }

                })
                alertDialog.show()

            }
if(item.itemId== R.id.group_member){
    var intent=Intent(this, GroupMembers::class.java)
    intent.putExtra("group_id",groupid)
    startActivity(intent)
}

        return super.onOptionsItemSelected(item);
    }

    private fun removeIcon() {
        groupreference?.child("Group Image")?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful) {
                    Toast.makeText(applicationContext,"Group Icon Removed",Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun changeGroupIcon() {
        var gallery= Intent()
        gallery.type="image/*"
        gallery.action=Intent.ACTION_GET_CONTENT
        startActivityForResult(gallery,111)

    }

    private fun exitGroup(){
var dialog=ProgressDialog(this)
    dialog.setMessage("Please wait...")
    dialog.setCanceledOnTouchOutside(false)
    dialog.show()
    groupreference?.child("Group Members")?.child(uid)?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
        override fun onComplete(p0: Task<Void>) {
            if(p0.isSuccessful){
                var groupdetails=FirebaseDatabase.getInstance().getReference("Groups Details").child(uid).child(groupid)
                groupdetails?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
                    override fun onComplete(p0: Task<Void>) {
                        if(p0.isSuccessful){
                            dialog.dismiss()
                            finish()
                        }
                        else
                            dialog.dismiss()
                    }

                })
            }
            else
                dialog.dismiss()
        }

    })
}
    private fun changeGroupName() {
        var dialog=AlertDialog.Builder(this)
        var edittext=EditText(this)
        edittext.setText(group_name)
        dialog.setTitle("Edit group name")
        dialog.setView(edittext)
        dialog.setPositiveButton("Change",object :DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                var group_name=edittext.text.toString()
                groupreference?.child("Group Name")?.setValue(group_name)?.addOnCompleteListener(object :OnCompleteListener<Void>{
                    override fun onComplete(p0: Task<Void>) {
                        if(p0.isSuccessful)
                            Toast.makeText(applicationContext,"Group name changed",Toast.LENGTH_SHORT).show()
                    }

                })

            }

        })
        dialog.setNegativeButton("Cancel",object:DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {

            }

        } )
        dialog.show()

    }

}
