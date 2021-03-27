package com.ludev.mychat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
        val password: String = repeat_password_register.text.toString()

        if (validateUsername() && validateName() && validateEmail() && validatePasswords()) {

            register_progress_bar.visibility = View.VISIBLE
            register_btn.text = ""
            register_btn.isClickable = false

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
                    userHashMap["lastMessageTime"] = 0

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
                    register_progress_bar.visibility = View.GONE
                    register_btn.text = getString(R.string.register)
                    register_btn.isClickable = true
                }
            }
        }

    }

    private fun validateUsername(): Boolean {

        val username: String = username_register.text.toString()
        val usernameInput: TextInputEditText = username_register
        val usernameLayout: TextInputLayout = username_register_layout

        if (username.isEmpty()) {

            usernameLayout.isErrorEnabled = true
            usernameLayout.error = getString(R.string.empty_field)
            usernameInput.requestFocus()
            return false

        }
        else {
            usernameLayout.isErrorEnabled = false
            usernameLayout.error = null
        }

        return true

    }

    private fun validateName(): Boolean {

        val name: String = name_register.text.toString()
        val nameInput: TextInputEditText = name_register
        val nameLayout: TextInputLayout = name_register_layout

        if (name.isEmpty()) {

            nameLayout.isErrorEnabled = true
            nameLayout.error = getString(R.string.empty_field)
            nameInput.requestFocus()
            return false

        }
        else {
            nameLayout.isErrorEnabled = false
            nameLayout.error = null
        }

        return true

    }

    private fun validateEmail(): Boolean {

        val email: String = email_register.text.toString()
        val emailInput: TextInputEditText = email_register
        val emailLayout: TextInputLayout = email_register_layout

        if (email.isEmpty() || !isValidEmail(email)) {

            emailLayout.isErrorEnabled = true
            emailLayout.error = getString(R.string.invalid_email)
            emailInput.requestFocus()
            return false

        }
        else {
            emailLayout.isErrorEnabled = false
            emailLayout.error = null
        }

        return true

    }

    private fun isValidEmail(text: String): Boolean {

        return android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()

    }

    private fun validatePasswords(): Boolean {

        val password: String = password_register.text.toString()
        val passwordInput: TextInputEditText = password_register
        val passwordLayout: TextInputLayout = password_register_layout

        val password2: String = repeat_password_register.text.toString()
        val passwordInput2: TextInputEditText = repeat_password_register
        val passwordLayout2: TextInputLayout = repeat_password_register_layout

        when {
            password2 != password -> {

                passwordLayout2.isErrorEnabled = true
                passwordLayout2.error = getString(R.string.pass_not_match)
                passwordInput2.requestFocus()
                return false

            }
            password.isEmpty() -> {

                passwordLayout.isErrorEnabled = true
                passwordLayout.error = getString(R.string.empty_field)
                passwordInput.requestFocus()
                return false

            }
            password2.isEmpty() -> {

                passwordLayout2.isErrorEnabled = true
                passwordLayout2.error = getString(R.string.empty_field)
                passwordInput2.requestFocus()
                return false

            }
            else -> {
                passwordLayout.isErrorEnabled = false
                passwordLayout2.isErrorEnabled = false
                passwordLayout.error = null
                passwordLayout2.error = null
            }
        }

        return true

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