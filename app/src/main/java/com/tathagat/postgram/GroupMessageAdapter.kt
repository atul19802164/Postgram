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


class GroupMessageAdapter(var context: Context, messagesList:ArrayList<Messages>): RecyclerView.Adapter<GroupMessageAdapter.ViewHolder>() {
    var messagesList = ArrayList<Messages>()
    var uid = FirebaseAuth.getInstance().currentUser?.uid
    var userreference = FirebaseDatabase.getInstance().getReference("Users")
    var groupmessagereference = FirebaseDatabase.getInstance().getReference("Group Messages")
    var total_sender: Long = 0
    var total_reciever: Long = 0


    init {
        this.messagesList = messagesList

    }

    override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
        var message = messagesList.get(p1)
        var fromUserId = message.from
        var groupid=message.to
        var text = message.text
        var messageId = message.messageid
        var type = message.type
        var username=""
        var link=""
        var today = Date()
        val format_date = SimpleDateFormat("MMM dd, yyyy")
        val now = format_date.format(today)
        holder.reciever_name?.setOnClickListener {
var builder=AlertDialog.Builder(context)
            var options= arrayOf(username+"'s Profile","Send "+username+ " Message")
            builder.setItems(options,object: DialogInterface.OnClickListener{

                override fun onClick(dialog: DialogInterface?, which: Int) {
                    when(which){
                        0->{
                            var intent= Intent(context, user_information::class.java)
                            intent.putExtra("reciever_id",fromUserId)
                            context.startActivity(intent)
                        }
                        1->{
                            var intent =Intent(context, MessageActivity::class.java)
                            intent.putExtra("reciever_id",fromUserId)
                            intent.putExtra("username",username)
                            intent.putExtra("profilepic",link)
                            context.startActivity(intent)

                        }


                    }

                }

            })
            builder.show()
        }
        if (uid.equals(fromUserId)) {
            holder.reciever.visibility = View.GONE
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
                                    deleteMessageForEveryone(groupid, messageId)
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
                                    deleteMessageForEveryone(groupid,messageId)
                                }
                            }
                        }

                    })
                    alertDialog.show()
                }

                true
            }
            if(type.equals("text")){
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
                holder.sender_message_image.visibility=View.GONE
                holder.reciever_message_image.visibility=View.GONE
                holder.reciever_message_image.visibility=View.GONE

        }

            else{
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
                holder.sender.visibility = View.VISIBLE
                holder.reciever.visibility = View.GONE
                holder.reciever_message_image.visibility = View.GONE

            }
        } else {
            userreference?.child(fromUserId)?.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {
                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0!!.exists()){
                        username=p0?.child("username")?.value.toString()
                        holder.reciever_name.text=username
                        holder.reciever_name_image.text=username
                    }
                  }

            })
            if (type.equals("text")) {
                var alertDialog = AlertDialog.Builder(context)
                if (type.equals("text")) {
                    holder.itemView!!.setOnClickListener {
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
                }
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
                holder.reciever_message_image.visibility=View.GONE
                holder.reciever.visibility = View.VISIBLE
            }
            else{
                holder.itemView.setOnClickListener {
                    var intent = Intent(context, ImageInFullSize::class.java)
                    intent.putExtra("image link", message.text)
                    context.startActivity(intent)
                }
                var text = message.text
                Picasso.get().load(text).into(holder.reciever_image)
                if (message.date.equals(now))
                    holder.sender_date_image.text = message.time
                else
                    holder.reciever_date_image.text = message.time + " " + message.date
                holder.sender_message_image.visibility = View.GONE
                holder.sender.visibility = View.GONE
                holder.reciever.visibility = View.GONE
                holder.reciever_message_image.visibility = View.VISIBLE
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

    private fun deleteMessageForEveryone(groupid: String, messageId: String) {
        groupmessagereference?.child(groupid)?.child(messageId)?.removeValue()?.addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful)
                    Toast.makeText(context,"Message Deleted",Toast.LENGTH_SHORT).show()
            }

        })



    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        var view = LayoutInflater.from(p0.context).inflate(R.layout.custom_messages_group, p0, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var reciever_text=itemView.findViewById<TextView>(R.id.reciever_message_text_group)
        var sender_text=itemView.findViewById<TextView>(R.id.sender_message_text_group)
        var sender_date=itemView.findViewById<TextView>(R.id.sender_date_group)
        var reciever_date=itemView.findViewById<TextView>(R.id.reciever_date_group)
        var sender=itemView.findViewById<LinearLayout>(R.id.sender_message_group)
        var reciever=itemView.findViewById<LinearLayout>(R.id.reciever_message_group)
        var reciever_name=itemView.findViewById<TextView>(R.id.reciever_name_group)
        var sender_message_image=itemView.findViewById<LinearLayout>(R.id.sender_message_image_group)
        var sender_image=itemView.findViewById<ImageView>(R.id.sender_image_group)
        var sender_date_image=itemView.findViewById<TextView>(R.id.sender_date_image_group)
        var reciever_message_image=itemView.findViewById<LinearLayout>(R.id.reciever_message_image_group)
        var reciever_image=itemView.findViewById<ImageView>(R.id.reciever_image_group)
        var reciever_date_image=itemView.findViewById<TextView>(R.id.reciever_date_image_group)
        var reciever_name_image=itemView.findViewById<TextView>(R.id.reciever_name_message_group)


    }

    override fun getItemCount(): Int {
        return messagesList.size
    }
}