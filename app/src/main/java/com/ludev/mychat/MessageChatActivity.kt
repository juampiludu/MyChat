package com.ludev.mychat

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.ludev.mychat.AdapterClasses.ChatAdapter
import com.ludev.mychat.Fragments.ApiService
import com.ludev.mychat.ModelClasses.Chat
import com.ludev.mychat.ModelClasses.Users
import com.ludev.mychat.Notifications.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MessageChatActivity : AppCompatActivity() {

    private var userIdVisit: String = ""
    private var firebaseUser: FirebaseUser? = null
    private var chatAdapter: ChatAdapter? = null
    private var mChatList: List<Chat>? = null
    lateinit var recyclerViewChats: RecyclerView
    private var reference: DatabaseReference? = null

    var notify = false
    var apiService: ApiService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(ApiService::class.java)

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recyclerViewChats = findViewById(R.id.recycler_view_chats)
        recyclerViewChats.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerViewChats.layoutManager = linearLayoutManager

        reference = FirebaseGlobalValue().ref
            .child("Users").child(userIdVisit)
        reference!!.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {

                val user: Users? = p0.getValue(Users::class.java)

                username_mchat.text = user!!.username
                Picasso.get().load(user.profile).into(profile_image_mchat)

                if (user.status == "online") {
                    user_status.text = "online"
                    user_status.setTextColor(resources.getColor(R.color.colorRed))
                }
                else {
                    user_status.text = "offline"
                    user_status.setTextColor(resources.getColor(android.R.color.darker_gray))
                }

                retrieveMessages(firebaseUser!!.uid, userIdVisit, user.profile)

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        text_message.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (text_message.text.toString() == "") {
                    send_message_btn.visibility = View.GONE
                }
                else {
                    send_message_btn.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        send_message_btn.setOnClickListener {

            notify = true

            val message = text_message.text.toString()
            if (message != "") {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            text_message.setText("")

        }

        attach_image_file_btn.setOnClickListener {

            notify = true

            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Image"), 438)

        }

        toolbar_message_chat.setNavigationOnClickListener {
            onBackPressed()
        }

        seenMessage(userIdVisit)

        text_message.requestFocus()
        if(text_message.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
        }

    }

    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {

        val reference = FirebaseGlobalValue().ref
        val messageKey = reference.push().key
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentTime = String.format("%02d:%02d", hour, minute)

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["seen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        messageHashMap["time"] = calendar.time.toString()
        messageHashMap["timeInMillis"] = System.currentTimeMillis()
        messageHashMap["showTime"] = currentTime
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val chatsListReference = FirebaseGlobalValue().ref
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)

                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{

                        override fun onDataChange(p0: DataSnapshot) {

                            if (!p0.exists()) {
                                chatsListReference.child("id").setValue(userIdVisit)
                            }

                            val chatsListReceiverReference = FirebaseGlobalValue().ref
                                .child("ChatList")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)
                            chatsListReceiverReference.child("id").setValue(firebaseUser!!.uid)

                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })

                }
            }

        // implement push notifications
        val ref = FirebaseGlobalValue().ref
            .child("Users").child(firebaseUser!!.uid)

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(Users::class.java)
                if (notify) {
                    sendNotification(receiverId, user!!.username, message)
                }
                notify = false

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    private fun sendNotification(receiverId: String?, username: String, message: String) {

        val ref = FirebaseGlobalValue().ref
            .child("Tokens")

        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                for (dataSnapshot in p0.children) {
                    val token: Token? = dataSnapshot.getValue(Token::class.java)

                    val data = Data(firebaseUser!!.uid, R.mipmap.ic_launcher, "$username: $message", "New Message", userIdVisit)

                    val sender = Sender(data, token!!.token)

                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyResponse>{

                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if (response.code() == 200) {

                                    if (response.body()!!.success != 1) {

                                        Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()

                                    }

                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                            }
                        })
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null) {

            val loadingBar = ProgressDialog(this)
            loadingBar.setMessage("Sending image...")
            loadingBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseGlobalValue().ref
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->

                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }

                return@Continuation filePath.downloadUrl

            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)
                    val currentTime = String.format("%02d:%02d", hour, minute)

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you a photo."
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["seen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId
                    messageHashMap["time"] = calendar.time.toString()
                    messageHashMap["timeInMillis"] = System.currentTimeMillis()
                    messageHashMap["showTime"] = currentTime

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                loadingBar.dismiss()

                                // implement push notifications
                                val reference = FirebaseGlobalValue().ref
                                    .child("Users").child(firebaseUser!!.uid)

                                reference.addValueEventListener(object : ValueEventListener{
                                    override fun onDataChange(p0: DataSnapshot) {

                                        val user = p0.getValue(Users::class.java)
                                        if (notify) {
                                            sendNotification(userIdVisit, user!!.username, "sent you a photo.")
                                        }
                                        notify = false

                                    }

                                    override fun onCancelled(p0: DatabaseError) {

                                    }
                                })
                            }
                        }

                }
            }

        }

    }

    private fun retrieveMessages(senderId: String, receiverId: String?, receiverImageUrl: String) {

        mChatList = ArrayList()
        val reference = FirebaseGlobalValue().ref.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in p0.children) {

                    val chat = snapshot.getValue(Chat::class.java)

                    if (chat!!.receiver == senderId && chat.sender == receiverId || chat.receiver == receiverId && chat.sender == senderId) {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatAdapter = ChatAdapter(
                        this@MessageChatActivity,
                        (mChatList as ArrayList<Chat>)
                    )
                    recyclerViewChats.adapter = chatAdapter

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

    }

    private var seenListener: ValueEventListener? = null

    private fun seenMessage(userId: String) {

        val reference = FirebaseGlobalValue().ref.child("Chats")

        seenListener = reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children) {

                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if (chat!!.receiver == firebaseUser!!.uid && chat.sender == userId) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["seen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    override fun onPause() {
        super.onPause()

        reference!!.removeEventListener(seenListener!!)

    }

}