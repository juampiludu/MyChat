package com.ludev.mychat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ludev.mychat.AdapterClasses.UserAdapter
import com.ludev.mychat.ModelClasses.Users
import java.util.*
import kotlin.collections.ArrayList

class SearchActivity : AppCompatActivity() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var recyclerView: RecyclerView? = null
    private var searchUsersEditText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(findViewById(R.id.search_toolbar))

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.search_list)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(this)

        searchUsersEditText = findViewById(R.id.search_users_et)


        mUsers = ArrayList()


        searchUsersEditText!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

            }

            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {
                searchForUsers(cs.toString().toLowerCase(Locale.ROOT), count)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

        })

    }

    private fun searchForUsers(str: String, count: Int) {

        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUsers = FirebaseGlobalValue().ref
            .child("Users")
            .orderByChild("search")
            .startAt(str)

        if (count > 0) {

            queryUsers.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.exists()) {

                        (mUsers as ArrayList<Users>).clear()
                        for (snapshot in p0.children) {
                            val user: Users? = snapshot.getValue(Users::class.java)
                            if (user!!.uid != firebaseUserID) {
                                (mUsers as ArrayList<Users>).add(user)
                            }
                        }
                        userAdapter = UserAdapter(this@SearchActivity, mUsers!!, false)
                        recyclerView!!.adapter = userAdapter
                        userAdapter!!.notifyDataSetChanged()

                    }

                }

                override fun onCancelled(p0: DatabaseError) {

                }

            })
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}