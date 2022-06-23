package com.karthee.chatapp.fragments.login


import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.karthee.chatapp.models.Country
import com.karthee.chatapp.ui.activities.MainActivity
import com.karthee.chatapp.utils.LogInFailedState
import com.karthee.chatapp.utils.printMeD
import com.karthee.chatapp.utils.toast
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LoginEmailRepo @Inject constructor(@ActivityRetainedScoped val actContxt: MainActivity,
                                    @ApplicationContext val context: Context) {

    private val verificationId: MutableLiveData<String> = MutableLiveData()

    private val credential: MutableLiveData<PhoneAuthCredential> = MutableLiveData()

    private val taskResult: MutableLiveData<Task<AuthResult>> = MutableLiveData()

    private val failedState: MutableLiveData<LogInFailedState> = MutableLiveData()

    private val auth = FirebaseAuth.getInstance()

    init {
        "LoginRepo init".printMeD()
    }

    fun signIn(email:String,pass:String){
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(object:OnCompleteListener<AuthResult>{
            override fun onComplete(result: Task<AuthResult>) {
                if (result.isSuccessful) {
                    taskResult.value = result
                } else {
                    failedState.value = LogInFailedState.Failed
                }
            }
        })

    }

    fun registerUser(email:String,pass:String){
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(object:OnCompleteListener<AuthResult>{
            override fun onComplete(result: Task<AuthResult>) {
                if(result.isSuccessful) {
                    signIn(email,pass)
                }else {
                    failedState.value = LogInFailedState.Failed
                }
            }
        })
    }
//
//    fun setMobile(country: Country, mobile: String) {
//        Timber.v("Mobile $mobile")
//        val number = country.noCode + " " + mobile
//        val options = PhoneAuthOptions.newBuilder(auth)
//            .setPhoneNumber(number)
//            .setTimeout(60L, TimeUnit.SECONDS)
//            .setActivity(actContxt)
//            .setCallbacks(this)
//            .build()
//        PhoneAuthProvider.verifyPhoneNumber(options)
//    }
//
//    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//        Timber.v("onVerificationCompleted:$credential")
//        this.credential.value = credential
//        Handler(Looper.getMainLooper()).postDelayed({
//            signInWithPhoneAuthCredential(credential)
//        }, 1000)
//    }
//
//    override fun onVerificationFailed(exp: FirebaseException) {
//        "onVerficationFailed:: ${exp.message}".printMeD()
//        failedState.value = LogInFailedState.Verification
//        when (exp) {
//            is FirebaseAuthInvalidCredentialsException ->
//                context.toast("Invalid Request")
//            else -> context.toast(exp.message.toString())
//        }
//    }
//
//    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//        Timber.v("onCodeSent:$verificationId")
//        this.verificationId.value = verificationId
//        context.toast("Verification code sent successfully")
//    }
//
//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//        FirebaseAuth.getInstance().signInWithCredential(credential)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Timber.v("signInWithCredential:success")
//                    taskResult.value = task
//                } else {
//                    Timber.v("signInWithCredential:failure ${task.exception}")
//                    if (task.exception is FirebaseAuthInvalidCredentialsException)
//                        context.toast("Invalid verification code!")
//                    failedState.value = LogInFailedState.SignIn
//                }
//            }
//    }
//
//    fun setCredential(credential: PhoneAuthCredential) {
//        signInWithPhoneAuthCredential(credential)
//    }

    fun getVCode(): MutableLiveData<String> {
        return verificationId
    }

    fun setVCodeNull() {
        verificationId.value = null
    }

    fun clearOldAuth(){
        credential.value=null
        taskResult.value=null
    }

    fun getCredential(): LiveData<PhoneAuthCredential> {
        return credential
    }

    fun getTaskResult(): LiveData<Task<AuthResult>> {
        return taskResult
    }

    fun getFailed(): LiveData<LogInFailedState> {
        return failedState
    }
}