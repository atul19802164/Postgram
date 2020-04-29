package com.tathagat.postgram

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.lang.Exception

class MainActivity : AppCompatActivity() {
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
    var profilepic:CircleImageView?=null
    var username:TextView?=null
    var add_posts_button:ImageButton?=null
    var my_stories:CircleImageView?=null
    var add_my_story:LinearLayout?=null
    var storyreference:DatabaseReference?=null
    var storytype:TextView?=null
    var add_story_btn:CircleImageView?=null
    var comment_count:TextView?=null
    var like_count:TextView?=null
    var no_like:Long=0
    var no_comment:Long=0
var uid:String?=null
    var apiService: APIService?=null
    var story_recyclerview:RecyclerView?=null
    var query:Query?=null
    var querystory:Query?=null
    var userreference:DatabaseReference?=null
    var likereference:DatabaseReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        add_posts_button = findViewById(R.id.add_new_post_button)
        var header = navigationView?.getHeaderView(0)
        mToolbar = findViewById(R.id.main_page_toolbar)
        firebaseauth = FirebaseAuth.getInstance()
        firebaseuser = firebaseauth?.currentUser
        firebasedatabase = FirebaseDatabase.getInstance()
        my_stories=findViewById(R.id.my_stories)
        add_story_btn=findViewById(R.id.add_story_btn)
        storytype=findViewById(R.id.story_type)
        storyreference=FirebaseDatabase.getInstance().getReference("Stories")
        likereference=FirebaseDatabase.getInstance().getReference("Likes")

        apiService = Client()
            .getClient("https://fcm.googleapis.com/").create(APIService::class.java)
        querystory=storyreference?.orderByChild("lastupdated")
        add_my_story=findViewById(R.id.add_my_story)
story_recyclerview=findViewById(R.id.storiesRecyclerView)
        var linearLayoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true)
        linearLayoutManager.reverseLayout=true
        linearLayoutManager.stackFromEnd=true
        story_recyclerview?.layoutManager=linearLayoutManager
        profilepic = header!!.findViewById(R.id.nav_profile_image)
        username = header!!.findViewById(R.id.nav_user_full_name)
        uid = firebaseuser?.uid.toString()
        var tokenreference=FirebaseDatabase.getInstance().getReference("Tokens")
        var device_token= FirebaseInstanceId.getInstance().getToken()
        tokenreference.child(uid).setValue(device_token)

        postList=findViewById(R.id.all_users_post_lists)
        var layoutManager=LinearLayoutManager(applicationContext)
        layoutManager.reverseLayout=true
        layoutManager.stackFromEnd=true
        postList?.layoutManager=layoutManager
        databasereference = firebasedatabase?.reference?.child("Users")
        friendreference=FirebaseDatabase.getInstance().getReference("Friends").child(uid)
        postreference=FirebaseDatabase.getInstance().getReference("Posts")
        query=postreference.orderByChild("counter")
        userreference=FirebaseDatabase.getInstance().getReference("Users")
         commentreference=FirebaseDatabase.getInstance().getReference("Comments")
               if (firebaseuser == null) {
            sendToLoginActivity()
        } else {
                   var isemail=false
                   for(user in FirebaseAuth.getInstance().currentUser!!.providerData){
                       if(user.providerId.equals("password"))
                           isemail=true
                   }
                   if(isemail&&!firebaseuser!!.isEmailVerified)
                       sendToLoginActivity()
            setSupportActionBar(mToolbar)
            supportActionBar?.title = "Postgram"
            databasereference?.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot?) {
if(p0!!.child(uid).child("profileimage").exists())
{

    var profilepic_url=p0.child(uid).child("profileimage").value.toString()
    Picasso.get().load(profilepic_url).placeholder(R.drawable.profile).into(profilepic)
    Picasso.get().load(profilepic_url).placeholder(R.drawable.profile).into(my_stories)
}
else{
    Picasso.get().load(R.drawable.profile).placeholder(R.drawable.profile).into(profilepic)
}
                    if (p0.child(uid).child("fullname").exists()) {
                        var fullname = p0?.child(uid).child("fullname").value.toString()
                        username?.text = fullname
                    }


                }
            })

            add_posts_button?.setOnClickListener {
                startActivity(Intent(applicationContext, PostActivity::class.java))
            }

            var toggle = ActionBarDrawerToggle(
                this, drawerLayout, mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            )
            drawerLayout?.addDrawerListener(toggle)
            toggle.syncState()
            navigationView?.setNavigationItemSelectedListener(object :
                NavigationView.OnNavigationItemSelectedListener {
                override fun onNavigationItemSelected(p0: MenuItem): Boolean {
                    when (p0.itemId) {
                        R.id.nav_add_posts -> {

                            startActivity(Intent(applicationContext, PostActivity::class.java))

                        }
                        R.id.nav_profile -> {
                            var intent= Intent(applicationContext, user_information::class.java)
                            intent.putExtra("reciever_id",uid)
                            startActivity(intent) }

                        R.id.nav_friends -> {
                            startActivity(Intent(applicationContext, MyFriend::class.java))

                        }
                        R.id.nav_find_friends -> {
                            startActivity(Intent(applicationContext, find_friends::class.java))
                        }
                        R.id.nav_messages -> {
                            startActivity(Intent(applicationContext, MessageList::class.java))
                        }
                        R.id.nav_settings -> {
                            var intent= Intent(applicationContext, EditProfile::class.java)
                            startActivity(intent) }

                        R.id.nav_logout -> {
                            firebaseauth?.signOut()
                            sendToLoginActivity()
                            finish()
                        }
                        R.id.nav_friends_requests ->{
                            startActivity(Intent(applicationContext, friends_request::class.java))
                        }
R.id.nav_groups ->{
    startActivity(Intent(applicationContext, Groups::class.java))

}
                    }
                    return true
                }

            })

        }
        displayUserPosts()
displayStories()
        startStoriesFeatures()
        postreference!!.keepSynced(true)
        storyreference!!.keepSynced(true)
        likereference!!.keepSynced(true)
        commentreference!!.keepSynced(true)
    var madAview=findViewById<AdView>(R.id.adView)
MobileAds.initialize(this@MainActivity,"ca-app-pub-7840922433679558~6851867431")
        var adRequest= AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build()
        madAview.loadAd(adRequest)

    }
 fun generateNotificaction(recieverId:String, username:String, message:String) {
     var tokenreference = FirebaseDatabase.getInstance().getReference("Tokens").child(recieverId)
     tokenreference.addValueEventListener(object :ValueEventListener{
         override fun onCancelled(p0: DatabaseError?) {

         }

         override fun onDataChange(p0: DataSnapshot?) {

             var token =p0!!.value.toString()
             var data= notification(
                 firebaseuser!!.uid,
                 R.drawable.logo,
                 username + ":" + message,
                 "New Message",
                 firebaseuser!!.uid
             )
             var sender= Sender(data, token)
             apiService?.sendNotification(sender)?.enqueue(object :Callback<MyResponse>{
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


    private fun displayStories() {
    val option = FirebaseRecyclerOptions.Builder<myFriends>()
        .setQuery(querystory!!, myFriends::class.java).build()
    val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<myFriends, MyViewHolder>(option) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(applicationContext).inflate(R.layout.friends_status_layout,parent,false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: myFriends) {
var userid=getRef(position).key.toString()
            friendreference?.child(userid)?.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0!!.exists()){
                        userreference?.child(userid)?.addValueEventListener(object :ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {

                            }

                            override fun onDataChange(p0: DataSnapshot?) {
                                if(p0!!.exists()) {
                                    var username = p0?.child("username")?.value.toString()
                                    var profilepic = p0?.child("profileimage").value.toString()
                                    holder.username_story.text = username
                                    Picasso.get().load(profilepic).placeholder(R.drawable.profile)
                                        .into(holder.story_userpic)
                                    var intent = Intent(applicationContext, StoryActivity::class.java)

                                    intent.putExtra("user_id", userid)
                                    intent.putExtra("counter",0)


                                    holder.itemView.setOnClickListener{
                                        startActivity(intent)
                                    }
                                }

                            }

                        })
                    }
                    else{
                        holder.itemView.visibility = View.GONE
                        var params=holder.itemView.layoutParams
                        params.height=0
                        params.width=0
                        holder.itemView.layoutParams=params
                    }

               }

            })
        }
    }



    firebaseRecyclerAdapter.notifyDataSetChanged()
    story_recyclerview?.adapter=firebaseRecyclerAdapter
    firebaseRecyclerAdapter.startListening()
}

    private fun startStoriesFeatures() {
storyreference?.child(uid)?.addValueEventListener(object :ValueEventListener{
    override fun onCancelled(p0: DatabaseError?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDataChange(p0: DataSnapshot?) {

        if(p0!!.exists()){
   storytype?.text="My story"
            add_story_btn?.visibility=View.GONE
            add_my_story?.setOnClickListener {
                var alertDialog=AlertDialog.Builder(this@MainActivity)
                var options= arrayOf("View Story","Add Story")
                alertDialog.setItems(options,object :DialogInterface.OnClickListener{

                    override fun onClick(dialog: DialogInterface?, which: Int) {
                when(which)
                {
                    0->{
                        var intent=Intent(applicationContext, StoryActivity::class.java)
                        intent.putExtra("user_id",uid)
                        intent.putExtra("counter",0)

                        startActivity(intent)

                    }
                    1->{
                        startActivity(Intent(applicationContext, add_story::class.java))

                    }
                }
                    }

                })

alertDialog.show()
            }
        }
        else{
            storytype?.text="Add story"
            add_story_btn?.visibility=View.VISIBLE
            add_my_story?.setOnClickListener {
                startActivity(Intent(applicationContext, add_story::class.java))
            }
            }
    }

})
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
                commentreference?.child(post_id).addValueEventListener(object :ValueEventListener{
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
                likereference?.child(post_id)?.addValueEventListener(object :ValueEventListener{
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

                postreference?.child(post_id)?.addValueEventListener(object :ValueEventListener{
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
                            userreference?.child(userid)?.addValueEventListener(object :ValueEventListener{
                                override fun onCancelled(p0: DatabaseError?) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                          if(p0!!.exists()){
                          var username=p0?.child("username").value.toString()
                              var profileimage=p0?.child("profileimage").value.toString()
                              friendreference?.child(userid)?.addValueEventListener(object :ValueEventListener{
                                  override fun onCancelled(p0: DatabaseError?) {
                                      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                  }

                                  override fun onDataChange(p0: DataSnapshot?) {
                                      if (p0!!.exists() or userid.equals(firebaseuser?.uid)) {
                                          holder.relativeLayout.visibility=View.VISIBLE
                                          holder.username.text = username
                                          holder.date.text = date
                                          holder.time.text = time
                                          holder.post_desp.text = desp

                                          if(userid.equals(firebaseuser?.uid))
                                          {
                                              holder.edit_option.visibility=View.VISIBLE

                                              holder.edit_option.setOnClickListener {
  var alertDialog=AlertDialog.Builder(this@MainActivity)
                                                  var options= arrayOf("Edit Post","Delete Post")
                                                  alertDialog.setItems(options,object :DialogInterface.OnClickListener{

                                                      override fun onClick(dialog: DialogInterface?, which: Int) {
                                                   when(which){
                                                       0->{
                                                           editPost(post_id,desp)
                                                       }
                                                       1->{
                                                           var dialog=AlertDialog.Builder(this@MainActivity)
                                                           dialog.setMessage("Do you want to delete your post?")
                                                           dialog.setPositiveButton("yes",object :DialogInterface.OnClickListener{
                                                               override fun onClick(dialog: DialogInterface?, which: Int) {
                                                                   deletePost(post_id)
                                                               }

                                                           })

                                                           dialog.setNegativeButton("no",object :DialogInterface.OnClickListener{

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
holder.edit_option.visibility=View.GONE
                                          }
                                          Picasso.get().load(profileimage).placeholder(R.drawable.profile)
                                              .into(holder.profilepic)
                                          Picasso.get().load(postimage).networkPolicy(NetworkPolicy.OFFLINE).into(holder.post_pic,
                                              object :com.squareup.picasso.Callback{
                                                  override fun onSuccess() {
                                                  }

                                                  override fun onError(e: Exception?) {
                                                 Picasso.get().load(postimage).into(holder.post_pic)
                                                  }

                                              }
                                              )
                                          holder.itemView.setOnClickListener{
                                              var intent =Intent(applicationContext,
                                                  ImageInFullSize::class.java)
                                              intent.putExtra("image link",postimage)
                                              startActivity(intent)
                                          }
                                          Picasso.get().load(R.drawable.comment).into(holder.comment_btn)
                                          likereference?.child(post_id)?.child(firebaseuser?.uid)?.addValueEventListener(object :ValueEventListener{
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
                                              var intent = Intent(this@MainActivity, comment::class.java)
                                              intent.putExtra("post_id", post_id)
                                              startActivity(intent)
                                          }


                                          holder.profilepic.setOnClickListener {
                                              var intent=Intent(this@MainActivity,
                                                  user_information::class.java)
                                              intent.putExtra("reciever_id",userid)
                                              startActivity(intent)
                                          }
0
holder.like_counter.setOnClickListener {
    var intent=Intent(this@MainActivity, likes::class.java)
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
                        all_users_post_lists.adapter=firebaseRecyclerAdapter
firebaseRecyclerAdapter.startListening()
    }

    private fun likePost(post_id: String) {
        var today= Date()
        val format = SimpleDateFormat("yyyyMMddHHmmss")
        val date = format.format(today)
        likereference?.child(post_id)?.child(firebaseuser?.uid)?.child("liked_date")?.setValue(date)?.addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {

                if(p0.isSuccessful)
                    Toast.makeText(applicationContext,"You liked the post",Toast.LENGTH_SHORT).show()
            }

        })


    }

    private fun dislikePost(post_id: String) {
likereference?.child(post_id)?.child(firebaseuser?.uid)?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
    override fun onComplete(p0: Task<Void>) {
        if(p0.isSuccessful)
            Toast.makeText(applicationContext,"You disliked the post",Toast.LENGTH_SHORT).show()


    }
})

    }

    private fun deletePost(post_id: String) {
        postreference?.child(post_id).removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful){
                    Toast.makeText(applicationContext,"Post Deleted",Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }
            }

        })
    }
    private fun editPost(post_id:String,post_desp:String) {
        var dialog=AlertDialog.Builder(this)
        dialog.setTitle("Update Post")
        var post_description=EditText(this)
        post_description.setText(post_desp)
        dialog.setView(post_description)
        dialog.setPositiveButton("Update",object :DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                var progressDialog= ProgressDialog(this@MainActivity)
                progressDialog?.setTitle("Updating post")
                progressDialog?.setMessage("Please wait while we are updating your post...")
                progressDialog?.setCanceledOnTouchOutside(false)
                progressDialog?.show()
                postreference?.child(post_id).child("description")?.setValue(post_description.text.toString())?.addOnCompleteListener(object :
                    OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        if(p0.isSuccessful){
                            progressDialog?.dismiss()
                            Toast.makeText(applicationContext,"Post Updated",Toast.LENGTH_SHORT).show()

                        }
                    }

                })
            }

        })
        dialog.setNegativeButton("Cancel",object :DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }

        })
        dialog.show()
    }
    override fun onStart() {

        super.onStart()
        checkUserExistence()
        displayUserPosts()
    }

    private fun sendToLoginActivity() {
        finish()
        startActivity(Intent(applicationContext, login::class.java))
    }
    private fun checkUserExistence(){
        var uid=firebaseuser!!.uid.toString()


        databasereference?.addListenerForSingleValueEvent(valueEventListener)

    }
    var valueEventListener=   object :ValueEventListener{
        override fun onDataChange(p0: DataSnapshot) {
            if(!p0.child(uid).child("fullname").exists())


            {
                var intent = Intent(applicationContext, setup::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }

        override fun onCancelled(p0: DatabaseError) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    private fun sendSetupActivity(){
        startActivity(Intent(applicationContext, setup::class.java))
        finish()
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
