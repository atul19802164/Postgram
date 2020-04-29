package com.tathagat.postgram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class user_information : AppCompatActivity() {
var profilepic:CircleImageView?=null
    var username:TextView?=null
    var friends_count:TextView?=null
    var posts_count:TextView?=null
    var userreference:DatabaseReference?=null
    var firebaseauth: FirebaseAuth?=null
    var firebaseuser:FirebaseUser?=null
    var requestreference:DatabaseReference?=null
    var friendsreference:DatabaseReference?=null
    var postsreference:DatabaseReference?=null
    var mToolbar:Toolbar?=null
    var senderid:String?=null
    var recieverid:String?=null
    var sendFriendRequest:Button?=null
    var friend:TextView?=null
    var posts:TextView?=null
    var status:TextView?=null
    var query_countposts:Query?=null
    var mRecyclerView:RecyclerView?=null
    lateinit var totalposts:String
    var senderName:String?=null
    var apiService: APIService?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_information)
        apiService = Client()
            .getClient("https://fcm.googleapis.com/").create(APIService::class.java)
        username=findViewById(R.id.userinfo_name)
        profilepic=findViewById(R.id.userinfo_profile_image)
        mToolbar=findViewById(R.id.user_info_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        posts_count=findViewById(R.id.post_counter)
        friends_count=findViewById(R.id.friend_counter)
        sendFriendRequest=findViewById(R.id.send_friend_request_userinfo)
        friend=findViewById(R.id.friend_count)
        posts=findViewById(R.id.post_count)
        firebaseuser=FirebaseAuth.getInstance().currentUser
        senderid=firebaseuser?.uid
        status=findViewById(R.id.userinfo_status)
        recieverid=intent.getStringExtra("reciever_id")
        mRecyclerView=findViewById(R.id.postpicsRecyclerView)
        var gridLayoutManager=GridLayoutManager(this,3)
        mRecyclerView?.layoutManager=gridLayoutManager
        userreference=FirebaseDatabase.getInstance().getReference("Users")
        var reference=FirebaseDatabase.getInstance().getReference("Users")
        reference.child(senderid).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                senderName=p0?.child("username")?.value.toString() }

        })
        friendsreference=FirebaseDatabase.getInstance().getReference("Friends")
        requestreference=FirebaseDatabase.getInstance().getReference("Friends Requests")
        postsreference=FirebaseDatabase.getInstance().getReference("Posts")
        query_countposts=postsreference?.orderByChild("uid")?.equalTo(recieverid)
        query_countposts?.addListenerForSingleValueEvent(valueEventListener)
        userreference?.child(recieverid)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()){
                    var name=p0?.child("fullname").value.toString()
                    var profileimage=p0?.child("profileimage").value.toString()
                    username?.text=name
                    Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(profilepic)
                    var userInfo=p0?.child("username").value.toString()
                    var Status=p0?.child("status").value.toString()
                    status?.text=Status
                    posts_count?.text=totalposts
                    supportActionBar?.title=userInfo
                    posts?.visibility=View.VISIBLE
                    friend?.visibility=View.VISIBLE
                }
            }

        })
        friendsreference?.child(recieverid)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                friends_count?.text=p0?.childrenCount.toString()  }

        })
friends_count?.setOnClickListener {
    var intent= Intent(applicationContext, userinfo_friends::class.java)
    intent.putExtra("visitor_user_id",recieverid)
    startActivity(intent)
}
        profilepic?.setOnClickListener {
            var intent= Intent(applicationContext, ProfileActivity::class.java)
            intent.putExtra("visitor_user_id",recieverid)
            startActivity(intent)
        }

        maintainButtons()
        displayPosts()
    }
private fun displayPosts(){
    val option = FirebaseRecyclerOptions.Builder<myFriends>()
        .setQuery(query_countposts!!, myFriends::class.java)
        .build()
    val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<myFriends, MyViewHolder>(option) {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView =
                LayoutInflater.from(applicationContext).inflate(R.layout.postimage_layout, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: myFriends) {
     var post_id=getRef(position).key.toString()
            postsreference?.child(post_id)?.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0!!.exists()){
                        var postimage=p0?.child("postimage").value.toString()
                        Picasso.get().load(postimage).into(holder.postpic)
                        holder.itemView.setOnClickListener {
var intent=Intent(this@user_information, UserPostActivity::class.java)
                            intent.putExtra("post_id",post_id)
                            startActivity(intent)

                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {

                }
            })


        }
    }
    firebaseRecyclerAdapter.notifyDataSetChanged()
    mRecyclerView?.adapter = firebaseRecyclerAdapter
    firebaseRecyclerAdapter.startListening()
}

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId==android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
}
    private fun maintainButtons() {
        requestreference?.child(senderid)?.child(recieverid)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.exists()) {
                    var type = p0?.child("request_type").value.toString()
                    if (type.equals("sent")) {
                        sendFriendRequest?.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                        sendFriendRequest?.visibility = View.VISIBLE
                        sendFriendRequest?.text = "Decline Friend Request"
                        sendFriendRequest?.setOnClickListener {
                            declineRequest()
                        }

                    }
                    if (type.equals("recieved")) {
                        sendFriendRequest?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
                        sendFriendRequest?.visibility = View.VISIBLE
                        sendFriendRequest?.text = "Accept Friend Request"
                        sendFriendRequest?.setOnClickListener {
                            acceptRequest()
                        }
                    }
                } else {
                    friendsreference?.child(senderid)?.child(recieverid)
                        ?.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError?) {

                            }

                            override fun onDataChange(p0: DataSnapshot?) {
                                if (p0!!.exists()) {
                                    sendFriendRequest?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
                                    sendFriendRequest?.text = "Friend"
                                    sendFriendRequest?.visibility = View.VISIBLE
                                } else {
                                    if (!senderid.equals(recieverid)) {
                                        sendFriendRequest?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
                                        sendFriendRequest?.visibility = View.VISIBLE
                                        sendFriendRequest?.text = "Send Friend Request"
                                        sendFriendRequest?.setOnClickListener {
                                            sendrequest()
                                        }

                                    } else {
                                        sendFriendRequest?.text="Edit Profile"
                                        sendFriendRequest?.visibility=View.VISIBLE
                                        sendFriendRequest?.setOnClickListener{
                                            startActivity(Intent(this@user_information,
                                                EditProfile::class.java))
                                        }
                                    }
                                }
                            }

                        })

                }
            }
})

}
var valueEventListener=object :ValueEventListener{
    override fun onCancelled(p0: DatabaseError?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDataChange(p0: DataSnapshot?) {
  totalposts=p0?.childrenCount.toString()
    }

}
    private fun acceptRequest() {
        val c = Calendar.getInstance().getTime()
        val df = SimpleDateFormat("dd-MMM-yyyy")
        var saveCurrentDate = df.format(c)
        friendsreference?.child(senderid)?.child(recieverid)?.child("date")?.setValue(saveCurrentDate)
            ?.addOnCompleteListener(object :OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    if(p0.isSuccessful){
                        friendsreference!!.child(recieverid).child(senderid).child("date")?.setValue(saveCurrentDate)
                            ?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                override fun onComplete(p0: Task<Void>) {
                                    if(p0?.isSuccessful){
                                        requestreference?.child(senderid)?.child(recieverid)
                                            ?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                                override fun onComplete(p0: Task<Void>) {
                                                    if(p0?.isSuccessful){
                                                        requestreference!!.child(recieverid).child(senderid)
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

    private fun declineRequest() {
     requestreference?.child(senderid)?.child(recieverid)?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
         override fun onComplete(p0: Task<Void>) {
             if(p0.isSuccessful){
                 requestreference!!.child(recieverid).child(senderid)?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
                     override fun onComplete(p0: Task<Void>) {
                         if(p0.isSuccessful)
                             Toast.makeText(applicationContext,"Friend Request Declined",Toast.LENGTH_SHORT).show()                     }

                 })
             }
         }

     })
    }

    private fun sendrequest() {
        requestreference?.child(senderid)?.child(recieverid)?.child("request_type")?.setValue("sent")?.addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful){
                    requestreference!!.child(recieverid).child(senderid).child("request_type").setValue("recieved").addOnCompleteListener(object :OnCompleteListener<Void>{
                        override fun onComplete(p0: Task<Void>) {
                            if(p0.isSuccessful){
                                generateNotificaction(senderid!!,recieverid!!,senderName!!)
                                Toast.makeText(applicationContext,"Friend Request Sent",Toast.LENGTH_SHORT).show()
                        }}

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
    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        var postpic = itemView!!.findViewById<ImageView>(R.id.postlayout_image)


    }
}
