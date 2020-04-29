package com.tathagat.postgram

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Groups : AppCompatActivity() {
var mToolbar:Toolbar?=null
    var query:Query?=null
    var groupreference:DatabaseReference?=null
    var groupdetailsreference:DatabaseReference?=null
    var mRecyclerView:RecyclerView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        mToolbar=findViewById(R.id.groupToolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(
            true
        )
        supportActionBar?.setTitle("Groups")
        var uid=FirebaseAuth.getInstance().currentUser?.uid
        groupreference=FirebaseDatabase.getInstance().getReference("Groups")
        groupdetailsreference=FirebaseDatabase.getInstance().getReference("Groups Details")
        query=groupreference?.orderByChild("last sent time:")
        mRecyclerView=findViewById(R.id.group_recyclerView)
        var linearLayoutManager= LinearLayoutManager(this)
        linearLayoutManager.reverseLayout=true
        linearLayoutManager.stackFromEnd=true
        mRecyclerView?.layoutManager=linearLayoutManager
        displayGroups()
    }

    private fun displayGroups() {
        val option = FirebaseRecyclerOptions.Builder<myFriends>()
            .setQuery(query!!, myFriends::class.java)
            .build()
        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<myFriends, MyViewHolder>(option) {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val itemView =
                    LayoutInflater.from(applicationContext).inflate(R.layout.group_layout, parent, false)
                return MyViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: myFriends) {
                var groupid = getRef(position).key.toString()

                groupreference?.child(groupid)?.addValueEventListener(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        var uid=FirebaseAuth.getInstance().currentUser?.uid
                        var groupimage = p0?.child("Group Image")?.value.toString()
                        var groupname = p0?.child("Group Name")?.value.toString()
                        var last_message=p0?.child("last sent message:")?.value.toString()
                        var sender_id=p0?.child("last sent by:")?.value.toString()
                        Picasso.get().load(groupimage).placeholder(R.drawable.group_icon).into(holder.grouppic)
                        holder?.groupname.text=groupname
                        groupdetailsreference?.child(uid)?.child(groupid)?.addValueEventListener(object :ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {

                            }

                            override fun onDataChange(p0: DataSnapshot?) {
                           if(p0!!.exists()){


                               var ref=FirebaseDatabase.getInstance().getReference("Users").child(sender_id)
                               ref.addValueEventListener(object :ValueEventListener{
                                   override fun onCancelled(p0: DatabaseError?) {
                                       TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                   }

                                   override fun onDataChange(p0: DataSnapshot?) {
                                       if(p0!!.exists()){
                                           var username=p0?.child("username")?.value.toString()
                                           if(last_message.length>30){
                                               last_message=last_message.substring(0,25)+"..."
                                           }
                                           var uid=FirebaseAuth.getInstance().currentUser?.uid
                                           if(sender_id==uid)
                                               holder.lastmessage.text="(You)"+": "+last_message
                                           else
                                               holder.lastmessage.text="("+username+")"+": "+last_message


                                       }
                                   }

                               })

holder?.grouppic?.setOnClickListener {
    var intent = Intent(applicationContext, ImageInFullSize::class.java)
    intent.putExtra("image link", groupimage)
    startActivity(intent)
}
                               holder?.itemView.setOnClickListener {
                                   var intent = Intent(applicationContext, GroupChatActivity::class.java)
                                   intent.putExtra("group_id", groupid)
                                   startActivity(intent)
                               }

                           }
                                else{
                               holder.itemView.visibility=View.GONE
                               var params=holder.itemView.layoutParams
                               params.height=0
                               params.width=0
                               holder.itemView.layoutParams=params
                           }
                            }

                        })

                    }
                })
            }
        }
        firebaseRecyclerAdapter.notifyDataSetChanged()
        mRecyclerView?.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }

    override fun onCreateOptionsMenu(menu: Menu):Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_group, menu);
        return true;
    }


    override fun onOptionsItemSelected(item:MenuItem):Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml

        //noinspection SimplifiableIfStatement
        if(item?.itemId==android.R.id.home)
            finish()
        if (item.itemId== R.id.create_group) {
            var dialog= AlertDialog.Builder(this)
            dialog.setTitle("Create Group")
            var group_name= EditText(this)
            dialog.setView(group_name)
            dialog.setPositiveButton("Create",object :DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    createGroup(group_name.text.toString())
                }
            })
            dialog.show()
        }

        return super.onOptionsItemSelected(item);
    }
    fun createGroup(group_name:String){

       var  groupreference=FirebaseDatabase.getInstance().getReference("Groups").push()
        var progressDialog= ProgressDialog(this)
        progressDialog?.setTitle("Creating")
        progressDialog?.setMessage("Please wait while we are creating group...")
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.show()
        var groupid:String=""
        groupreference?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                groupid=p0?.key.toString()
            }

        })
        var uid=FirebaseAuth.getInstance().currentUser?.uid
        groupreference?.child("Group Name")?.setValue(group_name)?.addOnCompleteListener(object :OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful){
                    groupreference?.child("Group Members")?.child(uid)?.child(uid)?.setValue(uid)
                    groupdetailsreference?.child(uid)?.child(groupid)?.setValue(groupid)
Toast.makeText(applicationContext,"Group created",Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()
                }
                else{
                    Toast.makeText(applicationContext,p0.exception?.message.toString(),Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()
                }
            }

        })
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        var grouppic = itemView!!.findViewById<CircleImageView>(R.id.group_image)
        var groupname = itemView!!.findViewById<TextView>(R.id.group_name)
        var lastmessage=itemView!!.findViewById<TextView>(R.id.message_desp_group)


    }

}
