package com.tathagat.postgram

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class FindFriendsAdapter(var context: Context, data:ArrayList<Friends>):RecyclerView.Adapter<FindFriendsAdapter.ViewHolder>() {
    var data = ArrayList<Friends>()

    init {
        this.data = data
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        Picasso.get().load(data[p1].profileimage).placeholder(R.drawable.profile).into(p0.profilepic)
        p0.status.text = data[p1].fullname
        p0.username.text = data[p1].status
        var visitor_user_id=data[p1].user_id
        var dialog=AlertDialog.Builder(context)
        var options= arrayOf("Send Message","View Profile")
        p0.itemView.setOnClickListener {
            var uid=FirebaseAuth.getInstance().currentUser!!.uid
            if(uid.equals(visitor_user_id)){
                var intent = Intent(context, user_information::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("reciever_id", visitor_user_id)
                context.startActivity(intent)
            }
            else {
                dialog.setItems(options, object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        when (p1) {
                            0 -> {
                                var intent = Intent(context, MessageActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.putExtra("reciever_id", visitor_user_id)
                                intent.putExtra("username", data[p1].status)
                                intent.putExtra("profilepic", data[p1].profileimage)
                                context.startActivity(intent)

                            }
                            1 -> {
                                var intent = Intent(context, user_information::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.putExtra("reciever_id", visitor_user_id)
                                context.startActivity(intent)
                            }
                        }
                    }

                })
                dialog.show()
            }
        }

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        var view = LayoutInflater.from(p0.context).inflate(R.layout.users_display_layout, p0, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profilepic = itemView.findViewById<CircleImageView>(R.id.users_profile_image)
        var status = itemView.findViewById<TextView>(R.id.user_status)
        var username = itemView.findViewById<TextView>(R.id.user_profile_name)


    }

    override fun getItemCount(): Int {
        return data.size
    }
}