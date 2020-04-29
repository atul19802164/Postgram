package com.tathagat.postgram

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MessageList : AppCompatActivity() {
    var mRecyclerView:RecyclerView?=null
    var mToolbar:Toolbar?=null
    var messagereference:DatabaseReference?= null
    var userreference:DatabaseReference?= null
    var uid:String?=null
    var query:Query?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)
        mToolbar=findViewById(R.id.messageListToolbar)
        mRecyclerView=findViewById(R.id.messageListRecyclerView)
        var layoutManager= LinearLayoutManager(this)
        layoutManager.reverseLayout=true
        layoutManager.stackFromEnd=true
        mRecyclerView?.layoutManager=layoutManager
        mToolbar=findViewById(R.id.messageListToolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title="Messages"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        uid=FirebaseAuth.getInstance().currentUser?.uid
        messagereference=FirebaseDatabase.getInstance().getReference("Messages").child(uid)
        messagereference!!.keepSynced(true)
        query=messagereference?.orderByChild("last sent")
        userreference=FirebaseDatabase.getInstance().getReference("Users")
displayMessagesList()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId==android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)

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

    private fun displayMessagesList() {
        val option = FirebaseRecyclerOptions.Builder<myFriends>()
            .setQuery(query!!, myFriends::class.java).build()
        val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<myFriends, MyViewHolder>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView = LayoutInflater.from(applicationContext).inflate(R.layout.user_messages_layout,parent,false)
                return MyViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: myFriends) {
                var reciever_id=getRef(position).key.toString()
                var last_message=""
                    messagereference?.child(reciever_id)?.addValueEventListener(object :ValueEventListener{
                        override fun onCancelled(p0: DatabaseError?) {

                        }

                        override fun onDataChange(p0: DataSnapshot?) {
                            if(p0!!.exists()){

                                for(x in p0.children) {
                                   if(!x.key.toString().equals("last sent")) {
                                        var type = x.child("type").value.toString()
                                        if (type.equals("text")) {
                                            last_message = x.child("message")?.value.toString()
                                        }
                                       if(type.equals("image")){
                                           last_message="Photo"
                                       }
                                       if(type.equals("video"))
                                           last_message="Video"
                                       if(type.startsWith("$"))
                                           last_message="Replied on a story"

                                    }

                                    }
                                if(last_message.length>30){
                                    last_message=last_message.substring(0,25)+"..."
                                }
                                holder.last_msg.text=last_message

var ref=FirebaseDatabase.getInstance().getReference("Messages").child(uid).child(reciever_id)
                                ref.addValueEventListener(object :ValueEventListener{
                                    override fun onCancelled(p0: DatabaseError?) {

                                    }

                                    override fun onDataChange(p0: DataSnapshot?) {
                                     if(p0!!.exists()){
                                         var count=0
                                         for(x in p0.children){
                                             var seen=x.child("message seen").value.toString()
                                             var from=x.child("from").value.toString()
                                             if(seen.equals("false")&&from.equals(reciever_id))
                                                 count++

                                         }
                                         if(count>0){
                                            holder.unseen_message_count?.text=count.toString()
                                            holder.unseen_message_count?.visibility=View.VISIBLE
                                         }
                                         else
                                             holder.unseen_message_count?.visibility=View.GONE
                                     }
                                    }

                                })
                            }
                        }

                    })
                var profileimage:String?=null
                var username:String?=null
                    userreference?.child(reciever_id)?.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {

                        }

                        override fun onDataChange(p0: DataSnapshot?) {
                        profileimage=p0?.child("profileimage")?.value.toString()
                            username=p0?.child("username")?.value.toString()
                            holder.username.text=username
                            var status=p0?.child("User Status")?.child("status")?.value.toString()
                            if(status.equals("is typing..."))
                                holder.last_msg.text="is typing..."
                            else
                                holder.last_msg.text=last_message
                            if(status.equals("online")||status.equals("is typing..."))
                                holder.online_icon.visibility=View.VISIBLE
                            else
                                holder.online_icon.visibility=View.GONE
                            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(holder.profilepic)
                        }


                    })
                holder.itemView.setOnClickListener {
                    var intent =Intent(applicationContext, MessageActivity::class.java)
                    intent.putExtra("reciever_id",reciever_id)
                    intent.putExtra("username",username)
                    intent.putExtra("profilepic",profileimage)
                    startActivity(intent)
                }

            }
        }



        firebaseRecyclerAdapter.notifyDataSetChanged()
        mRecyclerView?.adapter=firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }


    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {


        var profilepic = itemView!!.findViewById<CircleImageView>(R.id.message_profile_image)
        var username=itemView!!.findViewById<TextView>(R.id.message_username)
        var online_icon = itemView!!.findViewById<CircleImageView>(R.id.online_icon)
        var last_msg=itemView!!.findViewById<TextView>(R.id.message_desp)
        var unseen_message_count=itemView!!.findViewById<TextView>(R.id.unseen_message_count)
    }
}
