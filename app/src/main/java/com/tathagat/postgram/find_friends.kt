package com.tathagat.postgram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

import android.widget.SearchView

class find_friends : AppCompatActivity() {
lateinit var recyclerView:RecyclerView
    var mToolbar:Toolbar?=null
    var show_progress:ProgressBar?=null
    var ref:DatabaseReference?=null
    lateinit var message:TextView
    lateinit var nothing_found:LinearLayout
var details=ArrayList<Friends>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)
        mToolbar=findViewById(R.id.find_friends_toolbar)
        recyclerView=findViewById(R.id.find_friends_recycler_view)
        show_progress=findViewById(R.id.find_friend_progress)
        message=findViewById(R.id.message)
        nothing_found=findViewById<LinearLayout>(R.id.nothing_found)
        setSupportActionBar(mToolbar)
        recyclerView?.layoutManager = GridLayoutManager(applicationContext,1)
        ref=FirebaseDatabase.getInstance().getReference("Users")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Find Friends")
        ref?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                var friends=ArrayList<Friends>()
                if(p0.exists()){
                    for(x in p0.children) {
                        var user_id=x.key.toString()
var profilename=x.child("fullname").value.toString()
                         var profileimage=x.child("profileimage").value.toString()
                       var status=x.child("username").value.toString()
                        friends.add(Friends(profileimage, profilename, status, user_id))


                    }
                    details=friends
                    if(friends.size>0){

                        show_progress?.visibility=View.GONE
                    }

                }

          }

        })

    }

        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.search_menu,menu)

            var menuItem:MenuItem=menu!!.findItem(R.id.search_friends)
            var searchView=menuItem.actionView as SearchView
            searchView.queryHint="Find Friends"
            searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    message.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    if (details.size == 0)
                        show_progress?.visibility = View.VISIBLE
                } else {
                    message.visibility = View.VISIBLE
                    nothing_found.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    show_progress?.visibility = View.GONE
                }
            }
            searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(p0: String?): Boolean {


                    if(details.size!=0)
                        show_progress?.visibility=View.GONE

                    var result = ArrayList<Friends>()
                    if(!p0.equals("")) {
                        show_progress?.visibility = View.VISIBLE
                        var str = p0?.toLowerCase()

                        for (x in details) {
                            if (x.fullname.toLowerCase().contains(str!!)||x.status.toLowerCase().contains(str!!)) {
                                result.add(x)

                            }
                        }

                        if (result.size == 0&& details.size>0) {
                            nothing_found.visibility = View.VISIBLE;
                        } else {
                            nothing_found.visibility = View.GONE
                        }
                        recyclerView.layoutManager = GridLayoutManager(applicationContext, 1)
                    }
                    else{
                        nothing_found.visibility=View.GONE
                    }
                    if(details.size!=0) {

                        var adapter = FindFriendsAdapter(this@find_friends, result)
                        recyclerView.adapter = adapter
                        show_progress?.visibility = View.GONE
                    }

                    return true
                }

            })
            return true
        }

    }
