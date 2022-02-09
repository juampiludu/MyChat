package com.ludev.mychat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        setSupportActionBar(findViewById(R.id.toolbar_forgot_password))

        supportActionBar!!.title = "Forgot password"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_forgot_password.setNavigationOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        send_email_btn.setOnClickListener {
            val email = email_forgot_pass.text.toString()
            sendEmail(email)
        }

    }

    private fun sendEmail(email: String) {

        if (email.isEmpty()) {
            email_forgot_pass_layout.isErrorEnabled = true
            email_forgot_pass_layout.error = "Enter you email"
            email_forgot_pass_layout.requestFocus()
        }
        else if (!isValidEmail(email)) {
            email_forgot_pass_layout.isErrorEnabled = true
            email_forgot_pass_layout.error = "The email you have entered has errors"
            email_forgot_pass_layout.requestFocus()
        }
        else {
            send_email_progress_bar.visibility = View.VISIBLE
            send_email_btn.text = ""
            send_email_btn.isClickable = false

            email_forgot_pass_layout.isErrorEnabled = false
            email_forgot_pass_layout.error = ""

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener {

                if (it.isSuccessful) {
                    Toast.makeText(this, "Email sent!", Toast.LENGTH_SHORT).show()
                    email_forgot_pass.setText("")
                }
                else {
                    Toast.makeText(this, "Failed to send email.", Toast.LENGTH_SHORT).show()
                }

                send_email_progress_bar.visibility = View.GONE
                send_email_btn.text = "Send email"
                send_email_btn.isClickable = true

            }
        }

    }

    private fun isValidEmail(text: String): Boolean {

        return android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()

    }

    override fun onBackPressed() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}