package com.ludev.mychat.Fragments

import `in`.championswimmer.libsocialbuttons.FabSocial
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.ludev.mychat.FirebaseGlobalValue
import com.ludev.mychat.ModelClasses.Users
import com.ludev.mychat.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {

    private var usersReference: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null
    private val RequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var editUsername: String = ""
    private var editPhone: String = ""
    private var editFacebook: String = ""
    private var editInstagram: String = ""
    private var editTwitter: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersReference = FirebaseGlobalValue().ref.child(
            "Users"
        ).child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("Users Images")

        usersReference!!.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {

                    val user: Users? = p0.getValue(Users::class.java)

                    activity?.let {
                        if (context != null) {
                            view.username_settings.text = user!!.username
                            view.set_email.text = user.email
                            view.set_phone.text = user.phone
                            editUsername = user.username
                            editPhone = user.phone
                            editFacebook = user.facebook
                            editInstagram = user.instagram
                            editTwitter = user.twitter
                            Picasso.get().load(user.profile).into(view.profile_image_settings)
                        }
                    }

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

        view.edit_profile_photo_btn.setOnClickListener {
            pickImage()
        }

        socialMedia(view.set_facebook, editFacebook)
        socialMedia(view.set_instagram, editInstagram)
        socialMedia(view.set_twitter, editTwitter)

        view.edit_profile_btn.setOnClickListener {
            editProfile(editUsername, editPhone, editFacebook, editInstagram, editTwitter)
        }

        return view
    }

    private fun editProfile(username: String, phone: String, facebook: String, instagram: String, twitter: String) {

        val inflater = layoutInflater

        val inflateView = inflater.inflate(R.layout.edit_profile_view, null)

        val editTextUsername: TextInputEditText = inflateView.findViewById(R.id.edit_username)
        val editTextPhone: TextInputEditText = inflateView.findViewById(R.id.edit_phone)
        val editTextFacebook: TextInputEditText = inflateView.findViewById(R.id.edit_facebook)
        val editTextInstagram: TextInputEditText = inflateView.findViewById(R.id.edit_instagram)
        val editTextTwitter: TextInputEditText = inflateView.findViewById(R.id.edit_twitter)

        editTextUsername.setText(username)
        editTextPhone.setText(phone)
        editTextFacebook.setText(facebook)
        editTextInstagram.setText(instagram)
        editTextTwitter.setText(twitter)

        editTextFacebook.text!!.delete(0,25)
        editTextInstagram.text!!.delete(0,26)
        editTextTwitter.text!!.delete(0,24)

        val builder =
            MaterialAlertDialogBuilder(inflateView.context, R.style.AlertDialogCustom)

        builder.setView(inflateView)
        builder.setPositiveButton("Save") { _, _ ->

            if (editTextUsername.text.isNullOrEmpty()) {
                editTextUsername.error = "Complete this field"
            }
            else {
                updateProfile(
                    editTextUsername.text.toString(),
                    editTextPhone.text.toString(),
                    editTextFacebook.text.toString(),
                    editTextInstagram.text.toString(),
                    editTextTwitter.text.toString()
                )
            }


        }
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()

        editTextUsername.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (editTextUsername.text.isNullOrEmpty()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorPrimary))
                }
                else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorRedLight))
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


    }

    private fun updateProfile(username: String, phone: String, facebook: String, instagram: String, twitter: String) {

        val hashMap = HashMap<String, Any>()
        hashMap["username"] = username
        hashMap["search"] = username.toLowerCase()
        hashMap["phone"] = phone
        hashMap["facebook"] = "https://www.facebook.com/$facebook"
        hashMap["instagram"] = "https://www.instagram.com/$instagram"
        hashMap["twitter"] = "https://www.twitter.com/$twitter"

        usersReference!!.updateChildren(hashMap).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun pickImage() {

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {

            imageUri = data.data
            Toast.makeText(context, "Uploading...", Toast.LENGTH_LONG).show()
            uploadImageToDatabase()

        }

    }

    private fun uploadImageToDatabase() {

        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Image is uploading, please wait...")
        progressBar.show()

        if (imageUri != null) {
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

            val uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->

                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }

                return@Continuation fileRef.downloadUrl

            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val mapProfileImg = HashMap<String, Any>()
                    mapProfileImg["profile"] = url
                    usersReference!!.updateChildren(mapProfileImg)

                    progressBar.dismiss()
                }
            }
        }

    }

    private fun socialMedia(buttonDisplay: FabSocial, socialMedia: String) {

        buttonDisplay.setOnClickListener {

            if (socialMedia == "" ||
                socialMedia == "https://www.facebook.com/" ||
                socialMedia == "https://www.instagram.com/" ||
                socialMedia == "https://www.twitter.com/"
            ) {
                Toast.makeText(context, "You have not added this social media. Add it by clicking \"Edit profile\" button.", Toast.LENGTH_LONG).show()
            }
            else {

                val uri = Uri.parse(socialMedia)

                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)

            }

        }

    }

}