package com.tathagat.postgram

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
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
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception

class setup : AppCompatActivity(){

    var username:EditText?=null
    var fullname:EditText?=null
    var country:Spinner?=null
    var save_information:Button?=null
    var profile_pic:CircleImageView?=null
    var firebasedatabase:FirebaseDatabase?=null
    var firebaseuser:FirebaseUser?=null
    var firebaseauth:FirebaseAuth?=null
    var usersdatabasereference:DatabaseReference?=null
    var profileimagesdatabasereference:DatabaseReference?=null
    var progressdialog:ProgressDialog?=null
    var imageUri: Uri?= null
    var firebasestorage:FirebaseStorage?=null
    var storagereference:StorageReference?=null
    var progressdialoguploadingpic:ProgressDialog?=null
    var userinfo = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        username=findViewById(R.id.setup_username)
        fullname=findViewById(R.id.setup_fullname)
        save_information=findViewById(R.id.save_information_btn)
        profile_pic=findViewById(R.id.setup_profile_image)
        firebaseauth=FirebaseAuth.getInstance()
        firebaseuser=firebaseauth?.currentUser
        firebasedatabase= FirebaseDatabase.getInstance()
        usersdatabasereference=firebasedatabase?.reference?.child("Users")

        firebasestorage= FirebaseStorage.getInstance()
        storagereference=firebasestorage?.reference?.child("Profile Images")
        save_information?.setOnClickListener {
            saveUserInformation()
        }
        profile_pic?.setOnClickListener {
            uploadImageToStorage()
        }

        usersdatabasereference?.child(firebaseuser?.uid!!)?.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.hasChild("profileimage")){
                    var profilepic_url=p0.child("profileimage").value.toString()
                    Picasso.get().load(profilepic_url).placeholder(R.drawable.profile).into(profile_pic)
                }
                else{
                    Picasso.get().load(R.drawable.profile).placeholder(R.drawable.profile).into(profile_pic)
                }
             }

        })

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
                                usersdatabasereference?.child(firebaseuser?.uid)?.child("profileimage")?.setValue(url)?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                    override fun onComplete(p0: Task<Void>) {
                                        if(p0.isSuccessful){

                                            Toast.makeText(applicationContext, "Profile pic successfully updated.", Toast.LENGTH_SHORT).show()

                                            progressdialoguploadingpic?.dismiss()
                                       }
                                        else{
                                            var err=p0.exception?.message
                                            Toast.makeText(applicationContext,err,Toast.LENGTH_SHORT).show()
                                            progressdialoguploadingpic?.dismiss()

                                        }
                                    }

                                })

                            }
                            else{
                                var err=p0.exception?.message
                                Toast.makeText(applicationContext,err,Toast.LENGTH_SHORT).show()
                                progressdialoguploadingpic?.dismiss()
                           }
                        }
                    })
            }


            catch(err:Exception){
                Toast.makeText(applicationContext,err.toString(),Toast.LENGTH_SHORT)
            }
        }}
    }

    private fun saveUserInformation() {
        progressdialog=ProgressDialog(this)
        progressdialog?.setTitle("Saving Information")
        progressdialog?.setMessage("Please wait while we are saving your information...")
        progressdialog?.setCanceledOnTouchOutside(false)
        var username = username?.text.toString().trim()
        var fullname = fullname?.text.toString().trim()
        if (TextUtils.isEmpty(username))
            Toast.makeText(applicationContext, "Please write your username...", Toast.LENGTH_SHORT).show()
        else
            if (TextUtils.isEmpty(fullname))
                Toast.makeText(applicationContext, "Please write your full name...", Toast.LENGTH_SHORT).show()
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
                            if(total>0){
                                Toast.makeText(applicationContext, "A user with username "+username+ " already exists",Toast.LENGTH_SHORT).show()

                            progressdialog?.dismiss()
                            }
                            else {
                                userinfo.put("username", username)
                                userinfo.put("fullname", fullname)
                                userinfo.put("status", "Hey there, I am using Postgram developed by Vaibhav Agrawal.")
                                progressdialog?.show()
                                usersdatabasereference?.child(firebaseuser?.uid!!)?.updateChildren(userinfo)
                                    ?.addOnCompleteListener(object : OnCompleteListener<Void> {
                                        override fun onComplete(p0: Task<Void>) {
                                            if (p0.isSuccessful) {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Your details are saved successfully.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                progressdialog?.dismiss()
                                                    sendToMainActivity()
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






    private fun sendToMainActivity() {
        var intent =Intent(applicationContext, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)

    }
}
