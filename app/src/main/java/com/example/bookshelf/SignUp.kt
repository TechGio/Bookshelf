package com.example.bookshelf

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookshelf.databinding.ActivitySignUpBinding
import com.example.bookshelf.ui.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var editeditNickname: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editConfirmPassword: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private lateinit var userList: ArrayList<User>
    var count = 0
    val SharedPreferenceDataToSave : String = "SharedPreferenceDataToSave"
    lateinit var  sharedpreferences: SharedPreferences
    val emailKey: String = "email"
    val passwordKey: String = "password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mDBRef = FirebaseDatabase.getInstance().getReference()
        userList = ArrayList()
        editeditNickname = binding.editNickname
        editEmail = binding.editEmail
        editPassword = binding.editPassword
        editConfirmPassword = binding.editConfirmPassword
        mAuth = FirebaseAuth.getInstance()
        sharedpreferences = getSharedPreferences(SharedPreferenceDataToSave, Context.MODE_PRIVATE)

        binding.createAccountBtn.setOnClickListener{
            val nickname = editeditNickname.text.toString()
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            val confirmPassword = editConfirmPassword.text.toString()

            signUp(nickname.lowercase(), email, password, confirmPassword)
        }

        binding.cancelBtn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signUp(nickname: String, email: String, password: String, confirmPassword: String) {
        try {
            mDBRef.child("user").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(count == 0){
                        var presentNickname = false
                        var presentEmail = false
                        userList.clear()
                        for (postSnapshot in snapshot.children) {
                            val currentUser = postSnapshot.getValue(User::class.java)
                            if (currentUser!!.nickname == nickname)
                                presentNickname = true
                            if (currentUser!!.email == email)
                                presentEmail = true
                        }
                        if(nickname != null && nickname != ""){
                            if (!presentNickname) {
                                if(email != null && email != ""){
                                    if (!presentEmail) {
                                        if(password != null && password != ""){
                                            if (password == confirmPassword) {
                                                if(password.length >= 8){
                                                    mAuth.createUserWithEmailAndPassword(email, password)
                                                        .addOnCompleteListener(this@SignUp) { task ->
                                                            if (task.isSuccessful) {
                                                                count++
                                                                Toast.makeText(
                                                                    baseContext, "Registration successful.",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                val user =
                                                                    FirebaseAuth.getInstance().currentUser
                                                                val profileUpdates =
                                                                    UserProfileChangeRequest.Builder()
                                                                        .setDisplayName(nickname)
                                                                        .build()
                                                                user!!.updateProfile(profileUpdates)
                                                                addUserToDatabase(nickname, email, mAuth.currentUser?.uid!!)
                                                                saveData(email, password)
                                                                val intent = Intent(this@SignUp, MainActivity::class.java)
                                                                startActivity(intent)
                                                                finish()
                                                            } else {
                                                                if(count == 0){
                                                                    Log.w(
                                                                        "AppErrorSignUp",
                                                                        "createUserWithEmail:failure",
                                                                        task.exception
                                                                    )
                                                                    Toast.makeText(
                                                                        baseContext, "Authentication failed.",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        }
                                                } else {
                                                    if(count == 0)
                                                        Toast.makeText(
                                                            baseContext, "Password must be at least 8 characters long!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                }
                                            } else {
                                                if (count == 0)
                                                    Toast.makeText(
                                                        baseContext, "Password are different! Please retry.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                            }
                                        } else{
                                            if (count == 0)
                                                Toast.makeText(
                                                    baseContext, "Please enter your password",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                        }
                                    } else {
                                        if (count == 0)
                                            Toast.makeText(
                                                baseContext, "Email already in use.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                    }
                                } else {
                                    if (count == 0)
                                        Toast.makeText(
                                            baseContext, "Please enter your email",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                }
                            } else {
                                if (count == 0)
                                    Toast.makeText(
                                        baseContext, "Nickname already taken. Please choose another one",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                        } else {
                            if (count == 0)
                                Toast.makeText(
                                    baseContext, "Please enter your nickname",
                                    Toast.LENGTH_SHORT
                                ).show()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        } catch (e: Exception){
            Toast.makeText(this, "An error occurred. Please try again", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun addUserToDatabase(nickname: String, email: String, uid: String) {
        mDBRef = FirebaseDatabase.getInstance().getReference()
        mDBRef.child("user").child(uid).setValue(User(nickname, email, uid))
    }

    private fun saveData(email: String, password: String){
        var editor: SharedPreferences.Editor = sharedpreferences.edit()
        editor.putString(emailKey, email)
        editor.putString(passwordKey, password)
        editor.commit()
    }
}