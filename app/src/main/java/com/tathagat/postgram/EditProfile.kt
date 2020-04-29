package com.tathagat.postgram

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception

class EditProfile : AppCompatActivity() {
var usersdatabasereference:DatabaseReference?=null
    var username: EditText?=null
    var fullname: EditText?=null
    var profile_pic: CircleImageView?=null
    var status: EditText?=null
    var save_btn:Button?=null
    var imageUri: Uri?= null
    var firebaseuser:FirebaseUser?=null
    var storagereference:StorageReference?=null
    var progressdialoguploadingpic:ProgressDialog?=null
    var Username:String?=null
    var profilpic:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        username=findViewById(R.id.edit_username)
        storagereference=FirebaseStorage.getInstance()?.reference?.child("Profile Images")
        fullname=findViewById(R.id.edit_fullname)
        profile_pic=findViewById(R.id.edit_profile_image)
        status=findViewById(R.id.edit_status)
        save_btn=findViewById(R.id.edit_information_btn)
        firebaseuser=FirebaseAuth.getInstance().currentUser
        usersdatabasereference= FirebaseDatabase.getInstance().getReference("Users")
        usersdatabasereference?.child(firebaseuser!!.uid)?.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.exists()) {
                        var Fullname = p0?.child("fullname").value.toString()
                        Username = p0?.child("username").value.toString()
                        var user_status = p0?.child("status").value.toString()
                        profilpic = p0?.child("profileimage").value.toString()
                        fullname?.setText(Fullname)
                        username?.setText(Username)
                        status?.setText(user_status)
                        Picasso.get().load(profilpic).placeholder(R.drawable.profile).into(profile_pic)
                    }}
            }
        )
        profile_pic?.setOnClickListener {
            if(profilpic!!.equals("null")){
                uploadImageToStorage()
            }
            else{
                var alertDialog=AlertDialog.Builder(this)
                var option=arrayOf("Change Profile Pic","Remove Profile Pic")
                alertDialog.setItems(option,object :DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        when(p1){
                            0->{
                                uploadImageToStorage()
                            }
                            1->{
                                usersdatabasereference?.child(firebaseuser!!.uid)?.child("profileimage")?.removeValue()
                                    ?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                        override fun onComplete(p0: Task<Void>) {
                                            if(p0.isSuccessful)
                                                Toast.makeText(applicationContext,"Profile Pic Removed",Toast.LENGTH_SHORT).show()
                                        }

                                    })
                            }
                        }
                    }

                })
                alertDialog.show()
            }

        }
        save_btn?.setOnClickListener {
            saveUserInformation()
        }
    }
    private fun saveUserInformation() {
        var userinfo = HashMap<String, Any>()
        var progressdialog=ProgressDialog(this)
        progressdialog?.setTitle("Saving Information")
        progressdialog?.setMessage("Please wait while we are saving your information...")
        progressdialog?.setCanceledOnTouchOutside(false)
        var username = username?.text.toString().trim()
        var fullname = fullname?.text.toString().trim()
        var status= status?.text.toString().trim()
        if (TextUtils.isEmpty(username))
            Toast.makeText(applicationContext, "Please write your username...", Toast.LENGTH_SHORT).show()
        else
            if (TextUtils.isEmpty(fullname))
                Toast.makeText(applicationContext, "Please write your full name...", Toast.LENGTH_SHORT).show()
            else
                if (TextUtils.isEmpty(status))
                    Toast.makeText(applicationContext, "Please write your status...", Toast.LENGTH_SHORT).show()
            else {
                progressdialog?.show()
                var total:Long=0

                var query=usersdatabasereference?.orderByChild("username")?.equalTo(username)
                var  valueEventListener=object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        total=p0!!.childrenCount
                        if(total>0&&!username.equals(Username)){
                            Toast.makeText(applicationContext, "A user with username "+username+ " already exists",Toast.LENGTH_SHORT).show()

                            progressdialog?.dismiss()
                        }
                        else {
                            userinfo.put("username", username)
                            userinfo.put("fullname", fullname)
                            userinfo.put("status", status)
                            progressdialog?.show()
                            usersdatabasereference?.child(firebaseuser!!.uid)?.updateChildren(userinfo)
                                ?.addOnCompleteListener(object : OnCompleteListener<Void> {
                                    override fun onComplete(p0: Task<Void>) {
                                        if (p0.isSuccessful) {
                                            Toast.makeText(
                                                applicationContext,
                                                "Your details are updated successfully.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            progressdialog?.dismiss()

                                        } else {
                                            progressdialog?.dismiss()
                                            var err = p0.exception?.message
                                            Toast.makeText(applicationContext, err, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                })
                        }
                    }

                }
                query?.addListenerForSingleValueEvent(valueEventListener)

            }
    }

    private fun uploadImageToStorage(){
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            var result = CropImage . getActivityResult (data)
            if (resultCode == RESULT_OK) {
                imageUri = result . getUri ()
                try {
                    progressdialoguploadingpic= ProgressDialog(this)
                    progressdialoguploadingpic?.setTitle("Uploading Profile Pic")
                    progressdialoguploadingpic?.setMessage("Please wait while we are uploading your profile pic...")
                    progressdialoguploadingpic?.show()
                    progressdialoguploadingpic?.setCanceledOnTouchOutside(false)
                    var filepath = storagereference?.child(firebaseuser?.uid + ".jpg")// name of image stored
                    filepath?.putFile(imageUri!!)
                        ?.addOnCompleteListener(object : OnCompleteListener<UploadTask.TaskSnapshot> {
                            override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                                if (p0.isSuccessful) {
                                    var url=p0.getResult()?.downloadUrl.toString()
                                    usersdatabasereference?.child(firebaseuser!!.uid)?.child("profileimage")?.setValue(url)?.addOnCompleteListener(object :
                                        OnCompleteListener<Void> {
                                        override fun onComplete(p0: Task<Void>) {
                                            if(p0.isSuccessful){

                                                Toast.makeText(applicationContext, "Profile pic successfully updated.", Toast.LENGTH_SHORT).show()

                                                progressdialoguploadingpic?.dismiss()
                                            }
                                            else{
                                                var err=p0.exception?.message
                                                Toast.makeText(applicationContext,err, Toast.LENGTH_SHORT).show()
                                                progressdialoguploadingpic?.dismiss()

                                            }
                                        }

                                    })

                                }
                                else{
                                    var err=p0.exception?.message
                                    Toast.makeText(applicationContext,err, Toast.LENGTH_SHORT).show()
                                    progressdialoguploadingpic?.dismiss()
                                }
                            }
                        })
                }


                catch(err: Exception){
                    Toast.makeText(applicationContext,err.toString(), Toast.LENGTH_SHORT)
                }
            }}
    }


}
