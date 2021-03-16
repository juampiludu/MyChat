package com.ludev.mychat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(findViewById(R.id.toolbar_login))

        supportActionBar!!.title = "Login"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_login.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        login_btn.setOnClickListener {
            loginUser()
        }

    }

    private fun loginUser() {

        val email: String = email_login.text.toString()
        val password: String = password_login.text.toString()

        when {

            email == "" -> {
                email_login.error = "Please, write your email."
            }
            password == "" -> {
                password_login.error = "Please, write your password."
            }
            else -> {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        Toast.makeText(this, "Error: ${task.exception!!.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }
}