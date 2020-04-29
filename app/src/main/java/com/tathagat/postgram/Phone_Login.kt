package com.tathagat.postgram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class Phone_Login : AppCompatActivity() {
    var phone_no: TextView? = null
    var send_verification_btn: TextView? = null
    var verification_code: TextView? = null
    var verify_btn: Button? = null
    var code_sent:String?=null
    var code_typed:String?=null
    var firebaseauth:FirebaseAuth?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone__login)
        phone_no = findViewById(R.id.login_phoneno)
        send_verification_btn = findViewById(R.id.send_verification_btn)
        verification_code = findViewById(R.id.verification_code)
        verify_btn = findViewById(R.id.verify_code_btn)
        firebaseauth=FirebaseAuth.getInstance()
        send_verification_btn?.setOnClickListener {
            var phone_number = phone_no?.text?.trim().toString()
            if (TextUtils.isEmpty(phone_number) || phone_number.length < 10)
                Toast.makeText(applicationContext, "Enter valid mobile number", Toast.LENGTH_SHORT).show()
            else {
                var mobile_number = "+91 " + phone_number
                phone_no?.visibility = View.INVISIBLE
                send_verification_btn?.visibility = View.INVISIBLE
                verification_code?.visibility = View.VISIBLE
                verify_btn?.visibility = View.VISIBLE
                sendVerificationCode(mobile_number)

                verify_btn?.setOnClickListener {
                    code_typed=verification_code?.text.toString()
                    if(code_typed!!.length==6&&code_sent!=null)
                 verifyCode(code_typed)
                    else
                        Toast.makeText(applicationContext,"Enter the correct verification code",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun verifyCode(code_typed: String?) {
var credential=PhoneAuthProvider.getCredential(code_sent!!,code_typed!!)
        signWithCredential(credential)
    }

    private fun signWithCredential(credential: PhoneAuthCredential?) {
       var progressDialog= ProgressDialog(this)
        progressDialog?.setTitle("Login")
        progressDialog?.setMessage("Please wait while you are allowed to login into your account...")
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.show()
firebaseauth?.signInWithCredential(credential!!)?.addOnCompleteListener(object :OnCompleteListener<AuthResult>{
    override fun onComplete(p0: Task<AuthResult>) {
        if(p0.isSuccessful)
        {
            progressDialog?.dismiss()
            Toast.makeText(applicationContext,"You are logged in successfully.",Toast.LENGTH_SHORT).show()

            var intent = Intent(applicationContext, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
        else{
            progressDialog.dismiss()
            var err=p0.exception?.message.toString()
            Toast.makeText(applicationContext,err,Toast.LENGTH_SHORT).show()
        }
    }

})
    }

    private fun sendVerificationCode(mobile_number: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            mobile_number, 60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD,
            mCallBack
        )

    }

    var mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(p0: PhoneAuthCredential?) {
            if(p0?.smsCode!=null)
                verification_code?.text=p0.smsCode}

        override fun onCodeSent(p0: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
            code_sent=p0

        }

        override fun onVerificationFailed(p0: FirebaseException?) {
            Toast.makeText(applicationContext, p0?.message.toString(),Toast.LENGTH_SHORT).show()
        }

    }
}
