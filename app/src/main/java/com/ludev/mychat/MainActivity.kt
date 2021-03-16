package com.ludev.mychat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.ludev.mychat.Fragments.ChatsFragment
import com.ludev.mychat.Fragments.SearchFragment
import com.ludev.mychat.Fragments.SettingsFragment
import com.ludev.mychat.ModelClasses.Chat
import com.ludev.mychat.ModelClasses.Users
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var refUsers: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseGlobalValue().ref.child("Users").child(firebaseUser!!.uid)

        supportActionBar!!.title = ""

        val ref = FirebaseGlobalValue().ref.child("Chats")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var countUnreadMessages = 0

                for (dataSnapshot in p0.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.receiver == firebaseUser!!.uid && !chat.seen) {

                        countUnreadMessages += 1

                    }
                }

                if (countUnreadMessages == 0) {
                    viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
                }
                else {
                    viewPagerAdapter.addFragment(ChatsFragment(), "Chats ($countUnreadMessages)")
                }

                viewPagerAdapter.addFragment(SearchFragment(), "Search")
                viewPagerAdapter.addFragment(SettingsFragment(), "Settings")

                view_pager.adapter = viewPagerAdapter
                tab_layout.setupWithViewPager(view_pager)

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        // display username and picture

        refUsers!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {

                    val user: Users? = p0.getValue(Users::class.java)

                    user_name.text = user!!.username
                    Picasso.get().load(user.profile).placeholder(R.drawable.profile).into(profile_image)

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

                return true
            }
        }
        return false
    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager) {

        private val fragments: ArrayList<Fragment> = ArrayList()
        private val titles: ArrayList<String> = ArrayList()

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
            notifyDataSetChanged()
        }

        override fun getPageTitle(i: Int): CharSequence? {
            return titles[i]
        }

    }

    private fun updateStatus(status: String) {

        val ref = FirebaseGlobalValue().ref.child("Users").child(firebaseUser!!.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        ref.updateChildren(hashMap)

    }

    override fun onResume() {
        super.onResume()

        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()

        updateStatus("offline")
    }

}