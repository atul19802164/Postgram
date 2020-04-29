package com.tathagat.postgram

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class PostActivity : AppCompatActivity() {
    var mToolbar: Toolbar? = null
    var select_image: ImageView? = null
    var post_desp: TextView? = null
    var update_post: Button? = null
    var firebaseuser: FirebaseUser? = null
    var storagereference: StorageReference? = null
    var postreference: DatabaseReference? = null
    lateinit var userreference:DatabaseReference
    var contentURI:Uri?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this);
        mToolbar = findViewById(R.id.post_activity_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(
            true
        )
        supportActionBar?.setTitle("Update Post")
        select_image = findViewById(R.id.select_post_image)
        post_desp=findViewById(R.id.post_description)
        update_post=findViewById(R.id.update_post_button)
        storagereference=FirebaseStorage.getInstance().getReference("Posts Images")
        postreference=FirebaseDatabase.getInstance().getReference("Posts")
        firebaseuser=FirebaseAuth.getInstance().currentUser
        userreference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseuser?.uid)
select_image?.setOnClickListener {
    selectImageFromGallery()
}
        update_post?.setOnClickListener {
            updatePost()
        }
    }
    private fun selectImageFromGallery() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)

    }

    private fun updatePost() {
        val today = Date()
        val format_date = SimpleDateFormat("MMM dd, yyyy")
        val format_time = SimpleDateFormat("hh:mm a")
        val date = format_date.format(today)
        val time = format_time.format(today)
        val format = SimpleDateFormat("yyyyMMddHHmmss")
        val filename = format.format(today)
        var counter=filename.toLong()
        var post_description=post_desp?.text.toString()
        var filepath=storagereference?.child(filename+":"+firebaseuser?.uid)
        if(contentURI==null)
            Toast.makeText(applicationContext,"Please choose a pic for your post...",Toast.LENGTH_SHORT).show()
        else {
            var dialog = ProgressDialog(this)
            dialog.setTitle("Updating Post")
            dialog.setMessage("Please wait a while we are updating your post...")
            dialog.show()
            dialog.setCanceledOnTouchOutside(false)
            filepath?.putFile(contentURI!!)
                ?.addOnCompleteListener(object : OnCompleteListener<UploadTask.TaskSnapshot> {
                    override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                        if (p0?.isSuccessful) {
                            var link = p0?.result?.downloadUrl.toString()
                            var postinfo = HashMap<String, Any>()
                            postinfo.put("postimage", link)
                            postinfo.put("date", date)
                            postinfo.put("time", time)
                            postinfo.put("uid", firebaseuser?.uid!!)
                            postinfo.put("description", post_description)
                            postinfo.put("counter", counter)
                            postreference?.child(filename + firebaseuser?.uid)?.updateChildren(postinfo)
                                ?.addOnCompleteListener(object : OnCompleteListener<Void> {
                                    override fun onComplete(p0: Task<Void>) {
                                        if (p0.isSuccessful) {
                                            dialog.dismiss()
                                            Toast.makeText(applicationContext, "Post Updated", Toast.LENGTH_SHORT)
                                                .show()
                                            finish()
                                        }
                                    }

                                })


                        }
                    }

                })
        }
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            var result = CropImage . getActivityResult (data)
            if (resultCode == RESULT_OK) {
                contentURI = result . getUri ()
                Picasso.get().load(contentURI).into(select_image)
            }
        }
    }


}