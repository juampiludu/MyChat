package com.ludev.mychat

import `in`.championswimmer.libsocialbuttons.FabSocial
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ludev.mychat.ModelClasses.Users
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_visit_user_profile.*

class VisitUserProfileActivity : AppCompatActivity() {

    private var userVisitId: String = ""
    var user: Users? = null
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_user_profile)

        profile_visit_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        profile_visit_toolbar.navigationIcon?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                colorFilter = BlendModeColorFilter(Color.WHITE, BlendMode.SRC_IN)
            }else{
                ForegroundColorSpan(Color.WHITE)
            }
        }

        userVisitId = intent.getStringExtra("visit_id").toString()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        val ref = FirebaseGlobalValue().ref.child("Users").child(userVisitId)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {

                    user = p0.getValue(Users::class.java)

                    username_display.text = "@" + user!!.username
                    name_display.text = user!!.name
                    Picasso.get().load(user!!.profile).into(profile_display)
                    email_display.text = user!!.email

                    if (user!!.phone == "+12 3456 7890" || user!!.phone == "") {
                        phone_display_ln.visibility = View.GONE
                    }
                    else {
                        phone_display.text = user!!.phone
                    }

                    socialMedia(facebook_display, user!!.facebook)
                    socialMedia(instagram_display, user!!.instagram)
                    socialMedia(twitter_display, user!!.twitter)

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        send_msg_button.setOnClickListener {
            val intent = Intent(applicationContext, MessageChatActivity::class.java)
            intent.putExtra("visit_id", user!!.uid)
            startActivity(intent)
        }

    }

    private fun socialMedia(buttonDisplay: FabSocial?, socialMedia: String?) {

        buttonDisplay!!.setOnClickListener {

            if (socialMedia == "" ||
                socialMedia == "https://www.facebook.com/" ||
                socialMedia == "https://www.instagram.com/" ||
                socialMedia == "https://www.twitter.com/"
            ) {
                Toast.makeText(applicationContext, resources.getString(R.string.user_not_social_media), Toast.LENGTH_LONG).show()
            }
            else {

                val uri = Uri.parse(socialMedia)

                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)

            }

        }

    }

    override fun onResume() {
        super.onResume()

        MainActivity().updateStatus("online", firebaseUser!!.uid)
    }

    override fun onPause() {
        super.onPause()

        MainActivity().updateStatus("offline", firebaseUser!!.uid)
    }

}