package com.ludev.mychat

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ludev.mychat.ModelClasses.Users
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_full_image.*

class ViewFullImageActivity : AppCompatActivity() {

    private var imageViewer: ImageView? = null
    private var imageUrl: String = ""
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_image)

        full_image_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        full_image_toolbar.navigationIcon?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                colorFilter = BlendModeColorFilter(Color.WHITE, BlendMode.SRC_IN)
            }else{
                setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            }
        }

        imageUrl = intent.getStringExtra("url")
        userId = intent.getStringExtra("user_id")
        imageViewer = findViewById(R.id.image_viewer)

        Picasso.get().load(imageUrl).into(imageViewer)

        imageViewer!!.setOnClickListener {

            if (full_image_toolbar.visibility == View.VISIBLE && full_image_rl.visibility == View.VISIBLE) {
                full_image_toolbar.visibility = View.GONE
                full_image_rl.visibility = View.GONE
            }
            else {
                full_image_toolbar.visibility = View.VISIBLE
                full_image_rl.visibility = View.VISIBLE
            }

        }

        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

        val ref = FirebaseGlobalValue().ref.child("Users").child(userId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {

                    val user: Users? = p0.getValue(Users::class.java)

                    val username = user!!.username

                    full_image_user_txt.text = username
                    /*if (currentUser.toString() == userId) {
                        full_image_user_txt.text = "You"
                    }
                    else {
                        full_image_user_txt.text = username
                    }*/

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }
}