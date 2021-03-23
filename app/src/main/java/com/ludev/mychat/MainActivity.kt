package com.ludev.mychat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.ludev.mychat.AdapterClasses.UserAdapter
import com.ludev.mychat.ModelClasses.ChatList
import com.ludev.mychat.ModelClasses.Users
import com.ludev.mychat.Notifications.Token
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var refUsers: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var usersChatList: List<ChatList>? = null
    private lateinit var recyclerViewChatList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseGlobalValue().ref.child("Users").child(firebaseUser!!.uid)
        recyclerViewChatList = recycler_view_chat_list
        recyclerViewChatList.setHasFixedSize(true)
        recyclerViewChatList.layoutManager = LinearLayoutManager(this)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()

        supportActionBar!!.title = ""

        val ref = FirebaseGlobalValue().ref.child("ChatList").child(firebaseUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {

                    (usersChatList as ArrayList).clear()

                    for (dataSnapshot in p0.children) {
                        val chatList = dataSnapshot.getValue(ChatList::class.java)

                        (usersChatList as ArrayList).add(chatList!!)
                    }
                    retrieveChatList()

                }

            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error: ${p0.toException()}", Toast.LENGTH_LONG).show()
                Log.e("Error MainActivity ref", p0.message)
            }

        })

        updateToken(FirebaseInstanceId.getInstance().token)

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

                val builder = MaterialAlertDialogBuilder(this, R.style.AlertDialogCustom)

                builder.setTitle("Are you sure to logout from this account?")
                builder.setPositiveButton("Logout") {_,_ ->

                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()

                }
                builder.setNegativeButton("Cancel") {_,_ ->}
                builder.show()

                return true
            }
            R.id.action_search -> {

                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)

                return true

            }
            R.id.action_settings -> {

                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)

                return true

            }
        }
        return false
    }

    private fun updateStatus(status: String) {

        val ref = FirebaseGlobalValue().ref.child("Users").child(firebaseUser!!.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        ref.updateChildren(hashMap)

    }

    override fun onStart() {
        super.onStart()

        updateStatus("online")
    }

    override fun onDestroy() {
        super.onDestroy()

        updateStatus("offline")
    }

    private fun updateToken(token: String?) {
        val ref = FirebaseGlobalValue().ref.child("Tokens")
        val token1 = Token(token!!)
        ref.child(firebaseUser!!.uid).setValue(token1)
    }

    private fun retrieveChatList() {

        mUsers = ArrayList()

        val ref = FirebaseGlobalValue().ref.child("Users")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {

                    (mUsers as ArrayList).clear()

                    for (dataSnapshot in p0.children) {
                        val user = dataSnapshot.getValue(Users::class.java)

                        for (eachChatList in usersChatList!!) {
                            if (user!!.uid == eachChatList.id) {
                                (mUsers as ArrayList).add(user)
                            }
                        }
                    }

                    userAdapter = UserAdapter(this@MainActivity, (mUsers as ArrayList<Users>), true)
                    recyclerViewChatList.adapter = userAdapter
                    userAdapter!!.notifyDataSetChanged()

                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@MainActivity, "An error has occurred. Try again later.", Toast.LENGTH_SHORT).show()
                Log.e(p0.message, p0.details)
            }
        })

    }

}