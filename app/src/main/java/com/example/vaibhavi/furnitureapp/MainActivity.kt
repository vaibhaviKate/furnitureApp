package com.example.vaibhavi.furnitureapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.vaibhavi.furnitureapp.R.id.username
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //firebase authentication to create user
        registerButton.setOnClickListener{

            val email = emailid.text.toString() //get entered text
            val password = password.text.toString()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Enter email/password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("MainActivity", "Email is $email")
            Log.d("MainActivity", "Password is $password")

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if(!it.isSuccessful) return@addOnCompleteListener

                        //else if successful
                        Log.d("MainActivity", "Successfully created user with uid : ${it.result!!.user.uid}")
                        Toast.makeText(this@MainActivity, "Registration successful.", Toast.LENGTH_SHORT).show()
                        saveUserToFirebaseDatabase()

                        val intent = Intent(this, HomePage::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    .addOnFailureListener{
                        Log.d("MainActivity", "Failed to create user ${it.message}") //failure message
                        Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                    }
        }

        alreadyRegistered.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }
    }

    private fun saveUserToFirebaseDatabase(){

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val username =  username.text.toString()

        val user = User(uid,username)

        Log.d("MainActivity", username)
        Log.d("MainActivity", ref.toString())

        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("MainActivity", "$username added to firebase db.")
                }

                .addOnFailureListener{
                    Log.d("MainActivity", "databse failed.")
                }
    }
}

class User(val uid: String, val username: String)


