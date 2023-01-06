package com.devanasmohammed.locationreminder.authentication

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.devanasmohammed.locationreminder.R
import com.devanasmohammed.locationreminder.databinding.ActivityAuthenticationBinding
import com.devanasmohammed.locationreminder.locationreminders.RemindersActivity
import com.devanasmohammed.locationreminder.utils.SharedPreferenceManger
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private var _binding: ActivityAuthenticationBinding? = null
    private val binding get() = _binding!!
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil
            .setContentView(this, R.layout.activity_authentication)
        binding.lifecycleOwner = this


        val isSignIn = SharedPreferenceManger.loadBoolean(this,"isSignIn",false)

        if(isSignIn==true){
            navigateToReminderScreen()
        }

        //Implement the create account and sign in using FirebaseUI,
        //use sign in using email and sign in using Google
        signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { res ->
            this.onSignInResult(res)
        }

        //start sign in flow
        binding.loginBtn.setOnClickListener {
            signIn()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    /**
     * This method to kick off the FirebaseUI sign in flow
     * with my authentication providers
     */
    private fun signIn(){
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    /**
     * Handle on sign in result
     * if RESULT_Ok navigateToReminderScreen
     */
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            SharedPreferenceManger.saveData(this,"isSignIn",true)
            navigateToReminderScreen()
        }
    }

    private fun navigateToReminderScreen(){
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()
    }

}
