package com.ludev.mychat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSupportActionBar(findViewById(R.id.toolbar_register))

        supportActionBar!!.title = resources.getString(R.string.register)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_register.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        register_btn.setOnClickListener {

            registerUser()

        }

    }

    private fun registerUser() {

        val username: String = username_register.text.toString()
        val name: String = name_register.text.toString()
        val email: String = email_register.text.toString()
        val password: String = password_register.text.toString()

        when {

            username == "" -> username_register.error = "Please, write your username."
            name == "" -> name_register.error = "Please, write your name."
            email == "" -> email_register.error = "Please, write your email."
            password == "" -> password_register.error = "Please, write your password."
            else -> {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                    if (it.isSuccessful) {
                        firebaseUserID = mAuth.currentUser!!.uid
                        refUsers = FirebaseGlobalValue().ref.child("Users").child(firebaseUserID)

                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"] = firebaseUserID
                        userHashMap["username"] = username
                        userHashMap["email"] = email
                        userHashMap["name"] = name.capitalizeWords()
                        userHashMap["phone"] = "+12 3456 7890"
                        userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/mychat-4dcf8.appspot.com/o/profile.png?alt=media&token=0d3b3dbf-7acf-4ead-86fb-cea30904b325"
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = username.toLowerCase(Locale.ROOT)
                        userHashMap["facebook"] = "https://www.facebook.com/"
                        userHashMap["instagram"] = "https://www.instagram.com/"
                        userHashMap["twitter"] = "https://www.twitter.com/"

                        refUsers.updateChildren(userHashMap).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(this, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                    else {
                        Toast.makeText(this, "Error: ${it.exception!!.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }

    }

    override fun onBackPressed() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("DefaultLocale")
    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.toLowerCase().capitalize() }

}