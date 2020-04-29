package com.tathagat.postgram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class userinfo_friends : AppCompatActivity() {
var friendsreference:DatabaseReference?=null
    var userreference:DatabaseReference?=null
    var mRecyclerView:RecyclerView?=null
    var uid:String?=null
    var reciever_id:String?=null
    var mToolbar:androidx.appcompat.widget.Toolbar?=null
    var requestreference:DatabaseReference?=null
    var senderName:String?=null
    var apiService: APIService?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userinfo_friends)
        apiService = Client()
            .getClient("https://fcm.googleapis.com/").create(APIService::class.java)

    mRecyclerView=findViewById(R.id.userinfofriends_recyclerview)
        var linearLayout=LinearLayoutManager(this)
        mToolbar=findViewById(R.id.userinfofriends_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mRecyclerView?.layoutManager=linearLayout
         reciever_id=intent.getStringExtra("visitor_user_id")
        userreference=FirebaseDatabase.getInstance().getReference("Users")
        var reference=FirebaseDatabase.getInstance().getReference("Users")

        uid=FirebaseAuth.getInstance().currentUser?.uid
        reference.child(uid).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                senderName=p0?.child("username")?.value.toString() }

        })
        userreference?.child(reciever_id)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()) {

                    var userInfo = p0?.child("username").value.toString()

                    supportActionBar?.title = userInfo+"'s friends"
                }
            }

        })
        friendsreference=FirebaseDatabase.getInstance().getReference("Friends")
        requestreference=FirebaseDatabase.getInstance().getReference("Friends Requests")

        displayFriendList()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId==android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)

    }
    private fun displayFriendList(){
        val option = FirebaseRecyclerOptions.Builder<myFriends>()
            .setQuery(friendsreference?.child(reciever_id)!!, myFriends::class.java)
            .build()
        val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<myFriends, MyViewHolder>(option) {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView = LayoutInflater.from(applicationContext).inflate(R.layout.user_information_friends,parent,false)
                return MyViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: myFriends) {
                var userid=getRef(position).key.toString()
                friendsreference?.child(userid)?.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0!!.exists()) {
                            userreference?.child(userid)?.addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError?) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                                    if(p0!!.exists()){

                                        var username=p0?.child("username").value.toString()
                                        holder.username.text=username
                                        var link=p0?.child("profileimage").value.toString()
                                        Picasso.get().load(link).placeholder(R.drawable.profile).into(holder.profilepic)
                                       holder.profilepic.setOnClickListener {
                                           var intent= Intent(applicationContext,
                                               user_information::class.java)
                                           intent.putExtra("reciever_id",userid)
                                           startActivity(intent)
                                       }
                                        requestreference?.child(uid)?.child(userid)?.addValueEventListener(object :ValueEventListener{
                                            override fun onCancelled(p0: DatabaseError?) {

                                            }

                                            override fun onDataChange(p0: DataSnapshot?) {
                                                if (p0!!.exists()) {
                                                    var type = p0?.child("request_type").value.toString()
                                                    if (type.equals("sent")) {
                                                        holder.add_friend?.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                                                        holder.add_friend.text = "Cancel"
                                                        holder.add_friend.setOnClickListener {
                                                            declineRequest(uid!!, userid)
                                                        }
                                                    }
                                                    if (type.equals("recieved")) {
                                                        holder.add_friend?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
                                                        holder.add_friend.text = "Accept"
                                                        holder.add_friend.setOnClickListener {
                                                            acceptRequest(uid!!, userid)
                                                        }
                                                    }
                                                } else {
                                                    friendsreference?.child(uid)?.child(userid)
                                                        ?.addValueEventListener(object : ValueEventListener {
                                                            override fun onCancelled(p0: DatabaseError?) {

                                                            }

                                                            override fun onDataChange(p0: DataSnapshot?) {
                                                                if (p0!!.exists()) {
                                                                    holder.add_friend?.setBackgroundColor(resources.getColor(
                                                                        R.color.colorPrimaryDark
                                                                    ))

                                                                    holder.add_friend.text = "Friend"
                                                                } else {
                                                                    if (uid!!.equals(userid)) {
                                                                        holder.add_friend?.setBackgroundColor(resources.getColor(
                                                                            R.color.colorPrimaryDark
                                                                        ))
                                                                        holder.add_friend.text = "Edit"
                                                                        holder.add_friend.setOnClickListener{
                                                                            startActivity(Intent(this@userinfo_friends,
                                                                                EditProfile::class.java))

                                                                        }
                                                                    }
                                                                    else {
                                                                        holder.add_friend?.setBackgroundColor(resources.getColor(
                                                                            R.color.colorPrimaryDark
                                                                        ))
                                                                        holder.add_friend.text = "Add"
                                                                        holder.add_friend.setOnClickListener {
                                                                            sendrequest(uid!!, userid)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        })
                                                }
                                            }})

                                    }



                                }
                            })
                        }



                    }

                })

            }}
        firebaseRecyclerAdapter.notifyDataSetChanged()
        mRecyclerView?.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }
    private fun sendrequest(senderid:String,recieverid:String) {
        requestreference?.child(senderid)?.child(recieverid)?.child("request_type")?.setValue("sent")?.addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful){
                    requestreference!!.child(recieverid).child(senderid).child("request_type").setValue("recieved").addOnCompleteListener(object :OnCompleteListener<Void>{
                        override fun onComplete(p0: Task<Void>) {
                            if(p0.isSuccessful)
                            {   generateNotificaction(senderid!!,recieverid!!,senderName!!)
                                Toast.makeText(applicationContext,"Friend Request Sent",Toast.LENGTH_SHORT).show()
                        }}

                    })
                }
            }

        })
    }
    private fun declineRequest(senderid:String,recieverid:String) {
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
    private fun acceptRequest(senderid:String,recieverid:String) {
        val c = Calendar.getInstance().getTime()
        val df = SimpleDateFormat("dd-MMM-yyyy")
        var saveCurrentDate = df.format(c)
        friendsreference?.child(senderid)?.child(recieverid)?.child("date")?.setValue(saveCurrentDate)
            ?.addOnCompleteListener(object : OnCompleteListener<Void> {
                override fun onComplete(p0: Task<Void>) {
                    if(p0.isSuccessful){
                        friendsreference!!.child(recieverid).child(senderid).child("date")?.setValue(saveCurrentDate)
                            ?.addOnCompleteListener(object : OnCompleteListener<Void> {
                                override fun onComplete(p0: Task<Void>) {
                                    if(p0?.isSuccessful){
                                        requestreference?.child(senderid)?.child(recieverid)
                                            ?.removeValue()?.addOnCompleteListener(object : OnCompleteListener<Void> {
                                                override fun onComplete(p0: Task<Void>) {
                                                    if(p0?.isSuccessful){
                                                        requestreference!!.child(recieverid).child(senderid)
                                                            ?.removeValue()?.addOnCompleteListener(object :
                                                                OnCompleteListener<Void> {
                                                                override fun onComplete(p0: Task<Void>) {
                                                                    if(p0?.isSuccessful){
                                                                        Toast.makeText(applicationContext,"Friend Request Accepted",
                                                                            Toast.LENGTH_SHORT).show()


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

        var profilepic = itemView!!.findViewById<CircleImageView>(R.id.userinfofriends_profile_image)
        var add_friend=itemView!!.findViewById<Button>(R.id.userinfofriends_send_request)
        var username = itemView!!.findViewById<TextView>(R.id.userinfofriends_username)

    }
}
