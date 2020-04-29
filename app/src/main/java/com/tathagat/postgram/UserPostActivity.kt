package com.tathagat.postgram

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class UserPostActivity : AppCompatActivity() {
    lateinit var commentreference:DatabaseReference
    var navigationView: NavigationView?=null
    var drawerLayout: DrawerLayout?=null
    var postList: RecyclerView?=null
    var mToolbar:Toolbar?=null
    var firebaseauth: FirebaseAuth?=null
    var firebaseuser:FirebaseUser?=null
    var firebasedatabase:FirebaseDatabase?=null
    var databasereference:DatabaseReference?=null
    lateinit var postreference:DatabaseReference
    var friendreference:DatabaseReference?=null
    var profilepic: CircleImageView?=null
    var username: TextView?=null
    var add_posts_button: ImageButton?=null
    var my_stories: CircleImageView?=null
    var add_my_story: LinearLayout?=null
    var storyreference:DatabaseReference?=null
    var storytype: TextView?=null
    var add_story_btn: CircleImageView?=null
    var comment_count: TextView?=null
    var like_count: TextView?=null
    var no_like:Long=0
    var no_comment:Long=0
    var uid:String?=null
    var story_recyclerview:RecyclerView?=null
    var query: Query?=null
    var querystory: Query?=null
    var userreference:DatabaseReference?=null
    var likereference:DatabaseReference?=null
    var postid:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_post)
        mToolbar = findViewById(R.id.userposttoolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setTitle("Post")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        firebaseauth = FirebaseAuth.getInstance()
        firebaseuser = firebaseauth?.currentUser
        firebasedatabase = FirebaseDatabase.getInstance()
        likereference=FirebaseDatabase.getInstance().getReference("Likes")
        uid = firebaseuser?.uid.toString()
        postList=findViewById(R.id.particularpostsRecyclerView)
        var layoutManager= LinearLayoutManager(applicationContext)
        layoutManager.reverseLayout=true
        layoutManager.stackFromEnd=true
        postList?.layoutManager=layoutManager
        databasereference = firebasedatabase?.reference?.child("Users")
        friendreference=FirebaseDatabase.getInstance().getReference("Friends").child(uid)
        postid=intent.getStringExtra("post_id")
        postreference=FirebaseDatabase.getInstance().getReference("Posts")
        query=postreference
        userreference=FirebaseDatabase.getInstance().getReference("Users")
        commentreference=FirebaseDatabase.getInstance().getReference("Comments")
   displayUserPosts()
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId==android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)

    }
    private fun displayUserPosts() {
        val option = FirebaseRecyclerOptions.Builder<myFriends>()
            .setQuery(query!!, myFriends::class.java).build()
        val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<myFriends, MyViewHolder>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView = LayoutInflater.from(applicationContext).inflate(R.layout.users_posts_layout,parent,false)
                return MyViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: myFriends) {
                var post_id=getRef(position).key.toString()
                commentreference?.child(post_id).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        no_comment=p0?.childrenCount!!
                        if(no_comment>1)
                            holder.comment_counter.text=no_comment.toString()+" comments"
                        else{
                            if(no_comment<=0)
                                holder.comment_counter.text="No comments"
                            else
                                holder.comment_counter.text=no_comment.toString()+" comment"
                        }

                    }

                })
                likereference?.child(post_id)?.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        no_like=p0?.childrenCount!!
                        if(no_like>1)
                            holder.like_counter.text=no_like.toString()+" likes"
                        else{
                            if(no_like<=0)
                                holder.like_counter.text="No likes"
                            else
                                holder.like_count.text=no_like.toString()+"like"
                        }

                    }

                })

                postreference?.child(post_id)?.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if (p0!!.exists()){
                            var userid=p0?.child("uid").value.toString()
                            var postimage=p0?.child("postimage").value.toString()
                            var date=p0?.child("date").value.toString()
                            var time=p0?.child("time").value.toString()
                            var desp=p0?.child("description").value.toString()
                            userreference?.child(userid)?.addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError?) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                                    if(p0!!.exists()){
                                        var username=p0?.child("username").value.toString()
                                        var profileimage=p0?.child("profileimage").value.toString()
                                        friendreference?.child(userid)?.addValueEventListener(object : ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError?) {
                                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                            }

                                            override fun onDataChange(p0: DataSnapshot?) {
                                                if (post_id.equals(postid)) {
                                                    holder.relativeLayout.visibility= View.VISIBLE
                                                    holder.username.text = username
                                                    holder.date.text = date
                                                    holder.time.text = time
                                                    holder.post_desp.text = desp

                                                    if(userid.equals(firebaseuser?.uid))
                                                    {
                                                        holder.edit_option.visibility= View.VISIBLE

                                                        holder.edit_option.setOnClickListener {
                                                            var alertDialog= AlertDialog.Builder(this@UserPostActivity)
                                                            var options= arrayOf("Edit Post","Delete Post")
                                                            alertDialog.setItems(options,object : DialogInterface.OnClickListener{

                                                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                                                    when(which){
                                                                        0->{
                                                                            editPost(post_id,desp)
                                                                        }
                                                                        1->{
                                                                            var dialog= AlertDialog.Builder(this@UserPostActivity)
                                                                            dialog.setMessage("Do you want to delete your post?")
                                                                            dialog.setPositiveButton("yes",object : DialogInterface.OnClickListener{
                                                                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                                                                    deletePost(post_id)
                                                                                }

                                                                            })

                                                                            dialog.setNegativeButton("no",object : DialogInterface.OnClickListener{

                                                                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                                                                    dialog?.dismiss()
                                                                                }

                                                                            })
                                                                            dialog?.show()

                                                                        }

                                                                    }
                                                                }

                                                            })
                                                            alertDialog.show()
                                                        }
                                                    }
                                                    else {
                                                        holder.edit_option.visibility= View.GONE
                                                    }
                                                    Picasso.get().load(profileimage).placeholder(R.drawable.profile)
                                                        .into(holder.profilepic)
                                                    Picasso.get().load(postimage).into(holder.post_pic)
                                                    holder.itemView.setOnClickListener{
                                                        var intent = Intent(applicationContext,
                                                            ImageInFullSize::class.java)
                                                        intent.putExtra("image link",postimage)
                                                        startActivity(intent)
                                                    }
                                                    Picasso.get().load(R.drawable.comment).into(holder.comment_btn)
                                                    likereference?.child(post_id)?.child(firebaseuser?.uid)?.addValueEventListener(object :
                                                        ValueEventListener {
                                                        override fun onCancelled(p0: DatabaseError?) {

                                                        }

                                                        override fun onDataChange(p0: DataSnapshot?) {
                                                            if(p0!!.exists()){
                                                                Picasso.get().load(R.drawable.like).into(holder.like_btn)
                                                                holder.like_btn?.setOnClickListener {
                                                                    dislikePost(post_id)
                                                                }
                                                            }
                                                            else{
                                                                Picasso.get().load(R.drawable.dislike).into(holder.like_btn)
                                                                holder.like_btn?.setOnClickListener {
                                                                    likePost(post_id)
                                                                }
                                                            }
                                                        }

                                                    })
                                                    holder?.comment_btn?.setOnClickListener {
                                                        var intent = Intent(this@UserPostActivity, comment::class.java)
                                                        intent.putExtra("post_id", post_id)
                                                        startActivity(intent)
                                                    }


                                                    holder.profilepic.setOnClickListener {
                                                        var intent= Intent(this@UserPostActivity,
                                                            user_information::class.java)
                                                        intent.putExtra("reciever_id",userid)
                                                        startActivity(intent)
                                                    }

                                                    holder.like_counter.setOnClickListener {
                                                        var intent= Intent(this@UserPostActivity,
                                                            likes::class.java)
                                                        intent.putExtra("post_id",post_id)
                                                        startActivity(intent)
                                                    }
                                                }
                                                else {
                                                    holder.itemView.visibility = View.GONE
                                                    var params=holder.itemView.layoutParams
                                                    params.height=0
                                                    holder.itemView.layoutParams=params

                                                }
                                            }

                                        })


                                    }
                                }

                            })


                        }}

                })
                holder.like_counter?.setOnClickListener {

                }
            }
        }



        firebaseRecyclerAdapter.notifyDataSetChanged()
        postList?.adapter=firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }

    private fun likePost(post_id: String) {
        var today= Date()
        val format = SimpleDateFormat("yyyyMMddhhmmss")
        val date = format.format(today)
        likereference?.child(post_id)?.child(firebaseuser?.uid)?.child("liked_date")?.setValue(date)?.addOnCompleteListener(object :
            OnCompleteListener<Void> {
            override fun onComplete(p0: Task<Void>) {

                if(p0.isSuccessful)
                    Toast.makeText(applicationContext,"You liked the post", Toast.LENGTH_SHORT).show()
            }

        })


    }

    private fun dislikePost(post_id: String) {
        likereference?.child(post_id)?.child(firebaseuser?.uid)?.removeValue()?.addOnCompleteListener(object :
            OnCompleteListener<Void> {
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful)
                    Toast.makeText(applicationContext,"You disliked the post", Toast.LENGTH_SHORT).show()


            }
        })

    }

    private fun deletePost(post_id: String) {
        postreference?.child(post_id).removeValue()?.addOnCompleteListener(object : OnCompleteListener<Void> {
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful){
                    Toast.makeText(applicationContext,"Post Deleted", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }
            }

        })
    }
    private fun editPost(post_id:String,post_desp:String) {
        var dialog= AlertDialog.Builder(this)
        dialog.setTitle("Update Post")
        var post_description= EditText(this)
        post_description.setText(post_desp)
        dialog.setView(post_description)
        dialog.setPositiveButton("Update",object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                var progressDialog= ProgressDialog(this@UserPostActivity)
                progressDialog?.setTitle("Updating post")
                progressDialog?.setMessage("Please wait while we are updating your post...")
                progressDialog?.setCanceledOnTouchOutside(false)
                progressDialog?.show()
                postreference?.child(post_id).child("description")?.setValue(post_description.text.toString())?.addOnCompleteListener(object :
                    OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        if(p0.isSuccessful){
                            progressDialog?.dismiss()
                            Toast.makeText(applicationContext,"Post Updated", Toast.LENGTH_SHORT).show()

                        }
                    }

                })
            }

        })
        dialog.setNegativeButton("Cancel",object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }

        })
        dialog.show()
    }
    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {


        var profilepic = itemView!!.findViewById<CircleImageView>(R.id.post_profile_image)
        var username=itemView!!.findViewById<TextView>(R.id.post_username)
        var post_desp = itemView!!.findViewById<TextView>(R.id.post_desp)
        var date = itemView!!.findViewById<TextView>(R.id.post_date)
        var time = itemView!!.findViewById<TextView>(R.id.post_time)
        var post_pic = itemView!!.findViewById<ImageView>(R.id.post_image)
        var relativeLayout=itemView!!.findViewById<RelativeLayout>(R.id.postRelativeLayout)
        var like_btn=itemView!!.findViewById<ImageView>(R.id.like_btn)
        var comment_btn=itemView!!.findViewById<ImageView>(R.id.comment_btn)
        var like_count=itemView!!.findViewById<TextView>(R.id.like_count)
        var username_story=itemView!!.findViewById<TextView>(R.id.story_username)
        var story_userpic=itemView!!.findViewById<CircleImageView>(R.id.friend_status_userpic)
        var edit_option=itemView!!.findViewById<ImageView>(R.id.edit_post_icon)
        var comment_counter=itemView!!.findViewById<TextView>(R.id.comment_count)
        var like_counter=itemView!!.findViewById<TextView>(R.id.like_count)
    }

}
