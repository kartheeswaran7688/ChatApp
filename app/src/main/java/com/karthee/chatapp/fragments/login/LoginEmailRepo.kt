package com.karthee.chatapp.fragments.login


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.karthee.chatapp.ui.activities.MainActivity
import com.karthee.chatapp.utils.LogInFailedState
import com.karthee.chatapp.utils.printMeD
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import java.util.logging.Handler
import javax.inject.Inject

class LoginEmailRepo @Inject constructor(@ActivityRetainedScoped val actContxt: MainActivity,
                                    @ApplicationContext val context: Context) {

    private val verificationId: MutableLiveData<String> = MutableLiveData()

    private val credential: MutableLiveData<PhoneAuthCredential> = MutableLiveData()

    private val loginTaskResult: MutableLiveData<Task<AuthResult>> = MutableLiveData()
    private val registerTaskResult: MutableLiveData<Task<AuthResult>> = MutableLiveData()

    private val failedState: MutableLiveData<LogInFailedState> = MutableLiveData()

    private val auth = FirebaseAuth.getInstance()

    init {
        "LoginRepo init".printMeD()
    }

    fun signIn(email: String,pass: String) {
        signIn(email,pass,false)
    }
    fun signIn(email:String,pass:String,register:Boolean){
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(object:OnCompleteListener<AuthResult>{
            override fun onComplete(result: Task<AuthResult>) {
                if(!register) {
                    if (result.isSuccessful) {
                        loginTaskResult.value = result
                    } else {
                        failedState.value = LogInFailedState.Failed
                    }
                } else {
                    if (result.isSuccessful) {
                        registerTaskResult.value = result
                    } else {
                        failedState.value = LogInFailedState.Failed
                    }
                }
            }
        })

    }

    fun registerUser(email:String,pass:String){
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(object:OnCompleteListener<AuthResult>{
            override fun onComplete(result: Task<AuthResult>) {
                if(result.isSuccessful) {
                    registerTaskResult.value = result
                    //signIn(email,pass)
                }else {
                    failedState.value = LogInFailedState.Failed
                }
            }
        })
    }

    fun registerAndSignIn(email: String,pass: String) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(object:OnCompleteListener<AuthResult>{
            override fun onComplete(result: Task<AuthResult>) {
                if(result.isSuccessful) {
                   android.os.Handler().postDelayed(object:Runnable{
                        override fun run() {
                            signIn(email,pass,true)
                        }
                    },3000)

                    //signIn(email,pass)
                }else {
                    failedState.value = LogInFailedState.Failed
                }
            }
        })
    }

    fun getVCode(): MutableLiveData<String> {
        return verificationId
    }

    fun setVCodeNull() {
        verificationId.value = null
    }

    fun clearOldAuth(){
        credential.value=null
        loginTaskResult.value=null
        registerTaskResult.value=null
    }

    fun getCredential(): LiveData<PhoneAuthCredential> {
        return credential
    }

    fun getTaskResult(): LiveData<Task<AuthResult>> {
        return loginTaskResult
    }

    fun getRegisterTaskResult(): LiveData<Task<AuthResult>> {
        return registerTaskResult
    }

    fun getFailed(): LiveData<LogInFailedState> {
        return failedState
    }

}