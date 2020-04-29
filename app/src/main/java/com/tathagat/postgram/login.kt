package com.tathagat.postgram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class login : AppCompatActivity() {
var email:EditText?=null
    var password:EditText?=null
    var login_btn:Button?=null
    var create_account_link:TextView?=null
    var firebaseauth:FirebaseAuth?=null
    var progressDialog:ProgressDialog?=null
    var login_with_phone:TextView?=null
    var firebaseuser:FirebaseUser?=null
    var firebasedatabase:FirebaseDatabase?=null
    var databasereference:DatabaseReference?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        email=findViewById(R.id.login_email)
        password=findViewById(R.id.login_password)
        login_btn=findViewById(R.id.login_account)
        login_with_phone=findViewById(R.id.login_with_phone_link)
        var Forgot_password=findViewById<TextView>(R.id.forgot_password)
        Forgot_password.setOnClickListener {
            startActivity(Intent(applicationContext, forgot_password::class.java))
        }
        login_with_phone?.setOnClickListener {
            startActivity(Intent(this, Phone_Login::class.java))
        }
        firebaseauth=FirebaseAuth.getInstance()
        firebaseuser = firebaseauth?.currentUser
        firebasedatabase = FirebaseDatabase.getInstance()
        databasereference = firebasedatabase?.reference?.child("Users")
        create_account_link=findViewById(R.id.register_account_link)
        create_account_link?.setOnClickListener {
            startRegisterActivity()
        }
        login_btn?.setOnClickListener {
            allowUserToLogin()
        }
    }

    private fun allowUserToLogin() {
        var email=email?.text.toString().trim()
        var password=password?.text.toString().trim()
        if(TextUtils.isEmpty(email))
            Toast.makeText(applicationContext,"Please write your email...", Toast.LENGTH_SHORT).show()
        else
            if(TextUtils.isEmpty(email))
                Toast.makeText(applicationContext,"Please write your email...", Toast.LENGTH_SHORT).show()
        else{
                progressDialog=ProgressDialog(this)
                progressDialog?.setTitle("Login")
                progressDialog?.setMessage("Please wait a while you are allowed to login into your account...")
                progressDialog?.setCanceledOnTouchOutside(false)
                progressDialog?.show()
             firebaseauth?.signInWithEmailAndPassword(email,password)?.addOnCompleteListener(object :OnCompleteListener<AuthResult>{
                 override fun onComplete(p0: Task<AuthResult>) {
                     if(p0.isSuccessful) {
                         firebaseuser=firebaseauth!!.currentUser
                         if(firebaseuser!!.isEmailVerified){
                         progressDialog?.dismiss()
                         sendToMainActivity()

                         Toast.makeText(applicationContext, "You are logged in successfully.", Toast.LENGTH_SHORT).show()

                     }
                         else{
                             progressDialog?.dismiss()
                       Toast.makeText(applicationContext,"Your email is not verified.",Toast.LENGTH_SHORT).show()
                         }
                     }  else{
                         var err=p0.exception?.message
                         progressDialog?.dismiss()
                         Toast.makeText(applicationContext,err, Toast.LENGTH_SHORT).show()
                     }
                 }

             })
            }

    }


    private fun sendToMainActivity() {
    startActivity(Intent(applicationContext, MainActivity::class.java));
        finish()
    }

    private fun startRegisterActivity() {
        startActivity(Intent(applicationContext, register::class.java))
    }

}
