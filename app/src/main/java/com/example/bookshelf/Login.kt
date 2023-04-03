package com.example.bookshelf

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.example.bookshelf.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var mAuth: FirebaseAuth
    val emailKey: String = "email"
    val passwordKey: String = "password"
    val SharedPreferenceDataToSave : String = "SharedPreferenceDataToSave"
    lateinit var  sharedpreferences: SharedPreferences
    var savedEmail: String? = null
    var savedPassword: String? = null
    var emailPlaceholder: String? = null
    var passwordPlaceholder: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        sharedpreferences = getSharedPreferences(SharedPreferenceDataToSave, Context.MODE_PRIVATE)

        emailPlaceholder = intent.getStringExtra("email")
        passwordPlaceholder = intent.getStringExtra("password")
        if(intent.getStringExtra("email") == "toBeRemoved" && intent.getStringExtra("password") == "toBeRemoved")
            removeData()
        loadData()
        if(savedEmail != null && savedPassword != null){
            supportActionBar!!.title = "My Books"
            login(savedEmail!!, savedPassword!!)
        } else{
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
            editEmail = binding.editEmail
            editPassword = binding.editPassword

            binding.loginBtn.setOnClickListener{
                val email = editEmail.text.toString()
                val password = editPassword.text.toString()

                login(email, password)
            }
            binding.signUpBtn.setOnClickListener {
                val intent = Intent(this, SignUp::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun loadData(){
        savedEmail = sharedpreferences.getString(emailKey, null)
        savedPassword = sharedpreferences.getString(passwordKey, null)
    }

    private fun saveData(email: String, password: String){
        var editor: SharedPreferences.Editor = sharedpreferences.edit()
        editor.putString(emailKey, email)
        editor.putString(passwordKey, password)
        editor.commit()
    }

    fun removeData(){
        var editor: SharedPreferences.Editor = sharedpreferences.edit()
        editor.remove(emailKey)
        editor.remove(passwordKey)
        editor.commit()
    }

    private fun login(email: String, password: String) {
        try{
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        saveData(email, password)
                        val intent = Intent(this@Login, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.w("AppErrorSignIn", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed",
                            Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: Exception){
            Toast.makeText(this, "An error occurred. Please try again", Toast.LENGTH_LONG)
                .show()
        }
    }
}