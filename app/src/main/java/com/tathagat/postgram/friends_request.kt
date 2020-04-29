package com.tathagat.postgram

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import java.text.SimpleDateFormat
import java.util.*

class friends_request : AppCompatActivity() {
var mToolbar:Toolbar?=null
    var recyclerView:RecyclerView?=null
    lateinit var requestreference:DatabaseReference
    var userreference:DatabaseReference?=null
    var firebaseuser:FirebaseUser?=null
    lateinit var databaseReference: DatabaseReference
    var friendsreference:DatabaseReference?=null
    lateinit var userid:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_request)
        mToolbar = findViewById(R.id.friendrequest_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = "Requests"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = findViewById(R.id.friendrequestRecyclerView)
        recyclerView?.layoutManager = GridLayoutManager(applicationContext, 1)
        firebaseuser = FirebaseAuth.getInstance().currentUser
        userid = firebaseuser?.uid!!
        friendsreference=FirebaseDatabase.getInstance().getReference("Friends")
        databaseReference=FirebaseDatabase.getInstance().getReference("Friends Requests")
        requestreference = FirebaseDatabase.getInstance().getReference("Friends Requests").child(userid)
        userreference = FirebaseDatabase.getInstance().getReference("Users")
    showRequest()
    }

    fun showRequest() {

        var option = FirebaseRecyclerOptions.Builder<myFriends>()
            .setQuery(requestreference, myFriends::class.java)
            .build()
        var firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<myFriends, MyViewHolder>(option) {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView = LayoutInflater.from(applicationContext).inflate(R.layout.users_display_layout,parent,false)
                return MyViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: myFriends) {
                var request_id= getRef(position).key.toString()

                requestreference?.child(request_id)?.addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0!!.exists()){
                        if(p0!!.child("request_type").value!!.equals("recieved"))
                        {
                            userreference!!.child(request_id).addValueEventListener(object :ValueEventListener{
                                override fun onCancelled(p0: DatabaseError?) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                                    if(p0!!.exists()){
                                        var link=p0?.child("profileimage").value.toString()
                                        holder.username.text=p0?.child("username").value.toString()
                                        Picasso.get().load(link).placeholder(R.drawable.profile).into(holder.profilepic)
                                        holder.status.text=p0?.child("fullname").value.toString()
                                        holder.accept_request.visibility=View.VISIBLE
                                        holder.cancel_request.visibility=View.VISIBLE
                                        holder.profilepic.setOnClickListener {
                                            var intent =Intent(this@friends_request,
                                                user_information::class.java)
                                            intent.putExtra("reciever_id",request_id)
                                            startActivity(intent)
                                        }
holder.accept_request.setOnClickListener {
acceptFriendRequest(request_id)
}
                                        holder.cancel_request.setOnClickListener {
cancelFriendRequest(request_id)

                                        }


                                    }       }

                            })
                        }
                            else{
                            if(p0.child("request_type").value.toString().equals("sent")){
                                userreference?.child(request_id)?.addValueEventListener(object :ValueEventListener{
                                    override fun onCancelled(p0: DatabaseError?) {
                                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    }

                                    override fun onDataChange(p0: DataSnapshot?) {
                                        if(p0!!.exists()){
                                            var link=p0?.child("profileimage").value.toString()
                                            holder.username.text=p0?.child("username").value.toString()
                                            Picasso.get().load(link).placeholder(R.drawable.profile).into(holder.profilepic)
                                            holder.status.text=p0?.child("fullname").value.toString()
                                            holder.accept_request.text="Request Sent"
                                            holder.cancel_request.visibility=View.GONE
                                            holder.accept_request.visibility=View.VISIBLE
                                            holder.profilepic.setOnClickListener {
                                                var intent =Intent(this@friends_request,
                                                    user_information::class.java)
                                                intent.putExtra("reciever_id",request_id)
                                                startActivity(intent)
                                            }
                                            holder.itemView.setOnClickListener {
                                                var dialog = AlertDialog.Builder(this@friends_request)
                                                dialog.setMessage("Decline friend request?")
                                                dialog.setPositiveButton("Yes",object :DialogInterface.OnClickListener{
                                                    /**
                                                     * This method will be invoked when a button in the dialog is clicked.
                                                     *
                                                     * @param dialog the dialog that received the click
                                                     * @param which the button that was clicked (ex.
                                                     * [DialogInterface.BUTTON_POSITIVE]) or the position
                                                     * of the item clicked
                                                     */
                                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                                        declineFriendRequest(request_id)
                                                    }

                                                })
                                                dialog.setNeutralButton("Cancel",object :DialogInterface.OnClickListener{
                                                    /**
                                                     * This method will be invoked when a button in the dialog is clicked.
                                                     *
                                                     * @param dialog the dialog that received the click
                                                     * @param which the button that was clicked (ex.
                                                     * [DialogInterface.BUTTON_POSITIVE]) or the position
                                                     * of the item clicked
                                                     */
                                                    override fun onClick(dialog: DialogInterface?, which: Int) {

                                                    }

                                                })

                                                dialog.show()
                                            }
                                        }
                                    }

                                })

                            }
                        }
                    }}
                })


            }}
        firebaseRecyclerAdapter.notifyDataSetChanged()
        recyclerView?.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
        }

    private fun declineFriendRequest(request_id: String) {
        databaseReference.child(userid).child(request_id).removeValue().addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful){
                    databaseReference.child(request_id).child(userid).removeValue().addOnCompleteListener(object :OnCompleteListener<Void>{
                        override fun onComplete(p0: Task<Void>) {
                            if(p0.isSuccessful){
                                Toast.makeText(applicationContext,"Friend Request Declined",Toast.LENGTH_SHORT).show()
                            }
                        }
                    })

                }
            }

        })
    }

    private fun cancelFriendRequest(request_id: String) {
        databaseReference.child(userid).child(request_id).removeValue().addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful){
                    databaseReference.child(request_id).child(userid).removeValue().addOnCompleteListener(object :OnCompleteListener<Void>{
                        override fun onComplete(p0: Task<Void>) {
                            if(p0.isSuccessful)
                                Toast.makeText(applicationContext,"Request Cancelled",Toast.LENGTH_SHORT).show()
                        }

                    })
                }
            }

        })

    }

    private fun acceptFriendRequest( request_id:String) {
        databaseReference.child(userid).child(request_id).removeValue().addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful){
                    databaseReference.child(request_id).child(userid).removeValue().addOnCompleteListener(object :
                    OnCompleteListener<Void>{
                        override fun onComplete(p0: Task<Void>) {
                            if(p0?.isSuccessful){
                                val c = Calendar.getInstance().getTime()
                                val df = SimpleDateFormat("dd-MMM-yyyy")
                                var saveCurrentDate = df.format(c)
                                friendsreference?.child(userid)?.child(request_id)?.child("date")?.setValue(saveCurrentDate)?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                    override fun onComplete(p0: Task<Void>) {
                                        if(p0.isSuccessful){
                                            friendsreference!!.child(request_id)?.child(userid)?.child("date")?.setValue(saveCurrentDate)?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                                override fun onComplete(p0: Task<Void>) {
                                                    if(p0?.isSuccessful){
                                                        Toast.makeText(applicationContext,"Added in Friend List",Toast.LENGTH_SHORT).show()
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


    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        var profilepic = itemView!!.findViewById<CircleImageView>(R.id.users_profile_image)
        var status = itemView!!.findViewById<TextView>(R.id.user_status)
        var username = itemView!!.findViewById<TextView>(R.id.user_profile_name)
        var accept_request=itemView!!.findViewById<Button>(R.id.request_accept_btn)
        var cancel_request=itemView!!.findViewById<Button>(R.id.request_cancel_btn)

    }


}



