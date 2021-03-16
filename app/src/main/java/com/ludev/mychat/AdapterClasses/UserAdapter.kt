package com.ludev.mychat.AdapterClasses

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ludev.mychat.FirebaseGlobalValue
import com.ludev.mychat.MessageChatActivity
import com.ludev.mychat.ModelClasses.Chat
import com.ludev.mychat.ModelClasses.Users
import com.ludev.mychat.R
import com.ludev.mychat.VisitUserProfileActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    mContext: Context,
    mUsers: List<Users>,
    isChatCheck: Boolean
    ) : RecyclerView.Adapter<UserAdapter.ViewHolder?>()
{

    private val mContext: Context
    private val mUsers: List<Users>
    private var isChatCheck: Boolean
    private var lastMsg: String = ""

    init {
        this.mContext = mContext
        this.mUsers = mUsers
        this.isChatCheck = isChatCheck
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user: Users = mUsers[position]
        holder.userNameTxt!!.text = user.username
        Picasso.get().load(user.profile).placeholder(R.drawable.profile).into(holder.profileImageView)

        if (isChatCheck) {
            holder.lastMessageTxt!!.visibility = View.VISIBLE
            retrieveLastMessage(user.uid, holder.lastMessageTxt!!)
        }
        else {
            holder.lastMessageTxt!!.visibility = View.GONE
        }

        if (isChatCheck) {
            if (user.status == "online") {
                holder.onlineIcon!!.visibility = View.VISIBLE
                holder.offlineIcon!!.visibility = View.GONE
            }
            else {
                holder.onlineIcon!!.visibility = View.GONE
                holder.offlineIcon!!.visibility = View.VISIBLE
            }
        }
        else {
            holder.onlineIcon!!.visibility = View.GONE
            holder.offlineIcon!!.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {

            val intent = Intent(mContext, MessageChatActivity::class.java)
            intent.putExtra("visit_id", user.uid)
            mContext.startActivity(intent)

            /*val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit profile"
            )
            val builder: AlertDialog.Builder =
                AlertDialog.Builder(mContext)
            builder.setTitle("What do you want?")
            builder.setItems(options) { _, which ->

                if (which == 0) {
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id", user.uid)
                    mContext.startActivity(intent)
                }

                if (which == 1) {
                    Toast.makeText(mContext, "Soon", Toast.LENGTH_LONG).show()
                }

            }
            builder.show()*/
        }

        holder.profileImageView!!.setOnClickListener {

            val intent = Intent(mContext, VisitUserProfileActivity::class.java)
            intent.putExtra("visit_id", user.uid)
            mContext.startActivity(intent)

        }

    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameTxt: TextView? = itemView.findViewById(R.id.username)
        var profileImageView: CircleImageView? = itemView.findViewById(R.id.profile_image)
        var onlineIcon: CircleImageView? = itemView.findViewById(R.id.image_online)
        var offlineIcon: CircleImageView? = itemView.findViewById(R.id.image_offline)
        var lastMessageTxt: TextView? = itemView.findViewById(R.id.message_last)

    }

    private fun retrieveLastMessage(chatUserId: String, lastMessageTxt: TextView) {

        lastMsg = "default"

        val firebaseUsers = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseGlobalValue().ref.child("Chats")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children) {
                    val chat: Chat? = dataSnapshot.getValue(Chat::class.java)

                    if (firebaseUsers != null && chat != null) {

                        if (chat.receiver == firebaseUsers.uid &&
                            chat.sender == chatUserId ||
                            chat.receiver == chatUserId &&
                            chat.sender == firebaseUsers.uid) {

                            lastMsg = chat.message

                        }

                    }
                }
                when (lastMsg) {
                    "default" -> lastMessageTxt.text = ""
                    "sent you a photo." -> lastMessageTxt.text = "Photo"
                    else -> lastMessageTxt.text = lastMsg
                }
                lastMsg = "default"
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

}
