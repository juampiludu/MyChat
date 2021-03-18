package com.ludev.mychat.AdapterClasses

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ludev.mychat.FirebaseGlobalValue
import com.ludev.mychat.ModelClasses.Chat
import com.ludev.mychat.R
import com.ludev.mychat.ViewFullImageActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.message_item_left.view.*
import kotlinx.android.synthetic.main.message_item_right.view.*
import kotlinx.android.synthetic.main.message_item_right.view.show_date_rl
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val mContext: Context,
    private val mChatList: List<Chat>
) : RecyclerView.Adapter<ChatAdapter.ViewHolder?>() {

    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private val URL_REGEX =
        "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$"

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {

        return if (position == 1) {
            val view: View = LayoutInflater.from(mContext).inflate(
                R.layout.message_item_right,
                parent,
                false
            )
            ViewHolder(view)
        }
        else {
            val view: View = LayoutInflater.from(mContext).inflate(
                R.layout.message_item_left,
                parent,
                false
            )
            ViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val chat: Chat = mChatList[position]

        // images messages
        if (chat.url != "" && chat.message == "sent you a photo.") {

            // image message - right side
            if (chat.sender == firebaseUser.uid) {
                holder.show_text_message!!.visibility = View.GONE
                holder.right_image_view!!.visibility = View.VISIBLE
                Picasso.get().load(chat.url).into(holder.right_image_view)

                holder.right_image_view!!.setOnLongClickListener {

                    val builder = MaterialAlertDialogBuilder(
                        holder.itemView.context,
                        R.style.AlertDialogCustom
                    )
                    builder.setTitle("Delete image")
                    builder.setMessage("Are you sure to delete this image?")
                    builder.setPositiveButton("Delete") { _, _ ->

                        deleteSentMessage(position, holder)

                    }
                    builder.setNegativeButton("Cancel") { _, _ ->}
                    builder.show()

                    return@setOnLongClickListener true
                }

                holder.right_image_view!!.setOnClickListener {

                    val intent = Intent(mContext, ViewFullImageActivity::class.java)
                    intent.putExtra("url", chat.url)
                    intent.putExtra("user_id", chat.sender)
                    mContext.startActivity(intent)

                }

            }
            // image message - left side
            else if (chat.sender != firebaseUser.uid) {
                holder.show_text_message!!.visibility = View.GONE
                holder.left_image_view!!.visibility = View.VISIBLE
                Picasso.get().load(chat.url).into(holder.left_image_view)

                holder.left_image_view!!.setOnClickListener {

                    val intent = Intent(mContext, ViewFullImageActivity::class.java)
                    intent.putExtra("url", chat.url)
                    intent.putExtra("user_id", chat.sender)
                    mContext.startActivity(intent)

                }

            }

        }
        // text messages
        else {
            holder.show_text_message!!.text = chat.message
            holder.text_show_time!!.text = chat.showTime

            if (firebaseUser.uid == chat.sender) {

                holder.itemView.item_right_ln.setOnClickListener {
                    val builder = MaterialAlertDialogBuilder(
                        holder.itemView.context,
                        R.style.AlertDialogCustom
                    )
                    builder.setTitle("Delete message")
                    builder.setMessage("Are you sure to delete this message?")
                    builder.setPositiveButton("Delete") { _, _ ->

                        deleteSentMessage(position, holder)

                    }
                    builder.setNegativeButton("Cancel") { _, _ ->}
                    builder.show()
                }

                // Copy text from TextView

                if (chat.url == "" && chat.message != "sent you a photo.") {
                    holder.itemView.item_right_ln.setOnLongClickListener {

                        val text = holder.show_text_message!!.text.toString()
                        val clip: ClipData = ClipData.newPlainText(text, text)
                        val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                        clipboard.setPrimaryClip(clip)

                        Toast.makeText(mContext, "Message copied to clipboard.", Toast.LENGTH_SHORT).show()

                        return@setOnLongClickListener true

                    }
                }

            }
            else {

                // Copy text from TextView

                holder.itemView.item_left_ln.setOnLongClickListener {

                    val text = holder.show_text_message!!.text.toString()
                    val clip: ClipData = ClipData.newPlainText("text", text)
                    val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(mContext, "Message copied to clipboard.", Toast.LENGTH_SHORT).show()

                    return@setOnLongClickListener true

                }

            }


        }

        // sent and seen messages

        if (chat.seen) {
            holder.icon_seen!!.setImageResource(R.drawable.ic_seen)
            holder.icon_seen!!.visibility = View.VISIBLE

            /*if (chat.url != "" && chat.message == "sent you a photo.") {

            }*/

            if (firebaseUser.uid == chat.receiver) {
                holder.icon_seen!!.visibility = View.GONE
            }

        }
        else {
            holder.icon_seen!!.setImageResource(R.drawable.ic_sent)
            holder.icon_seen!!.visibility = View.VISIBLE

            /*if (chat.url != "" && chat.message == "sent you a photo.") {

            }*/
        }

        // Group messages by date

        val calendar = Calendar.getInstance()
        val mess = mChatList[position]
        calendar.timeInMillis = mess.timeInMillis

        val msg = mChatList[position].timeInMillis
        val dateFormat = SimpleDateFormat("MMMM d, yyyy").format(msg)

        val date = calendar.get(Calendar.DAY_OF_MONTH)

        if (position > 0) {

            val previousMessage = mChatList[position - 1]

            calendar.timeInMillis = previousMessage.timeInMillis
            val prevDate = calendar.get(Calendar.DAY_OF_MONTH)

            if (date > prevDate) {
                // This is a different day than the previous message, so show the Date
                holder.itemView.show_date_rl!!.visibility = View.VISIBLE
                holder.show_date!!.text = dateFormat
            }
            else {
                // Same day, so hide the Date
                holder.itemView.show_date_rl!!.visibility = View.GONE
                holder.show_date!!.text = ""
            }

        }
        else {
            // This is the first message, so show the date
            holder.itemView.show_date_rl!!.visibility = View.VISIBLE
            holder.show_date!!.text = dateFormat
        }

    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var show_text_message: TextView? = itemView.findViewById(R.id.show_text_message)
        var left_image_view: ImageView? = itemView.findViewById(R.id.left_image_view)
        var text_show_time: TextView? = itemView.findViewById(R.id.text_show_time)
        var right_image_view: ImageView? = itemView.findViewById(R.id.right_image_view)
        var icon_seen: ImageView? = itemView.findViewById(R.id.icon_seen)
        var show_date: TextView? = itemView.findViewById(R.id.show_date)

    }

    override fun getItemViewType(position: Int): Int {

        return if (mChatList[position].sender == firebaseUser.uid) {
            1
        }
        else {
            0
        }

    }

    private fun deleteSentMessage(position: Int, holder: ChatAdapter.ViewHolder) {

        val ref = FirebaseGlobalValue().ref
            .child("Chats").child(mChatList[position].messageId)
            .removeValue()
        ref.addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    Toast.makeText(
                        holder.itemView.context,
                        "Message deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Error: ${task.exception.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

    }

}
