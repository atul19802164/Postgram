package com.tathagat.postgram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class ProfileActivity : AppCompatActivity() {
    var username:TextView?=null
    var fullname: TextView?=null
    var profile_pic: CircleImageView?=null
    var status:TextView?=null
    var sendfriendrequestbtn:Button?=null
    var declinefriendrequestbtn:Button?=null
    var cancelfriendrequestbtn:Button?=null
    var databasereference:DatabaseReference?=null
    var friendrequestreference:DatabaseReference?=null
    var friendreference:DatabaseReference?=null
    var relativeLayout:RelativeLayout?=null
   lateinit var reciever_user_id:String
    lateinit var sender_user_id:String
    lateinit var saveCurrentDate:String
var senderName:String?=null
    var apiService: APIService?=null
     var firebaseuser:FirebaseUser?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        reciever_user_id=intent.getStringExtra("visitor_user_id")
        apiService = Client()
            .getClient("https://fcm.googleapis.com/").create(APIService::class.java)
           profile_pic=findViewById(R.id.person_profile_pic)
                fullname=findViewById(R.id.person_profile_name)
        relativeLayout=findViewById(R.id.profileRelativeLayout)
        firebaseuser=FirebaseAuth.getInstance().currentUser
        sender_user_id=firebaseuser?.uid!!
        sendfriendrequestbtn=findViewById(R.id.send_friend_request)
        declinefriendrequestbtn=findViewById(R.id.decline_friend_request)
        cancelfriendrequestbtn=findViewById(R.id.cancel_friend_request)
             username=findViewById(R.id.person_profile_username)
        status=findViewById(R.id.person_profile_status)
        var reference=FirebaseDatabase.getInstance().getReference("Users")
        reference.child(sender_user_id).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                senderName=p0?.child("username")?.value.toString() }

        })
        databasereference=FirebaseDatabase.getInstance().getReference("Users").child(reciever_user_id)
        friendreference=FirebaseDatabase.getInstance().getReference("Friends")
        databasereference?.addListenerForSingleValueEvent(valueEventListenener)
        declinefriendrequestbtn?.visibility=View.GONE
maintainButtons()
    }

     fun maintainButtons() {
        friendrequestreference=FirebaseDatabase.getInstance().getReference("Friends Requests")
        friendrequestreference?.child(sender_user_id)?.child(reciever_user_id)?.addValueEventListener(
            object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {

                    if(p0!!.exists()){
                    var status=p0?.child("request_type")?.value.toString()

                    if(status=="sent"){
                        declinefriendrequestbtn?.visibility=View.VISIBLE
                        declinefriendrequestbtn?.text="decline friend request"
                        sendfriendrequestbtn?.visibility=View.GONE
                        declinefriendrequestbtn?.setOnClickListener {
                     declineFriendRequest()
                        }
                    }

                        else{
                            if(status=="recieved"){
                                 sendfriendrequestbtn?.text="Accept Friend Request"
                                 sendfriendrequestbtn?.visibility=View.VISIBLE
                                declinefriendrequestbtn?.visibility=View.GONE
                                cancelfriendrequestbtn?.visibility=View.VISIBLE
                                sendfriendrequestbtn?.setOnClickListener{
                                    acceptFriendRequest()
                                }
                                cancelfriendrequestbtn?.setOnClickListener {
                                    declineFriendRequest()
                                }
                            }
                                        }}
                else{
                        friendreference?.child(sender_user_id)?.child(reciever_user_id)?.addValueEventListener(object:ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onDataChange(p0: DataSnapshot?) {


                 if(p0!!.exists()){

                     sendfriendrequestbtn?.visibility=View.GONE
                     cancelfriendrequestbtn?.visibility=View.GONE
                     declinefriendrequestbtn?.text="remove from friend list"
                     declinefriendrequestbtn?.visibility=View.VISIBLE
                     declinefriendrequestbtn?.setOnClickListener {
                         removeFromFriendList()
                     }
                 }
                                else{
                     if(sender_user_id.equals(reciever_user_id))
                         sendfriendrequestbtn?.visibility=View.GONE
                     else {
                         declinefriendrequestbtn?.visibility=View.GONE
                         cancelfriendrequestbtn?.visibility=View.GONE
                         sendfriendrequestbtn?.visibility = View.VISIBLE
                         sendfriendrequestbtn?.text = "send friend request"
                         sendfriendrequestbtn?.setOnClickListener {
                             sendFriendRequest()

                         }
                     }
                 }
                        }

                        })                    }
                }

            }
        )
    }

    private fun declineFriendRequest() {
        friendrequestreference?.child(sender_user_id)?.child(reciever_user_id)!!.removeValue()
            .addOnCompleteListener(object :OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    if(p0.isSuccessful){
                        friendrequestreference?.child(reciever_user_id)?.child(sender_user_id)!!
                            .removeValue().addOnCompleteListener(object :OnCompleteListener<Void>
                            {
                                override fun onComplete(p0: Task<Void>) {
                                    if(p0.isSuccessful){
                                      Toast.makeText(applicationContext,"Friend Request Declined",Toast.LENGTH_SHORT).show()
                                    }
                                }

                            }
                            )
                    }
                }

            })
    }

    private fun removeFromFriendList() {
friendreference?.child(sender_user_id)?.child(reciever_user_id)?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
    override fun onComplete(p0: Task<Void>) {
        if(p0?.isSuccessful){
            friendreference!!.child(reciever_user_id).child(sender_user_id).removeValue().addOnCompleteListener(object :
            OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    if(p0!!.isSuccessful)
                        Toast.makeText(applicationContext,"Removed from Friends List",Toast.LENGTH_SHORT).show()


                }

            })
        }
    }

})
    }

    private fun acceptFriendRequest() {
        val c = Calendar.getInstance().getTime()
        val df = SimpleDateFormat("dd-MMM-yyyy")
        saveCurrentDate = df.format(c)
        friendreference?.child(sender_user_id)?.child(reciever_user_id)?.child("date")?.setValue(saveCurrentDate)
            ?.addOnCompleteListener(object :OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    if(p0.isSuccessful){
                        friendreference!!.child(reciever_user_id).child(sender_user_id).child("date")?.setValue(saveCurrentDate)
                            ?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                override fun onComplete(p0: Task<Void>) {
                                    if(p0?.isSuccessful){
                                        friendrequestreference?.child(sender_user_id)?.child(reciever_user_id)
                                            ?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                                override fun onComplete(p0: Task<Void>) {
                                                    if(p0?.isSuccessful){
                                                        friendrequestreference!!.child(reciever_user_id).child(sender_user_id)
                                                            ?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                                                override fun onComplete(p0: Task<Void>) {
                                                                    if(p0?.isSuccessful){
                                                                        Toast.makeText(applicationContext,"Friend Request Accepted",Toast.LENGTH_SHORT).show()

                                                                    }
                                                                }

                                                            })
                                                    }
                                                }

                                            })
                                    }
                                }
                            })

                    }
                }

            })
    }

    var valueEventListenener= object :ValueEventListener{
        override fun onCancelled(p0: DatabaseError?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onDataChange(p0: DataSnapshot?) {
            if (p0!!.exists()) {
                var Fullname = p0?.child("fullname").value.toString()
                var Username = p0?.child("username").value.toString()
                var user_status = p0?.child("status").value.toString()
                var profilpic = p0?.child("profileimage").value.toString()
                fullname?.text=Fullname
                username?.text=Username
                status?.text=user_status
                Picasso.get().load(profilpic).placeholder(R.drawable.profile).into(profile_pic)
                profileRelativeLayout.visibility= View.VISIBLE

            }}
        }

    private fun sendFriendRequest() {
    friendrequestreference?.child(sender_user_id)?.child(reciever_user_id)?.
        child("request_type")?.setValue("sent")?.addOnCompleteListener(object :OnCompleteListener<Void>{
        override fun onComplete(p0: Task<Void>) {
            if(p0?.isSuccessful)
            { friendrequestreference?.child(reciever_user_id)?.child(sender_user_id)!!.child("request_type").setValue("recieved")
                    .addOnCompleteListener(object :OnCompleteListener<Void>{
                        override fun onComplete(p0: Task<Void>) {
if(p0?.isSuccessful)
{
    generateNotificaction(sender_user_id,reciever_user_id,senderName!!)

    Toast.makeText(applicationContext,"Friend Request Sent",Toast.LENGTH_SHORT).show()


}
                        }

                    })

            }
        }

    })
    }
    fun generateNotificaction(senderId:String,recieverId:String, username:String) {
        var tokenreference = FirebaseDatabase.getInstance().getReference("Tokens").child(recieverId)
        tokenreference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                var token =p0!!.value.toString()
                var data= notification(
                    senderId,
                    R.drawable.logo,
                    username + " has sent you a friend request.",
                    "Friend Request",
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

