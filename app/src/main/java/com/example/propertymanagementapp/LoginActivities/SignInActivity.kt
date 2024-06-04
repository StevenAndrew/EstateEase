package com.example.propertymanagementapp.LoginActivities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.propertymanagementapp.mainUI.MainActivity
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.data.User
import com.example.propertymanagementapp.databinding.ActivitySignInBinding
import com.example.propertymanagementapp.firebase.FirestoreClass
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lateinit var binding : ActivitySignInBinding
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        fun setupActionBar(){
            setSupportActionBar(findViewById(R.id.toolbar_sign_in_activity))

            val actionBar = supportActionBar

            if (actionBar!=null){
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
            }
            binding.toolbarSignInActivity.setNavigationOnClickListener{onBackPressed()}
        }

        fun isEmailValid(email: String): Boolean {
            val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
            return emailRegex.toRegex().matches(email)
        }

        fun validateForm(email: String, password: String): Boolean{
            return when {
                TextUtils.isEmpty(email)->{
                    showErrorSnackBar("Please enter a email")
                    false
                }
                !isEmailValid(email)->{
                    showErrorSnackBar("Please enter a valid email")
                    false
                }
                TextUtils.isEmpty(password)->{
                    showErrorSnackBar("Please enter a password")
                    false
                }
                else -> {true}
            }
        }



        auth = Firebase.auth
        fun signInUser(){
            val email: String = binding.etEmail.text.toString().trim{it<=' '}
            val password: String = binding.etPassword.text.toString().trim{it<=' '}
            if (validateForm(email, password)){
                showProgressDialog()
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()
                        // Sign in success, update UI with the signed-in user's information
                        FirestoreClass().loadUserData(this)
                    } else {
                        hideProgressDialog()
                        // If sign in fails, display a message to the user.
                        Log.w("Sign In", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT,).show()
                    }
                }
            }
        }

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setupActionBar()
        binding.btnSignIn.setOnClickListener{
            signInUser()
        }
    }

    fun signInSuccess(user: User){
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
