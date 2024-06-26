package com.example.ayurveda

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PaymentSuccess : AppCompatActivity() {


    // Initialize Firestore
    val db = Firebase.firestore
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)
        var alarmManager : AlarmManager
        val intent = Intent(this,AlarmManagerBroadcast::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 234, intent,
            PendingIntent.FLAG_MUTABLE)
        var alrarmbtn = findViewById<Button>(R.id.btnalarm)


        var Etext = findViewById<EditText>(R.id.edttxt)
        val refno = intent.getStringExtra("refno")
        val bookingRefNoTextView = findViewById<TextView>(R.id.bookingrefno)
        val subbookingRefNoTextView = findViewById<TextView>(R.id.subbookingrefno)
        val last4Characters = refno?.takeLast(4)
        bookingRefNoTextView.text = "#REF$last4Characters"
        subbookingRefNoTextView.text = "#REF$last4Characters"


        alrarmbtn.setOnClickListener{
            var i = Etext.text.toString().toInt()
            alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +
                        (i * 1000), pendingIntent
            )
            Toast.makeText(this, "Alarm set in $i seconds", Toast.LENGTH_LONG).show()
            val intent = Intent(this,Home::class.java)
            startActivity(intent)
        }

        // Navbar
        val sessionManager = SessionManager(this)
        val userEmail = sessionManager.getUserEmail()



        val userNavButton = findViewById<ImageButton>(R.id.userProfileNavbtn)
        userNavButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }
        // Retrieve the username from the "users" collection
        val usersCollection = db.collection("users")
        usersCollection.whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { userQuerySnapshot ->
                if (!userQuerySnapshot.isEmpty) {
                    // There may be multiple documents matching the email; loop through them if needed
                    for (userDocument in userQuerySnapshot.documents) {
                        val username = userDocument.getString("username")

                        // Now you have the username, and you can use it as needed
                        // For example, you can set it in a TextView
                        val usernameTextView = findViewById<TextView>(R.id.unamenavbar)
                        usernameTextView.text = username?.split(" ")?.get(0) ?: "User"

                        // If you found the username you were looking for, you can break out of the loop
                        break
                    }
                } else {
                    // Handle the case when no document with the matching email is found
                    Log.d("Firestore", "No user document found for email: $userEmail")
                }
            }
            .addOnFailureListener { exception ->
                // Handle the failure to retrieve user data
                Log.e("Firestore", "Error getting user document: $exception")
            }




    }


}