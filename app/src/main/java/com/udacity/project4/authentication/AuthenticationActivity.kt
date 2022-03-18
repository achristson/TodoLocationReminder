package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.udacity.project4.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*

private const val SIGN_IN_RESULT_CODE = 0
private const val TAG = "AuthenticationActivity"

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        login_button.setOnClickListener {
            launchSigninFlow()
        }

        FirebaseUserLiveData().observe(this, Observer { user ->
            if (user != null){
                val intentToRemindersActivity = Intent(this, RemindersActivity::class.java)
                startActivity(intentToRemindersActivity)
                finish()
            } else {
                Log.i(TAG, "User not logged in")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_RESULT_CODE){
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK){
                Log.i(TAG, "Signed ${FirebaseAuth.getInstance().currentUser?.displayName} in")
            } else {
                Log.e(TAG, "Failed to sign in ${response?.error?.errorCode}")
            }
        }
    }

    private fun launchSigninFlow(){
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
                SIGN_IN_RESULT_CODE)
    }
}
