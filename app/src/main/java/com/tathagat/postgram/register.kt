package com.tathagat.postgram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class register : AppCompatActivity() {
var email:EditText?=null
    var password:EditText?=null
    var confirm_password:EditText?=null
    var create_account:Button?=null
    var firebaseauth:FirebaseAuth?=null
    var loadingbar:ProgressDialog?=null
    var databasereference:DatabaseReference?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        email=findViewById(R.id.register_email)
        password=findViewById(R.id.register_password)
        confirm_password=findViewById(R.id.register_confirm_password)
        create_account=findViewById(R.id.register_create_account)
        databasereference=FirebaseDatabase.getInstance().getReference("Users")
        firebaseauth= FirebaseAuth.getInstance()
        loadingbar= ProgressDialog(this)
        create_account?.setOnClickListener {
            createAccount()
        }
    }
    private fun createAccount() {
        var email=email?.text.toString().trim()
        var password=password?.text.toString().trim()
        var confirmpassword=confirm_password?.text.toString().trim()
        if(TextUtils.isEmpty(email))
            Toast.makeText(applicationContext,"Please write your email...",Toast.LENGTH_SHORT).show()
        else
            if(TextUtils.isEmpty(password))
                Toast.makeText(applicationContext,"Please write your password...",Toast.LENGTH_SHORT).show()
            else
                if(TextUtils.isEmpty(confirmpassword))
                    Toast.makeText(applicationContext,"Please confirm your password...",Toast.LENGTH_SHORT).show()
        else
                    if(!password.equals(confirmpassword))
                        Toast.makeText(applicationContext,"Your password does not match with confirm password...",Toast.LENGTH_SHORT).show()
        else{
                        loadingbar=ProgressDialog(this)
                        loadingbar?.setTitle("Creating Account")
                        loadingbar?.setMessage("Please wait a while we are creating your new account...")
                        loadingbar?.show()
                        loadingbar?.setCanceledOnTouchOutside(false)
                        firebaseauth?.createUserWithEmailAndPassword(email,password)?.addOnCompleteListener(object :OnCompleteListener<AuthResult>{
                            override fun onComplete(p0: Task<AuthResult>) {
                                if(p0.isSuccessful){
                                    var firebaseuser=firebaseauth?.currentUser
                                    Toast.makeText(applicationContext,"Your account created.",Toast.LENGTH_SHORT).show()
                                    firebaseuser?.sendEmailVerification()?.addOnCompleteListener(object :OnCompleteListener<Void>{
                                        override fun onComplete(p0: Task<Void>) {
                                            if(p0?.isSuccessful){
                                                loadingbar?.dismiss()
                                                Toast.makeText(applicationContext,"Email verification link has been sent.",Toast.LENGTH_SHORT).show()
                                                startSetUpActivity()
                                            }
                                            else{
                                                loadingbar?.dismiss()
                                                finish()
                                                var err=p0?.exception?.message
                                                Toast.makeText(applicationContext,err,Toast.LENGTH_SHORT).show()
                                            }

                                        }

                                    })

                                }
                                else{
                                    var err=p0.exception?.message
                                    Toast.makeText(applicationContext,err,Toast.LENGTH_SHORT).show()
                                    loadingbar?.dismiss()


                                }
                            }

                        })

                    }
    }

    private fun sendToMainActivity() {
        startActivity(Intent(applicationContext, MainActivity::class.java));
        finish()
    }

    private fun startSetUpActivity() {
        startActivity(Intent(applicationContext, setup::class.java))
    }
}
