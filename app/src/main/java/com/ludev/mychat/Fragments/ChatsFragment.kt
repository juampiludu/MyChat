package com.ludev.mychat.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.ludev.mychat.AdapterClasses.UserAdapter
import com.ludev.mychat.FirebaseGlobalValue
import com.ludev.mychat.ModelClasses.ChatList
import com.ludev.mychat.ModelClasses.Users
import com.ludev.mychat.Notifications.Token
import com.ludev.mychat.R
import kotlinx.android.synthetic.main.fragment_chats.view.*

/**
 * A simple [Fragment] subclass.
 */
class ChatsFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var usersChatList: List<ChatList>? = null
    private lateinit var recyclerViewChatList: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        recyclerViewChatList = view.recycler_view_chat_list
        recyclerViewChatList.setHasFixedSize(true)
        recyclerViewChatList.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()

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
            }
        })

        updateToken(FirebaseInstanceId.getInstance().token)

        return view
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
                    activity?.let {
                        userAdapter = UserAdapter(context!!, (mUsers as ArrayList<Users>), true)
                        recyclerViewChatList.adapter = userAdapter
                        userAdapter!!.notifyDataSetChanged()
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

}