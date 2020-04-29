package com.tathagat.postgram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class likes : AppCompatActivity() {
    var mRecyclerView: RecyclerView? = null
    var likereference: DatabaseReference? = null
    var mToolbar: Toolbar? = null
    var query: Query? = null
    var userreference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likes)
        var post_id = intent.getStringExtra("post_id")
        likereference=FirebaseDatabase.getInstance().getReference("Likes").child(post_id)
        userreference=FirebaseDatabase.getInstance().getReference("Users")
        mRecyclerView=findViewById(R.id.likesRecyclerView)
        mToolbar=findViewById(R.id.likesToolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title="Likes"
        var linearLayoutManager=LinearLayoutManager(this)
        linearLayoutManager.reverseLayout=true
        linearLayoutManager.stackFromEnd=true
        mRecyclerView?.layoutManager=linearLayoutManager
        showLikes()
    }

    private fun showLikes() {
        val option = FirebaseRecyclerOptions.Builder<myFriends>()
            .setQuery(likereference!!, myFriends::class.java)
            .build()
        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<myFriends, MyViewHolder>(option) {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView =
                    LayoutInflater.from(applicationContext).inflate(R.layout.users_display_layout, parent, false)
                return MyViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: myFriends) {
                var userid = getRef(position).key.toString()
                userreference?.child(userid)?.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        var profileimage = p0?.child("profileimage")?.value.toString()
                        var username = p0?.child("username")?.value.toString()
                        var name = p0?.child("fullname")?.value.toString()
                        Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(holder.profilepic)
                        holder?.username.text = username
                        holder.status.text = name
                        holder.itemView.setOnClickListener {
                            var intent = Intent(applicationContext, user_information::class.java)
                            intent.putExtra("reciever_id", userid)
                            startActivity(intent)
                        }
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

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        var profilepic = itemView!!.findViewById<CircleImageView>(R.id.users_profile_image)
        var status = itemView!!.findViewById<TextView>(R.id.user_status)
        var username = itemView!!.findViewById<TextView>(R.id.user_profile_name)


    }
}