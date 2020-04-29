package com.tathagat.postgram

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
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

class add_story : AppCompatActivity() {
    var contentURI: Uri? = null
    var story_image: ImageView? = null
    var story_desp: EditText? = null
    var add_story_btn: Button? = null
    var storystoragereference: StorageReference? = null
    var storyreference: DatabaseReference? = null
    var firebaseuser: FirebaseUser? = null
    var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this);
        story_image = findViewById(R.id.select_story_image)
        story_desp = findViewById(R.id.story_description)
        add_story_btn = findViewById(R.id.add_story_button)
        storystoragereference = FirebaseStorage.getInstance().reference.child("Story Images")
        firebaseuser = FirebaseAuth.getInstance().currentUser
        uid = firebaseuser?.uid
        storyreference = FirebaseDatabase.getInstance().getReference("Stories").child(uid)
        story_image?.setOnClickListener {

            selectImageFromGallery()
        }
        add_story_btn?.setOnClickListener {
            addStoryToDatabase()

        }
    }

    private fun addStoryToDatabase() {
        if (contentURI == null)
            Toast.makeText(applicationContext, "Please choose a pic for your status...", Toast.LENGTH_SHORT).show()
        else {
            var progressDialog=ProgressDialog(this)
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog?.setTitle("Adding Story")
            progressDialog?.setMessage("Please wait while we are adding your story...")
            progressDialog?.show()

            var storydescription = story_desp?.text.toString()
            val today = Date()
            val format = SimpleDateFormat("yyyyMMddHHmmss")
            val filename = format.format(today) + uid
            var file = storystoragereference?.child(filename)
            file?.putFile(contentURI!!)?.addOnCompleteListener(object : OnCompleteListener<UploadTask.TaskSnapshot> {
                override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                    if (p0.isSuccessful) {
                        var statusimagelink = p0.result?.downloadUrl.toString()
                        var storyinfo = HashMap<String, Any>()
                        storyinfo.put("storydescription", storydescription)
                        storyinfo.put("storyimagelink", statusimagelink)

                        storyreference?.child(filename)?.updateChildren(storyinfo)
                            ?.addOnCompleteListener(object : OnCompleteListener<Void> {
                                override fun onComplete(p0: Task<Void>) {

                                    if (p0.isSuccessful) {
                                        progressDialog?.dismiss()
                                        Toast.makeText(applicationContext, "Story Added", Toast.LENGTH_SHORT).show()
                                        var databaseReference=storyreference
                                        databaseReference?.child("lastupdated")?.setValue(format.format(today))
finish()
                                    }
                                }

                            })
                    }
                }

            })
        }
    }

    private fun selectImageFromGallery() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this);

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            var result = CropImage . getActivityResult (data)
            if (resultCode == RESULT_OK) {
                contentURI = result . getUri ()
                Picasso.get().load(contentURI).into(story_image)
            }
        }
    }
}