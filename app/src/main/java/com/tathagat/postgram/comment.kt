package com.tathagat.postgram

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
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
import kotlin.collections.HashMap
import androidx.appcompat.widget.Toolbar;

class comment : AppCompatActivity() {
var usercommentpic:CircleImageView?=null
    var add_comment_text:EditText?=null
    var add_comment_btn:TextView?=null
    var commentreference:DatabaseReference?=null
    var userreference:DatabaseReference?=null
    var firebaseuser:FirebaseUser?=null
    var uid:String?=null
    var query:Query?=null
    var mRecyclerView:RecyclerView?=null
    var mToolbar:Toolbar?=null
            override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
                add_comment_text=findViewById(R.id.comment_text)
                add_comment_btn=findViewById(R.id.comment_post_btn)
                usercommentpic=findViewById(R.id.comment_userpic)
                firebaseuser=FirebaseAuth.getInstance().currentUser
                uid=firebaseuser?.uid
                mRecyclerView=findViewById(R.id.commentRecyclerView)
                mToolbar=findViewById(R.id.commentToolbar)
                setSupportActionBar(mToolbar)
                supportActionBar?.title="Comments"
                supportActionBar?.setDisplayShowHomeEnabled(true)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                var linearLayoutManager=LinearLayoutManager(this)
                linearLayoutManager?.reverseLayout=true
                linearLayoutManager?.stackFromEnd=true
                mRecyclerView?.layoutManager=linearLayoutManager
                var post_id=intent.getStringExtra("post_id")
                commentreference=FirebaseDatabase.getInstance().getReference("Comments").child(post_id)
                query=commentreference?.orderByChild("comment date")
                userreference=FirebaseDatabase.getInstance().getReference("Users")
                userreference?.child(uid)?.addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if(p0!!.exists()){
                            var profileimage=p0.child("profileimage").value.toString()
                            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(usercommentpic)
                        }
                    }

                })
                displayComment()
                add_comment_btn?.setOnClickListener {
                    addComment()
                }
    }

    private fun displayComment() {
        val option = FirebaseRecyclerOptions.Builder<myFriends>()
            .setQuery(query!!, myFriends::class.java).build()
        val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<myFriends, MyViewHolder>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView = LayoutInflater.from(applicationContext).inflate(R.layout.comment_layout,parent,false)
                return MyViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: myFriends) {
var commentid=getRef(position)?.key.toString()
                commentreference?.child(commentid)?.addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                    var text=p0?.child("comment_text")?.value.toString()
                        var userid=p0?.child("commenter_id")?.value.toString()
                        userreference?.child(userid)?.addValueEventListener(object :ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {

                            }

                            override fun onDataChange(p0: DataSnapshot?) {
                                if(p0!!.exists()){
                                    var pic=p0?.child("profileimage")?.value.toString()
                                    var username=p0?.child("username")?.value.toString()
                                    holder?.username.text=username
                                    holder?.comment_text.text=text
                                    Picasso.get().load(pic).placeholder(R.drawable.profile).into(holder.userpic)
                                    holder.userpic.setOnClickListener {
                                        var intent = Intent(applicationContext, user_information::class.java)
                                        intent.putExtra("reciever_id", userid)
                                        startActivity(intent)
                                    }
                                    if(uid.equals(userid)){
                                    holder.itemView.setOnLongClickListener{
                                        var alertDialog=AlertDialog.Builder(this@comment)
                                        alertDialog.setMessage("Do you want to delete this comment?")
                                        alertDialog.setPositiveButton("yes",object :DialogInterface.OnClickListener{

                                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                            commentreference?.child(commentid)?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                                override fun onComplete(p0: Task<Void>) {
                                                    if(p0.isSuccessful){
                                                        Toast.makeText(applicationContext,"Comment Deleted",Toast.LENGTH_SHORT).show()

                                                    }
                                                }

                                            })
                                            }

                                        })
                                        alertDialog?.setNegativeButton("no",object :DialogInterface.OnClickListener{

                                            override fun onClick(dialog: DialogInterface?, which: Int) {

                                            }

                                        })
                                        alertDialog.show()

                                        true
                                    }
                                }}
                              }

                        })
                    }

                })

            }
        }



        firebaseRecyclerAdapter.notifyDataSetChanged()
        mRecyclerView?.adapter=firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }

    private fun addComment() {
        var comment_text=add_comment_text?.text.toString()
        add_comment_text?.setText("")
        if(TextUtils.isEmpty(comment_text))
            Toast.makeText(applicationContext,"Please enter something for comment...",Toast.LENGTH_SHORT).show()
        else{
            var today= Date()
            val format = SimpleDateFormat("yyyyMMddHHmmss")
            val date = format.format(today)
            var comment_info=HashMap<String,Any>()
            comment_info.put("commenter_id",uid!!)
            comment_info.put("comment_text",comment_text)
            comment_info.put("comment date",date)

            commentreference?.push()?.updateChildren(comment_info)?.addOnCompleteListener(object :OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    if(p0.isSuccessful)

                    Toast.makeText(applicationContext,"Comment Posted",Toast.LENGTH_SHORT).show()
                }

            })


        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId==android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }
    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
var username=itemView!!.findViewById<TextView>(R.id.comment_username)
        var userpic=itemView!!.findViewById<CircleImageView>(R.id.commenter_userpic)
        var comment_text=itemView!!.findViewById<TextView>(R.id.comment_desp)

    }

}
