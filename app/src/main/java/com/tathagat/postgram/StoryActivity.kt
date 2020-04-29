package com.tathagat.postgram

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import jp.shts.android.storiesprogressview.StoriesProgressView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class StoryActivity : AppCompatActivity(),StoriesProgressView.StoriesListener {
    var counter:Int=0

   override fun onComplete() {
  finish()  }

    override fun onPrev() {
        if(counter-1<0) return;
        story_seen_counter?.text=""
        displayStories(--counter)
        addViews(counter)

      }


    override fun onNext() {
        if(counter>=story_ids.size)
            finish()
        story_seen_counter?.text=""
        displayStories(++counter)
addViews(counter)

    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
        storiesProgressView?.destroy()
    }

    override fun onPause() {
        super.onPause()
        storiesProgressView?.pause()
    }


    override fun onRestart() {
        super.onRestart()
        storiesProgressView?.resume()
    }
    var previous:Long=0
var onTouchListener= object :View.OnTouchListener {

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        when (event?.action) {
MotionEvent.ACTION_DOWN->{

    storiesProgressView?.pause()
    user_status?.visibility=View.GONE
    story_desp?.visibility= View.GONE
    edit_story?.visibility=View.GONE
    replystory?.visibility=View.GONE
    previous=System.currentTimeMillis()
}
            MotionEvent.ACTION_UP->
            {
                storiesProgressView?.resume()
                user_status?.visibility=View.VISIBLE
                story_desp?.visibility= View.VISIBLE
                if(uid.equals(userid))
                replystory?.visibility=View.INVISIBLE
                else
                    replystory?.visibility=View.VISIBLE
                if(uid.equals(userid))
                edit_story?.visibility=View.VISIBLE
                else
                    edit_story?.visibility=View.GONE
                var current=System.currentTimeMillis()

                if(current-previous<200)
                {
                v?.performClick()
                }

            }
        }

        return true

    }
}
    var storiesProgressView:StoriesProgressView?=null
    var story_images:ImageView?=null
    var skip:View?=null
    var reverse:View?=null
    var story_ids=ArrayList<String>()
    var story_desp:TextView?=null
    var storyreference:DatabaseReference?=null
    var viewsreference:DatabaseReference?=null
    var edit_story:RelativeLayout?=null
    var user_status:LinearLayout?=null
    var delete_story:ImageView?=null
    var progressBar:ProgressBar?=null
    lateinit var userid:String
    var story_userpic:CircleImageView?=null
    var story_username:TextView?=null
    var userreference:DatabaseReference?=null
    var uid:String=""
    var story_seen_counter:TextView?=null
    var story_view_icon:ImageView?=null
    var replystory:RelativeLayout?=null
    var reply_text:EditText?=null
    var reply_btn:ImageView?=null
    var apiService: APIService?=null
    var senderName:String?=null
   override fun onCreate(savedInstanceState: Bundle?) {
       apiService = Client()
           .getClient("https://fcm.googleapis.com/").create(APIService::class.java)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)
        story_images=findViewById(R.id.story_images)
skip=findViewById(R.id.skip)
       reply_btn=findViewById(R.id.send_story_btn)
       replystory=findViewById(R.id.reply_to_story)
       reply_text=findViewById(R.id.sendstorymessagetext)

        edit_story=findViewById(R.id.edit_story)
        delete_story=findViewById(R.id.delete_story)
user_status=findViewById(R.id.user_info_status)
 story_seen_counter=findViewById(R.id.story_view_counter)
       uid=FirebaseAuth.getInstance().currentUser!!.uid
       var reference=FirebaseDatabase.getInstance().getReference("Users")
       reference.child(uid).addValueEventListener(object :ValueEventListener{
           override fun onCancelled(p0: DatabaseError?) {
               TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
           }

           override fun onDataChange(p0: DataSnapshot?) {
               senderName=p0?.child("username")?.value.toString() }

       })
story_userpic=findViewById(R.id.story_userpic)
       story_username=findViewById(R.id.story_profilename)
        reverse=findViewById(R.id.reverse)
        story_desp=findViewById(R.id.story_text)
        skip?.setOnTouchListener(onTouchListener)
       reverse?.setOnTouchListener(onTouchListener)
       viewsreference=FirebaseDatabase.getInstance().getReference("Story Views")
       story_view_icon=findViewById(R.id.story_views)
        skip?.setOnClickListener{

            storiesProgressView?.skip()
        }
        reverse?.setOnClickListener{
            storiesProgressView?.reverse()
        }

         userid=intent.getStringExtra("user_id")
       reply_btn?.setOnClickListener {
           replyToStory(counter,uid,userid)
       }
       userreference=FirebaseDatabase.getInstance().getReference("Users").child(userid)

        storyreference=FirebaseDatabase.getInstance().getReference("Stories").child(userid)
        var total=""
       counter=intent.getIntExtra("counter",0)
        storyreference?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists()){
                story_ids.clear()
                total=p0?.childrenCount.toString()
                for(x in p0!!.children){
                    if(!x.key.toString().equals("lastupdated"))
                    story_ids.add(x.key.toString())
                }
                    if(counter>=story_ids.size)
                        finish()
                    else {




              storiesProgressView = findViewById(R.id.stories);
              storiesProgressView?.setStoriesCount(story_ids.size)
              storiesProgressView?.setStoryDuration(5000L)
              storiesProgressView?.setStoriesListener(this@StoryActivity)
                       storiesProgressView?.startStories(counter)
                        displayStories(counter)
                        addViews(counter)
                    }
            }
                if(story_ids.size==0||counter>=story_ids.size)
                    finish()
            }

        })
       reply_text!!.setOnFocusChangeListener(object :View.OnFocusChangeListener{
           override fun onFocusChange(p0: View?, p1: Boolean) {
               if(p1)
                storiesProgressView?.pause()
               else
                   storiesProgressView?.resume()
           }

       })

        delete_story?.setOnClickListener {
            deleteStory(counter)}
userDetails()

    }

    private fun replyToStory(counter: Int,messageSenderId:String,messageRecieverId:String) {
        var messagereference = FirebaseDatabase.getInstance().getReference("Messages")
        var text = reply_text?.text.toString().trim()
        if (TextUtils.isEmpty(text))
            Toast.makeText(applicationContext, "Please type some message...", Toast.LENGTH_SHORT).show()
        else {
            if(counter<story_ids.size){
            var story_id = story_ids.get(counter)
            storyreference?.child(story_id)?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    var imagelink = p0?.child("storyimagelink")?.value.toString()
                    var messageinfo = HashMap<String, Any>()
                    val today = Date()
                    val format_date = SimpleDateFormat("MMM dd, yyyy")
                    val format_time = SimpleDateFormat("hh:mm a")
                    val format = SimpleDateFormat("yyyyMMddHHmmss")
                    val date = format_date.format(today)
                    val time = format_time.format(today)
                    var message = text
                    messageinfo.put("date", date)
                    messageinfo.put("time", time)
                    messageinfo.put("message", imagelink)
                    messageinfo.put("from", messageSenderId!!)
                    messageinfo.put("to", messageRecieverId!!)
                    messageinfo.put("type", "$" + message)
                    messageinfo.put("message seen", false)
                    messageinfo.put("story id", story_id)
                    messageinfo.put("story position", counter)
                    messagereference?.child(messageSenderId)?.child(messageRecieverId)?.push()
                    messagereference?.child(messageSenderId)?.child(messageRecieverId)?.push()
                    var ref = messagereference?.child(messageSenderId)?.child(messageRecieverId)?.push()
                    var messagesendedid = ref?.key.toString()
                    ref?.updateChildren(messageinfo)?.addOnCompleteListener(object : OnCompleteListener<Void> {
                        override fun onComplete(p0: Task<Void>) {
                            if (p0.isSuccessful) {
                                messagereference?.child(messageRecieverId)?.child(messageSenderId)
                                    ?.child(messagesendedid)?.updateChildren(messageinfo)
                                    ?.addOnCompleteListener(object : OnCompleteListener<Void> {
                                        override fun onComplete(p0: Task<Void>) {
                                            if (p0.isSuccessful) {
                                                var databasereference = messagereference
                                                databasereference?.child(messageSenderId)?.child(messageRecieverId)
                                                    ?.child("last sent")?.setValue(format.format(today))
                                                databasereference?.child(messageRecieverId)?.child(messageSenderId)
                                                    ?.child("last sent")?.setValue(format.format(today))
                                                generateNotificaction(messageSenderId, messageRecieverId, senderName!!)


                                            } else {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Message delivery failed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                    })
                            }
                        }
                    })

                }

            })

        var intent =Intent(applicationContext, MessageActivity::class.java)
        intent.putExtra("reciever_id",messageRecieverId)
        startActivity(intent)
        finish()}
    }}
    private fun addViews(counter: Int){
        if(!uid.equals(userid)){
            var storyid=story_ids.get(counter)
            val today = Date()
            val format = SimpleDateFormat("yyyyMMddHHmmss")
            val filename = format.format(today)
            var storymap=HashMap<String,Any>()
            storymap.put("seen",true)
            storymap.put("time",filename)
            viewsreference?.child(storyid)?.child(uid)?.updateChildren(storymap)
        }

    }
private fun deleteStory(counter: Int) {
    var res=false
 if(story_ids.size==1)
     res=true
    storiesProgressView?.pause()
    var alertdialog = AlertDialog.Builder(this)
    alertdialog.setCancelable(false)
    alertdialog.setMessage("Do you want to delete your story update? It will be deleted for everyone who recieved it.")
    alertdialog.setPositiveButton("Delete", object : DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface?, which: Int) {
            var storyid=story_ids.get(counter)
            if(story_ids.size==1)
                storyreference?.child("lastupdated")?.removeValue()
            storyreference?.child(storyid)?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    if(p0.isSuccessful){
                        Toast.makeText(applicationContext,"Story Deleted",Toast.LENGTH_SHORT).show()
                        viewsreference?.child(storyid)?.removeValue()
                        if(res) {

                            story_seen_counter?.text=""
                            finish()
                        }
                storiesProgressView?.resume()
                    }


                }

            })
        }


    })
    alertdialog.setNegativeButton("No",object :DialogInterface.OnClickListener{

        override fun onClick(dialog: DialogInterface?, which: Int) {
      storiesProgressView?.resume()  }

    })
    alertdialog.show()
}
private fun displayStories(counter:Int){
var firebaseuser=FirebaseAuth.getInstance().currentUser
    uid=firebaseuser!!.uid
    if(!uid!!.equals(userid)) {
        edit_story?.visibility = View.GONE
        replystory?.visibility=View.VISIBLE
    }
    else {
        edit_story?.visibility = View.VISIBLE
        replystory?.visibility=View.INVISIBLE
    }
var story_id=story_ids.get(counter)
    storyreference?.child(story_id)?.addValueEventListener(object :ValueEventListener{
        override fun onCancelled(p0: DatabaseError?) {

        }

        override fun onDataChange(p0: DataSnapshot?) {
            var imagelink=p0?.child("storyimagelink")?.value.toString()
story_desp?.text=p0?.child("storydescription")?.value?.toString()
            viewsreference?.child(story_id)?.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
             story_seen_counter?.text=p0?.childrenCount.toString()
                }

            })
            Picasso.get().load(imagelink).into(story_images)
            story_view_icon?.setOnClickListener {
                var intent= Intent(this@StoryActivity, Story_Views::class.java)
                intent.putExtra("story_id",story_id)
                intent.putExtra("user_id",userid)
                startActivity(intent)
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
                    username + " has replied to your story ",
                    "New Message",
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
    lateinit var usernme:String
    private fun userDetails(){
        userreference?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
            usernme=p0?.child("username")?.value.toString()
                var pic=p0?.child("profileimage")?.value.toString()
                if(userid.equals(uid))
                    story_username?.text="You"
                else
                story_username?.text=usernme
                Picasso.get().load(pic).placeholder(R.drawable.profile).into(story_userpic)

            }
        })
    }
}
