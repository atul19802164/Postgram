package com.tathagat.postgram

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar;
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

class MyFriend : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    var mToolbar: Toolbar? = null
    lateinit var friendsreference: DatabaseReference
    var firebaseuser: FirebaseUser? = null
    var userreference: DatabaseReference? = null
    var friends = ArrayList<myFriends>()
    var blockreference:DatabaseReference?=null
    var name: String = ""
    var currentuserid:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_friend)
        mToolbar = findViewById(R.id.myFriendsToolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Friends"
        recyclerView = findViewById(R.id.friends_recyclerView)
        blockreference=FirebaseDatabase.getInstance().getReference("Blocked")
        recyclerView?.layoutManager = GridLayoutManager(applicationContext, 1)
        firebaseuser = FirebaseAuth.getInstance().currentUser
        friendsreference = FirebaseDatabase.getInstance().getReference("Friends").child(firebaseuser?.uid)
        userreference=FirebaseDatabase.getInstance().getReference("Users")
currentuserid=FirebaseAuth.getInstance().currentUser!!.uid
        val option = FirebaseRecyclerOptions.Builder<myFriends>()
            .setQuery(friendsreference, myFriends::class.java)
            .build()
        val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<myFriends, MyViewHolder>(option) {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView = LayoutInflater.from(applicationContext).inflate(R.layout.users_display_layout,parent,false)
                return MyViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: myFriends) {
                var userid=getRef(position).key.toString()
                friendsreference.child(userid).addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0!!.exists()) {
                            holder.status.text = "Friend Since: "+model.date
                            userreference?.child(userid)?.addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError?) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                                    if(p0!!.exists()){
                                        var uid=p0?.key.toString()
                                        var name=p0?.child("fullname").value.toString()
                                        var username=p0?.child("username").value.toString()
                                        blockreference!!.child(currentuserid)?.child(userid)?.addValueEventListener(object :ValueEventListener{
                                            override fun onCancelled(p0: DatabaseError?) {

                                            }

                                            override fun onDataChange(p0: DataSnapshot) {
                                                if(p0.exists())
                                                    holder.username.text=username+"(Blocked)"
                                                else
                                                    holder.username.text=username
                                            }
                                        })

                                        holder.username.text=username
                                        var link=p0?.child("profileimage").value.toString()
                                        Picasso.get().load(link).placeholder(R.drawable.profile).into(holder.profilepic)
                                        holder.itemView.setOnClickListener {
                                            val builder = AlertDialog.Builder(this@MyFriend)
                                            builder.setTitle("Select Option")
                                            var options =
                                                arrayOf(username + "'s Post", "Send Message", "Unfriend " + username)
                                            builder.setItems(options, object : DialogInterface.OnClickListener {

                                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                                    when (which) {
                                                        0 -> {
                                                            var intent =
                                                                Intent(applicationContext, user_information::class.java)
                                                            intent.putExtra("reciever_id", uid)
                                                            startActivity(intent)
                                                        }
                                                        1 -> {
                                                            var intent =
                                                                Intent(applicationContext, MessageActivity::class.java)
                                                            intent.putExtra("reciever_id", uid)
                                                            intent.putExtra("username", username)
                                                            intent.putExtra("profilepic", link)
                                                            startActivity(intent)

                                                        }
                                                        2 -> {
                                                            unFriend(uid)
                                                        }

                                                    }

                                                }

                                            })
                                            builder.show()

                                        }

                                    }



                                }
                            })
                        }



                    }

                })

            }}
        firebaseRecyclerAdapter.notifyDataSetChanged()
        recyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }

    private fun unFriend(reciever_id:String){
    var senderid=FirebaseAuth.getInstance().currentUser?.uid
    var ref=FirebaseDatabase.getInstance().getReference("Friends")
    ref.child(senderid)?.child(reciever_id)?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
        override fun onComplete(p0: Task<Void>) {
            if(p0.isSuccessful){
                ref.child(reciever_id).child(senderid).removeValue().addOnCompleteListener(object :OnCompleteListener<Void>{
                    override fun onComplete(p0: Task<Void>) {
                        if(p0.isSuccessful)
                            Toast.makeText(applicationContext,"Removed from friend list",Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }

    })
}
    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

       var profilepic = itemView!!.findViewById<CircleImageView>(R.id.users_profile_image)
        var status = itemView!!.findViewById<TextView>(R.id.user_status)
        var username = itemView!!.findViewById<TextView>(R.id.user_profile_name)

    }


}

