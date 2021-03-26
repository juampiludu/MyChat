package com.ludev.mychat.AdapterClasses

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
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
import java.text.SimpleDateFormat
import java.util.*

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
    private var lastMsgUser: Boolean? = null
    private var lastMsgSeen: Boolean? = null

    init {
        this.mContext = mContext
        this.mUsers = mUsers
        this.isChatCheck = isChatCheck
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(
            R.layout.user_search_item_layout,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user: Users = mUsers[position]
        holder.userNameTxt!!.text = user.name
        Picasso.get().load(user.profile).placeholder(R.drawable.profile).into(holder.profileImageView)

        if (isChatCheck) {
            retrieveLastMessage(
                user.uid,
                holder.lastMessageTxt!!,
                holder.iconSeen!!,
                holder.lastMessageTime!!
            )
            retrieveUnreadMessages(user.uid, holder.unreadMessagesRl!!, holder.unreadMessagesTxt!!)
        }
        else {
            holder.lastMessageTxt!!.text = "@" + user.username
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
        var iconSeen: ImageView? = itemView.findViewById(R.id.icon_seen)
        var unreadMessagesRl: RelativeLayout? = itemView.findViewById(R.id.unread_messages_rl)
        var unreadMessagesTxt: TextView? = itemView.findViewById(R.id.unread_messages_txt)
        var lastMessageTime: TextView? = itemView.findViewById(R.id.last_message_time)

    }

    private fun retrieveLastMessage(
        chatUserId: String,
        lastMessageTxt: TextView,
        iconSeen: ImageView,
        lastMessageTime: TextView
    ) {

        lastMsg = "default"
        lastMsgUser = false
        lastMsgSeen = false
        var lastMsgMillis: Long = 0
        var lastMsgDate = ""

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
                            chat.sender == firebaseUsers.uid
                        ) {

                            lastMsg = chat.message
                            lastMsgUser = chat.sender == firebaseUsers.uid
                            lastMsgSeen = chat.seen
                            lastMsgMillis = chat.timeInMillis
                            lastMsgDate = chat.time

                        }

                    }
                }
                when (lastMsg) {
                    "default" -> lastMessageTxt.text = ""
                    "sent you a photo." -> lastMessageTxt.text = "Photo"
                    else -> lastMessageTxt.text = lastMsg
                }
                lastMsg = "default"

                when (lastMsgUser) {
                    true -> iconSeen.visibility = View.VISIBLE
                    false -> iconSeen.visibility = View.GONE
                }
                lastMsgUser = false

                when (lastMsgSeen) {
                    true -> iconSeen.setImageResource(R.drawable.ic_seen)
                    false -> iconSeen.setImageResource(R.drawable.ic_sent)
                }
                lastMsgSeen = false

                // Checking last message date

                val year = SimpleDateFormat("yyy", Locale.ENGLISH)
                val day = SimpleDateFormat("yyyMMdd", Locale.ENGLISH)

                //

                val currentTime = Calendar.getInstance().timeInMillis
                val msgTime = lastMsgMillis
                val strToDate = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

                //

                val y1 = year.format(currentTime)
                val y2 = year.format(msgTime)

                val w: Date?
                var sameWeek: Boolean? = null

                if (lastMsgDate != "") {
                    w = strToDate.parse(lastMsgDate)
                    sameWeek = w?.let { isDateInCurrentWeek(it) }
                }

                val d1 = day.format(currentTime)
                val d2 = day.format(msgTime)

                val sameYear = checkDate(y1, y2)
                val sameDay = checkDate(d1, d2)
                val yesterday = checkYesterday(d1.toInt(), d2.toInt())

                when (sameWeek != null) {
                    sameDay -> {
                        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                        lastMessageTime.visibility = View.VISIBLE
                        lastMessageTime.text = format.format(msgTime)
                    }
                    yesterday -> {
                        lastMessageTime.visibility = View.VISIBLE
                        lastMessageTime.text = mContext.resources.getString(R.string.yesterday)
                    }
                    sameWeek!!-> {
                        val format = SimpleDateFormat("EEEE", Locale.getDefault())
                        lastMessageTime.visibility = View.VISIBLE
                        lastMessageTime.text = format.format(msgTime)
                    }
                    sameYear -> {
                        val format = SimpleDateFormat("MMM d", Locale.getDefault())
                        lastMessageTime.visibility = View.VISIBLE
                        lastMessageTime.text = format.format(msgTime)
                    }
                    else -> {
                        val format = if (mContext.resources.configuration.locale == Locale.ENGLISH) {
                            SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
                        }
                        else {
                            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                        }
                        lastMessageTime.visibility = View.VISIBLE
                        lastMessageTime.text = format.format(msgTime)
                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    private fun retrieveUnreadMessages(
        chatUserId: String,
        unreadMessagesRl: RelativeLayout,
        unreadMessagesTxt: TextView
    ) {

        val ref = FirebaseGlobalValue().ref.child("Chats")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {

                    //Log.e("Count", p0.childrenCount.toString())
                    var count = 0

                    for (i in p0.children) {

                        val chat = i.getValue(Chat::class.java)

                        if (chat!!.sender == chatUserId) {
                            //Log.e("Message from", chat.sender)

                            if (!chat.seen) {
                                count += 1
                                //Log.e("${chat.sender} count", count.toString())
                            }

                            when {
                                count > 0 -> {
                                    unreadMessagesRl.visibility = View.VISIBLE
                                    unreadMessagesTxt.text = count.toString()
                                }
                                count >= 1000 -> {
                                    unreadMessagesRl.visibility = View.VISIBLE
                                    unreadMessagesTxt.text = "999+"
                                }
                                else -> {
                                    unreadMessagesRl.visibility = View.GONE
                                    unreadMessagesTxt.text = ""
                                }
                            }

                        }

                    }

                }

            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(
                    mContext,
                    mContext.resources.getString(R.string.error_occurred),
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(p0.message, p0.details)
            }
        })

    }

    fun checkDate(a: String, b: String): Boolean {
        return a == b
    }

    fun checkYesterday(a: Int, b: Int): Boolean {
        return (a - 1) == b
    }

    fun isDateInCurrentWeek(date: Date): Boolean {
        val currentCalendar = Calendar.getInstance()
        val week = currentCalendar[Calendar.WEEK_OF_YEAR]
        val year = currentCalendar[Calendar.YEAR]
        val targetCalendar = Calendar.getInstance()
        targetCalendar.time = date
        val targetWeek = targetCalendar[Calendar.WEEK_OF_YEAR]
        val targetYear = targetCalendar[Calendar.YEAR]
        return week == targetWeek && year == targetYear
    }

}
