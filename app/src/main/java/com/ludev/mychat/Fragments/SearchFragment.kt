package com.ludev.mychat.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ludev.mychat.AdapterClasses.UserAdapter
import com.ludev.mychat.FirebaseGlobalValue
import com.ludev.mychat.ModelClasses.Users
import com.ludev.mychat.R
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var recyclerView: RecyclerView? = null
    private var searchUsersEditText: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.search_list)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        searchUsersEditText = view.findViewById(R.id.search_users_et)


        mUsers = ArrayList()


        searchUsersEditText!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

            }

            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {

                searchForUsers(cs.toString().toLowerCase(Locale.ROOT))

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

        })


        return view
    }

    private fun searchForUsers(str: String) {

        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUsers = FirebaseGlobalValue().ref
            .child("Users")
            .orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

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
                    activity?.let {
                        userAdapter = UserAdapter(context!!, mUsers!!, false)
                        recyclerView!!.adapter = userAdapter
                        userAdapter!!.notifyDataSetChanged()
                    }

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

    }

}