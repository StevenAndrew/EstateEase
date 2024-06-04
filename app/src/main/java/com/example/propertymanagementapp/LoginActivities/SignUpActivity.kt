package com.example.propertymanagementapp.LoginActivities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.propertymanagementapp.R
import com.example.propertymanagementapp.data.User
import com.example.propertymanagementapp.databinding.ActivitySignUpBinding
import com.example.propertymanagementapp.firebase.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        lateinit var binding : ActivitySignUpBinding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        fun setupActionBar(){
            setSupportActionBar(binding.toolbarSignUpActivity)

            val actionBar = supportActionBar

            if (actionBar!=null){
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
            }

            binding.toolbarSignUpActivity.setNavigationOnClickListener{onBackPressed()}
        }

        fun isEmailValid(email: String): Boolean {
            val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
            return emailRegex.toRegex().matches(email)
        }

        fun validateForm(name: String, email: String, password: String): Boolean{
            return when {
                TextUtils.isEmpty(name)->{
                    showErrorSnackBar("Please enter a name")
                    false
                }
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

        fun registerUser(){
            val name: String = binding.etName.text.toString().trim{it<=' '}
            val email: String = binding.etEmail.text.toString().trim{it<=' '}
            val password: String = binding.etPassword.text.toString().trim{it<=' '}

            if (validateForm(name,email,password)){
                showProgressDialog()
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        FirestoreClass().registerUser(this,user)
                    }else{
                        Toast.makeText(this,task.exception!!.message,Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setupActionBar()
        binding.btnSignUp.setOnClickListener{
            registerUser()
        }
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this,"You have successfully registered", Toast.LENGTH_SHORT).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}

