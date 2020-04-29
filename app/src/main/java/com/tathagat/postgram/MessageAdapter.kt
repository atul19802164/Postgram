package com.tathagat.postgram

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import tcking.github.com.giraffeplayer2.VideoInfo
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.*
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import java.util.regex.Pattern


class MessageAdapter(var context: Context, messagesList:ArrayList<Messages>): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    var messagesList = ArrayList<Messages>()
    var uid = FirebaseAuth.getInstance().currentUser?.uid
    var userreference = FirebaseDatabase.getInstance().getReference("Users")
    var messagereference = FirebaseDatabase.getInstance().getReference("Messages")
    var total_sender: Long = 0
    var total_reciever: Long = 0

    init {
        this.messagesList = messagesList

    }

    override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
        var message = messagesList.get(p1)
        var fromUserId = message.from
        var toUserId = message.to
        var messageId = message.messageid
        var type = message.type
        var today = Date()
        messagereference.child(fromUserId).child(toUserId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                total_sender = p0!!.childrenCount
            }

        })
        messagereference.child(toUserId).child(fromUserId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                total_reciever = p0!!.childrenCount
            }

        })
        val format_date = SimpleDateFormat("MMM dd, yyyy")
        val now = format_date.format(today)
        if (uid.equals(fromUserId)) {
            holder.reciever_story_message.visibility=View.GONE
            holder.itemView.setOnLongClickListener {
                var alertDialog = AlertDialog.Builder(context)
                if (type.equals("text")) {
                    var options = arrayOf("Copy text", "Delete message")
                    alertDialog.setItems(options, object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            when (p1) {
                                0 -> {
                                    copyToClipBoard(message.text)
                                }
                                1 -> {
                                    deleteMessageForEveryone(fromUserId, toUserId, messageId)
                                }
                            }
                        }

                    })
                    alertDialog.show()

                } else {
                    var options = arrayOf("Delete message")
                    alertDialog.setItems(options, object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            when (p1) {
                                0 -> {
                                    deleteMessageForEveryone(fromUserId, toUserId, messageId)
                                }
                            }
                        }

                    })
                    alertDialog.show()
                }
                true
            }
            if (type.equals("text")) {
                holder.message_seen_story.visibility=View.GONE
                holder.sender_story_message.visibility=View.GONE
                var text = message.text
                holder.reciever_message_video.visibility = View.GONE
                holder.sender_message_video.visibility = View.GONE
                if (text.startsWith("https://") || text.isPhoneNumber()) {
                    var ss = SpannableString(text)
                    var clickableSpan = object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (text.startsWith("https://")) {
                                var i = Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(text));
                                context.startActivity(i);
                            }
                            if (text.isPhoneNumber()) {
                                var i = Intent(Intent.ACTION_DIAL);
                                var phone = "tel:" + text
                                i.setData(Uri.parse(phone));
                                context.startActivity(i);
                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false
                            ds.color = Color.BLUE
                        }

                    }
                    ss.setSpan(clickableSpan, 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                    holder.sender_text.text = ss
                    holder.sender_text.movementMethod = LinkMovementMethod.getInstance()
                } else
                    holder.sender_text.text = text
                if (message.date.equals(now))
                    holder.sender_date.text = message.time
                else
                    holder.sender_date.text = message.time + " " + message.date
                holder.sender.visibility = View.VISIBLE
                holder.reciever.visibility = View.GONE
                holder.reciever_pic.visibility = View.GONE
                holder.reciever_message_image.visibility = View.GONE
                holder.sender_message_image.visibility = View.GONE
                holder.message_seen_image.visibility = View.GONE
                holder.message_seen_video.visibility = View.GONE
                if (p1 == messagesList.size - 1) {
                    if (message.isseen.equals("true"))
                        holder.message_seen.text = "seen"
                    else
                        holder.message_seen.text = "delivered"
                    holder.message_seen.visibility = View.VISIBLE

                }
                if (p1 < messagesList.size - 1)
                    holder.message_seen.visibility = View.GONE
            } else {
                if (type.equals("image")) {
                    holder.message_seen_story.visibility=View.GONE

                    holder.sender_story_message.visibility=View.GONE
                    holder.itemView.setOnClickListener {
                        var intent = Intent(context, ImageInFullSize::class.java)
                        intent.putExtra("image link", message.text)
                        context.startActivity(intent)
                    }
                    var text = message.text
                    Picasso.get().load(text).into(holder.sender_image)
                    if (message.date.equals(now))
                        holder.sender_date_image.text = message.time
                    else
                        holder.sender_date_image.text = message.time + " " + message.date
                    holder.sender_message_image.visibility = View.VISIBLE
                    holder.sender.visibility = View.GONE
                    holder.reciever.visibility = View.GONE
                    holder.reciever_pic.visibility = View.GONE
                    holder.reciever_message_image.visibility = View.GONE
                    holder.message_seen.visibility = View.GONE
                    holder.sender_date.visibility = View.GONE
                    holder.sender_date_image.visibility = View.VISIBLE
                    holder.reciever_message_video.visibility = View.GONE
                    holder.sender_message_video.visibility = View.GONE
                    holder.message_seen_video.visibility = View.GONE
                    holder.message_seen.visibility = View.GONE

                    if (p1 == messagesList.size - 1) {
                        if (message.isseen.equals("true"))
                            holder.message_seen_image.text = "seen"
                        else
                            holder.message_seen_image.text = "delivered"
                        holder.message_seen_image.visibility = View.VISIBLE

                    }
                    if (p1 < messagesList.size - 1)
                        holder.message_seen_image.visibility = View.GONE

                } else
                    if (type.equals("video")) {
                        holder.message_seen_story.visibility=View.GONE

                        holder.sender_story_message.visibility=View.GONE
                        var text = message.text
                        holder.reciever_message_image.visibility = View.GONE
                        holder.reciever.visibility = View.GONE
                        holder.sender.visibility = View.GONE
                        holder.reciever_pic.visibility = View.GONE
                        if (message.date.equals(now))
                            holder.sender_date_video.text = message.time
                        else
                            holder.sender_date_video.text = message.time + " " + message.date
                        holder.sender_message_image.visibility = View.GONE
                        holder.message_seen_image.visibility = View.GONE
                        holder.sender_message_image.visibility = View.GONE
                        holder.message_seen_image.visibility = View.GONE
                        holder.message_seen.visibility = View.VISIBLE
                        holder.reciever_message_video.visibility = View.GONE
                        holder.sender_message_video.visibility = View.VISIBLE
                        holder.message_seen.visibility = View.GONE
                        holder.message_seen_image.visibility = View.GONE
                        holder.sender_video.videoInfo.setAspectRatio(VideoInfo.AR_4_3_FIT_PARENT)
                            .setBgColor(Color.BLACK)
                            .setShowTopBar(false)
                            .setPortraitWhenFullScreen(false)
                        holder.sender_video.setVideoPath(text)
                        holder.sender_video.visibility = View.VISIBLE
                        if (p1 == messagesList.size - 1) {
                            if (message.isseen.equals("true"))
                                holder.message_seen_video.text = "seen"
                            else
                                holder.message_seen_video.text = "delivered"
                            holder.message_seen_video.visibility = View.VISIBLE
                        }
                        if (p1 < messagesList.size - 1)
                            holder.message_seen_video.visibility = View.GONE

                    }
                else

                    {
                        if(type.startsWith("$")) {
                            holder.reciever_message_video.visibility = View.GONE
                            holder.sender_message_video.visibility = View.GONE
                            holder.sender.visibility = View.GONE
                            holder.reciever.visibility = View.GONE
                            holder.reciever_pic.visibility = View.GONE
                            if (message.date.equals(now))
                                holder.sender_date_story.text = message.time
                            else
                                holder.sender_date_story.text = message.time + " " + message.date
                            holder.reciever_message_image.visibility = View.GONE
                            holder.sender_message_image.visibility = View.GONE
                            holder.message_seen_image.visibility = View.GONE
                            holder.message_seen_video.visibility = View.GONE
                            holder.sender_story_message.visibility = View.VISIBLE
                            holder.sender_story_text.text=type.substring(1,type.length)
                            Picasso.get().load(message.text).into(holder.sender_story_image)
                            var storyreference=FirebaseDatabase.getInstance().getReference("Stories").child(message.to)
                                .child(message.story_id)
                            storyreference.addValueEventListener(object :ValueEventListener{
                                override fun onCancelled(p0: DatabaseError?) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                                    if(p0!!.exists()){
                                        var intent = Intent(context, StoryActivity::class.java)
                                        intent.putExtra("user_id", message.to)
                                        intent.putExtra("counter",Integer.parseInt(message.story_position))
                                        holder.itemView.setOnClickListener{
                                            context.startActivity(intent)
                                        }
                                    }
                                 }

                            })
                            if (p1 == messagesList.size - 1) {
                                if (message.isseen.equals("true"))
                                    holder.message_seen_story.text = "seen"
                                else
                                    holder.message_seen_story.text = "delivered"
                                holder.message_seen_story.visibility = View.VISIBLE

                            }
                            if (p1 < messagesList.size - 1)
                                holder.message_seen_story.visibility = View.GONE
                        }
                                            else{

                            holder.itemView.visibility = View.GONE
                            var params=holder.itemView.layoutParams
                            params.height=0
                        }
                    }
            }

        } else {
            holder.message_seen_story.visibility=View.GONE
            holder.itemView.setOnLongClickListener {
                var alertDialog = AlertDialog.Builder(context)
                if (type.equals("text")) {
                    var options = arrayOf("Copy text")
                    alertDialog.setItems(options, object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            when (p1) {
                                0 -> {
                                    copyToClipBoard(message.text)
                                }

                            }
                        }

                    })
                    alertDialog.show()

                }

                true
            }
            var pic: String? = null
            userreference.child(fromUserId).addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.exists()) {
                        pic = p0?.child("profileimage").value.toString()
                        Picasso.get().load(pic).placeholder(R.drawable.profile).into(holder.reciever_pic)

                    }

                }

            })

            if (type.equals("text")) {
                var text = message.text
                if (text.startsWith("https://") || text.isPhoneNumber()) {
                    var ss = SpannableString(text)
                    var clickableSpan = object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            if (text.startsWith("https://")) {
                                var i = Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(text));
                                context.startActivity(i);
                            }
                            if (text.isPhoneNumber()) {
                                var i = Intent(Intent.ACTION_DIAL);
                                var phone = "tel:" + text
                                i.setData(Uri.parse(phone));
                                context.startActivity(i);
                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false
                            ds.color = Color.BLUE
                        }

                    }
                    ss.setSpan(clickableSpan, 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                    holder.reciever_text.text = ss
                    holder.reciever_text.movementMethod = LinkMovementMethod.getInstance()
                } else
                    holder.reciever_text.text = text
                if (message.date.equals(now))
                    holder.reciever_date.text = message.time
                else
                    holder.reciever_date.text = message.time + " " + message.date
                holder.reciever_date.visibility = View.VISIBLE
                holder.sender.visibility = View.GONE
                holder.sender_message_image.visibility = View.GONE
                holder.sender_message_video.visibility = View.GONE
                holder.message_seen_image.visibility = View.GONE
                holder.sender_message_image.visibility = View.GONE
                holder.message_seen_image.visibility = View.GONE
                holder.message_seen.visibility = View.GONE
                holder.reciever_pic.visibility = View.VISIBLE
                holder.reciever_story_message.visibility=View.GONE
                holder.sender_story_message.visibility=View.GONE
                holder.reciever_message_video.visibility = View.GONE
                holder.sender_message_video.visibility = View.GONE
                holder.reciever_message_image.visibility = View.GONE
                holder.reciever.visibility = View.VISIBLE
            } else {
                if (type.equals("image")) {
                    holder.itemView.setOnClickListener {
                        var intent = Intent(context, ImageInFullSize::class.java)
                        intent.putExtra("image link", message.text)
                        context.startActivity(intent)
                    }
                    var text = message.text
                    Picasso.get().load(text).into(holder.reciever_image)
                    if (message.date.equals(now))
                        holder.reciever_date_image.text = message.time
                    else
                        holder.reciever_date_image.text = message.time + " " + message.date
                    holder.message_seen_image?.visibility = View.GONE
                    holder.reciever_message_image.visibility = View.VISIBLE
                    holder.sender.visibility = View.GONE
                    holder.reciever_story_message.visibility=View.GONE
                    holder.sender_story_message.visibility=View.GONE
                    holder.sender_message_image.visibility = View.GONE
                    holder.sender_message_video.visibility = View.GONE
                    holder.reciever.visibility = View.GONE
                    holder.reciever_pic.visibility = View.VISIBLE
                    holder.reciever_message_image.visibility = View.VISIBLE
                    holder.message_seen.visibility = View.GONE
                    holder.sender_date.visibility = View.GONE
                    holder.sender_date_image.visibility = View.GONE
                    holder.reciever_message_video.visibility = View.GONE
                    holder.message_seen_video.visibility = View.GONE


                } else {
                    if (type.equals("video")) {
                        var text = message.text
                        holder.reciever_message_image.visibility = View.GONE
                        holder.reciever.visibility = View.GONE
                        holder.sender.visibility = View.GONE
                        holder.reciever_pic.visibility = View.GONE
                        if (message.date.equals(now))
                            holder.reciever_date_video.text = message.time
                        else
                            holder.reciever_date_video.text = message.time + " " + message.date
                        holder.sender_message_image.visibility = View.GONE
                        holder.message_seen_image.visibility = View.GONE
                        holder.sender_message_image.visibility = View.GONE
                        holder.sender_message_video.visibility = View.GONE
                        holder.message_seen_video.visibility = View.GONE
                        holder.message_seen_image.visibility = View.GONE
                        holder.message_seen.visibility = View.GONE
                        holder.reciever_message_video.visibility = View.VISIBLE
                        holder.reciever_pic.visibility = View.VISIBLE
                        holder.reciever_story_message.visibility=View.GONE
                        holder.sender_story_message.visibility=View.GONE
                        holder.message_seen.visibility = View.GONE
                        holder.message_seen_image.visibility = View.GONE
                        holder.reciever_video.videoInfo.setAspectRatio(VideoInfo.AR_4_3_FIT_PARENT)
                            .setBgColor(Color.BLACK)
                            .setShowTopBar(false)
                            .setPortraitWhenFullScreen(false)
                        holder.reciever_video.setVideoPath(text)

                    }
                    else{
                        if(type.startsWith("$")) {
                            holder.reciever_message_video.visibility = View.GONE
                            holder.sender_message_video.visibility = View.GONE
                            holder.sender.visibility = View.GONE
                            holder.reciever.visibility = View.GONE
                            holder.reciever_pic.visibility = View.VISIBLE
                            holder.reciever_message_image.visibility = View.GONE
                            holder.sender_message_image.visibility = View.GONE
                            holder.message_seen_image.visibility = View.GONE
                            holder.message_seen_video.visibility = View.GONE
                            if (message.date.equals(now))
                                holder.reciever_date_story.text = message.time
                            else
                                holder.reciever_date_story.text = message.time + " " + message.date
                            holder.sender_story_message.visibility = View.GONE
                            holder.reciever_story_message.visibility=View.VISIBLE
                            holder.reciever_story_text.text=type.substring(1,type.length)
                            Picasso.get().load(message.text).into(holder.reciever_story_image)
                            var storyreference=FirebaseDatabase.getInstance().getReference("Stories").child(message.to)
                                .child(message.story_id)
                            storyreference.addValueEventListener(object :ValueEventListener{
                                override fun onCancelled(p0: DatabaseError?) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot?) {
                                    if(p0!!.exists()){
                                        var intent = Intent(context, StoryActivity::class.java)
                                        intent.putExtra("user_id", message.to)
                                        intent.putExtra("counter",Integer.parseInt(message.story_position))
                                        holder.itemView.setOnClickListener{
                                            context.startActivity(intent)
                                        }
                                    }
                                }

                            })
                        }
                        else {
                            holder.sender_story_message.visibility = View.GONE
                            holder.itemView.visibility = View.GONE
                            var params = holder.itemView.layoutParams
                            params.height = 0
                            holder.itemView.layoutParams = params
                        }}

                }
            }


        }

    }


    val REG = "^(\\+91[\\-\\s]?)?[0]?(91)?[789]\\d{9}\$"
    var PATTERN: Pattern = Pattern.compile(REG)
    fun CharSequence.isPhoneNumber() : Boolean = PATTERN.matcher(this).find()
    private fun copyToClipBoard(text: String) {
        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("tect", text)
        clipboard!!.setPrimaryClip(clip)
        Toast.makeText(context,"Text Copied",Toast.LENGTH_SHORT).show()

    }

    private fun deleteMessageForEveryone(fromUserId: String, toUserId: String, messageId: String) {

        messagereference.child(fromUserId).child(toUserId).child(messageId).removeValue().addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful){
                    messagereference.child(toUserId).child(fromUserId).child(messageId).removeValue().addOnCompleteListener(object :OnCompleteListener<Void>{
                        override fun onComplete(p0: Task<Void>) {
                            if(p0.isSuccessful) {
                                Toast.makeText(context, "Message deleted.", Toast.LENGTH_SHORT).show()
                                if(total_sender.toString().equals("1"))
                                    messagereference.child(fromUserId).child(toUserId).child("last sent").removeValue()
                                if(total_reciever.toString().equals("1"))
                                    messagereference.child(toUserId).child(fromUserId).child("last sent").removeValue()


                            }
                        }
                    })

                }
            }
        })

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        var view = LayoutInflater.from(p0.context).inflate(R.layout.custom_messages_layout, p0, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
       var reciever_pic=itemView.findViewById<CircleImageView>(R.id.message_profile_image)
        var reciever_text=itemView.findViewById<TextView>(R.id.reciever_message_text)
        var sender_text=itemView.findViewById<TextView>(R.id.sender_message_text)
        var sender_date=itemView.findViewById<TextView>(R.id.sender_date)
        var reciever_date=itemView.findViewById<TextView>(R.id.reciever_date)
        var sender=itemView.findViewById<LinearLayout>(R.id.sender_message)
        var reciever=itemView.findViewById<LinearLayout>(R.id.reciever_message)
        var message_seen=itemView.findViewById<TextView>(R.id.message_seen)
        var sender_message_image=itemView.findViewById<LinearLayout>(R.id.sender_message_image)
        var sender_image=itemView.findViewById<ImageView>(R.id.sender_image)
        var sender_date_image=itemView.findViewById<TextView>(R.id.sender_date_image)
        var message_seen_image=itemView.findViewById<TextView>(R.id.message_seen_image)
        var reciever_message_image=itemView.findViewById<LinearLayout>(R.id.reciever_message_image)
        var reciever_image=itemView.findViewById<ImageView>(R.id.reciever_image)
        var reciever_date_image=itemView.findViewById<TextView>(R.id.reciever_date_image)
        var sender_message_video=itemView.findViewById<LinearLayout>(R.id.sender_message_video)
        var sender_date_video=itemView.findViewById<TextView>(R.id.sender_date_video)
        var sender_video=itemView.findViewById<tcking.github.com.giraffeplayer2.VideoView>(R.id.sender_video)
        var message_seen_video=itemView.findViewById<TextView>(R.id.message_seen_video)
        var reciever_video=itemView.findViewById<tcking.github.com.giraffeplayer2.VideoView>(R.id.reciever_video)
        var reciever_message_video=itemView.findViewById<LinearLayout>(R.id.reciever_message_video)
        var reciever_date_video=itemView.findViewById<TextView>(R.id.reciever_date_video)
        var sender_story_message=itemView.findViewById<LinearLayout>(R.id.sender_message_story)
        var sender_story_image=itemView.findViewById<ImageView>(R.id.sender_message_storypic)
        var sender_story_text=itemView.findViewById<TextView>(R.id.sender_story_message)
        var reciever_story_message=itemView.findViewById<LinearLayout>(R.id.reciever_message_story)
        var reciever_story_image=itemView.findViewById<ImageView>(R.id.reciever_message_storypic)
        var reciever_story_text=itemView.findViewById<TextView>(R.id.reciever_story_message)
        var reciever_date_story=itemView.findViewById<TextView>(R.id.reciever_date_story)
        var sender_date_story=itemView.findViewById<TextView>(R.id.sender_date_story)
        var message_seen_story=itemView.findViewById<TextView>(R.id.message_seen_story)

    }

    override fun getItemCount(): Int {
        return messagesList.size
    }
}