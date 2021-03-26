package com.ludev.mychat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(findViewById(R.id.toolbar_login))

        supportActionBar!!.title = resources.getString(R.string.login)
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

        if (validateEmail() && validatePassword()) {

            login_progress_bar.visibility = View.VISIBLE
            login_btn.text = ""
            login_btn.isClickable = false

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else {
                    Toast.makeText(this, "Error: ${task.exception!!.message}", Toast.LENGTH_LONG).show()
                    login_progress_bar.visibility = View.GONE
                    login_btn.text = getString(R.string.login)
                    login_btn.isClickable = true
                }
            }
        }

    }

    private fun validateEmail(): Boolean {

        val email: String = email_login.text.toString()
        val emailInput: TextInputEditText = email_login
        val emailLayout: TextInputLayout = email_login_layout

        if (email.isEmpty() || !isValidEmail(email)) {

            emailLayout.isErrorEnabled = true
            emailLayout.error = "This field is empty or email is invalid."
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

    private fun validatePassword(): Boolean {

        val password = password_login.text.toString()
        val passwordInput: TextInputEditText = password_login
        val passwordLayout: TextInputLayout = password_login_layout

        if (password.isEmpty()) {

            passwordLayout.isErrorEnabled = true
            passwordLayout.error = "Complete this field."
            passwordInput.requestFocus()
            return false

        }
        else {
            passwordLayout.isErrorEnabled = false
            passwordLayout.error = null
        }

        return true

    }

    override fun onBackPressed() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

}