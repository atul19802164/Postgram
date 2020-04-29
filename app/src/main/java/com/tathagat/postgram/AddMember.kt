package com.tathagat.postgram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
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

class AddMember : AppCompatActivity() {
    var apiService: APIService? = null
    var mToolbar: Toolbar? = null
    var mRecyclerView: RecyclerView? = null
    var friendsreference: DatabaseReference? = null
    var groupreference: DatabaseReference? = null
    var userreference: DatabaseReference? = null
    var uid:String?=null
    var senderName:String?=null
    var groupid:String?=null
    var groupname:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_member)
        mToolbar = findViewById(R.id.addMember_toolbar)
        setSupportActionBar(mToolbar)
        apiService = Client()
            .getClient("https://fcm.googleapis.com/").create(APIService::class.java)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mRecyclerView = findViewById(R.id.addMember_recyclerview)
        var linearLayout = LinearLayoutManager(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mRecyclerView?.layoutManager = linearLayout
        supportActionBar?.title = "Add Member"
        uid = FirebaseAuth.getInstance().currentUser?.uid
        groupid = intent.getStringExtra("group_id")
        friendsreference = FirebaseDatabase.getInstance().getReference("Friends").child(uid)
        userreference = FirebaseDatabase.getInstance().getReference("Users")
        userreference?.child(uid)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                senderName=p0?.child("username")?.value.toString() }

        })
        groupreference = FirebaseDatabase.getInstance().getReference("Groups")?.child(groupid)?.child("Group Members")
        var ref=FirebaseDatabase.getInstance().getReference("Groups")?.child(groupid)
        ref!!.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
          if(p0!!.exists()){
              groupname=p0.child("Group Name").value.toString()
          }
            }

        })
        seeFriends()
    }

    private fun seeFriends() {
        val option = FirebaseRecyclerOptions.Builder<myFriends>()
            .setQuery(friendsreference!!, myFriends::class.java)
            .build()
        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<myFriends, MyViewHolder>(option) {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView =
                    LayoutInflater.from(applicationContext).inflate(R.layout.user_information_friends, parent, false)
                return MyViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: myFriends) {
                var friendid = getRef(position).key.toString()
                groupreference?.child(friendid)?.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if (p0!!.exists()) {
                            userreference?.child(friendid)?.addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError?) {

                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                                    if (p0!!.exists()) {
                                        var username = p0?.child("username").value.toString()
                                        holder.username.text = username
                                        var link = p0?.child("profileimage").value.toString()
                                        Picasso.get().load(link).placeholder(R.drawable.profile).into(holder.profilepic)
                                        holder.add_friend.text = "Added"
                                        holder.add_friend?.setBackgroundColor(resources.getColor(R.color.colorPrimary))

                                    }
                                }
                            })
                        } else {
                            userreference?.child(friendid)?.addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError?) {

                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                                    if (p0!!.exists()) {
                                        var username = p0?.child("username").value.toString()
                                        holder.username.text = username
                                        var link = p0?.child("profileimage").value.toString()
                                        Picasso.get().load(link).placeholder(R.drawable.profile).into(holder.profilepic)
                                        holder.add_friend.text = "Add"
                                        holder.add_friend?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

                                        holder.add_friend.setOnClickListener {
                                            addInGroup(friendid)

                                        }
                                    }
                                }
                            })
                        }
                    }

                })

            }
        }
        firebaseRecyclerAdapter.notifyDataSetChanged()
        mRecyclerView?.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }

    fun addInGroup(userid: String) {
        groupreference?.child(userid)?.child(userid)?.setValue(userid)?.addOnCompleteListener(object : OnCompleteListener<Void> {
            override fun onComplete(p0: Task<Void>) {
                if (p0.isSuccessful) {
                  var  groupdetailsreference=FirebaseDatabase.getInstance().getReference("Groups Details").child(userid)
                    groupdetailsreference.child(groupid).setValue(groupid)
                    generateNotificaction(uid!!,userid!!,senderName!!)
                    Toast.makeText(applicationContext, "Added to group", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun generateNotificaction(senderId: String, recieverId: String, username: String) {
        var tokenreference = FirebaseDatabase.getInstance().getReference("Tokens").child(recieverId)
        tokenreference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                var token = p0!!.value.toString()
                var data = notification(
                    senderId,
                    R.drawable.logo,
                    username + " has added you to group (" + groupname + ").",
                    "New Group",
                    recieverId
                )
                var sender = Sender(data, token)
                apiService?.sendNotification(sender)?.enqueue(object : Callback<MyResponse> {
                    override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                        if (response.code() == 200) {
                            if (response.body()?.success != 1) {
                                Toast.makeText(applicationContext, "Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                })
            }

        })


    }
    override fun onOptionsItemSelected(item: MenuItem):Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml
        if(item?.itemId==android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item);
    }
    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        var profilepic = itemView!!.findViewById<CircleImageView>(R.id.userinfofriends_profile_image)
        var add_friend = itemView!!.findViewById<Button>(R.id.userinfofriends_send_request)
        var username = itemView!!.findViewById<TextView>(R.id.userinfofriends_username)

    }
}