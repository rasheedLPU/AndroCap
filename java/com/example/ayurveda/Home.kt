package com.example.ayurveda

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Home : AppCompatActivity() {

    // Initialize Firestore
    val db = Firebase.firestore
    private val doctorsList = mutableListOf<Doctor>()
    private val adapter = DoctorAdapter(doctorsList)
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        drawerLayout  = findViewById(R.id.drawerlayout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val sessionManager = SessionManager(this)
        val userEmail = sessionManager.getUserEmail()

        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            it.isChecked = true
            when(it.itemId){
                R.id.nav_home -> Toast.makeText(applicationContext,"Clicked Home",Toast.LENGTH_SHORT).show()
                R.id.remedy -> Toast.makeText(applicationContext,"Clicked Remedy",Toast.LENGTH_SHORT).show()
                R.id.appoitment -> Toast.makeText(applicationContext,"Clicked Appointment",Toast.LENGTH_SHORT).show()
                R.id.nav_login -> Toast.makeText(applicationContext,"Clicked Login",Toast.LENGTH_SHORT).show()
                R.id.user -> Toast.makeText(applicationContext,"Clicked User Profile",Toast.LENGTH_SHORT).show()
            }
            true
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        //Navbar
        //Profile
        val userNavButton = findViewById<ImageButton>(R.id.userProfileNavbtn)
        userNavButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }
        //Appointments
        val userAppointmentButton = findViewById<ImageButton>(R.id.userAppointmentsbtn)
        userAppointmentButton.setOnClickListener {
            val intent = Intent(this, UserAppointments::class.java)
            startActivity(intent)
        }
        //Store
        val userStoreButton = findViewById<ImageButton>(R.id.storenavbtn)
        userStoreButton.setOnClickListener {
            val intent = Intent(this, StoreHome::class.java)
            startActivity(intent)
        }
        //Store
        val RemedyButton = findViewById<ImageButton>(R.id.remedyNavBtn)
        RemedyButton.setOnClickListener {
            val intent = Intent(this, DoctorRecommendation::class.java)
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


        val searchView = findViewById<SearchView>(R.id.searchbydoctor)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterDoctors(newText)
                return true
            }
        })

        // Set up RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recycleviewdoctors)
        recyclerView.adapter = adapter // Set the adapter for the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        val doctorsCollection = db.collection("doctors")
        doctorsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val docNameEn = document.getString("docNameEn")
                    val docNameSn = document.getString("docNameSn")
                    val avatarUrl = document.getString("avatarUrl")
                    val docQualification = document.getString("docQualification")
                    val docLicense = document.getString("docLicense")
                    val docPhoneNo = document.getString("docPhoneNo")
                    val address = document.getString("address")
                    val longitude = document.getDouble("longitude")
                    val latitude = document.getDouble("latitude")
                    val category = document.getString("category")

                    if (docNameEn != null && docNameSn != null && category != null && avatarUrl != null &&
                        docQualification != null && docLicense != null && docPhoneNo != null &&
                        address != null && longitude != null && latitude != null ) {
                        val doctor = Doctor(
                            docNameEn,
                            docNameSn,
                            category,
                            avatarUrl,
                            docQualification,
                            docLicense,
                            docPhoneNo,
                            address,
                            longitude,
                            latitude,

                        )
                        doctorsList.add(doctor)
                    }
                }
                adapter.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                // Handle the failure to retrieve data
                Log.e("Firestore", "Error getting documents: $exception")
            }


    }
    private fun filterDoctors(query: String?) {
        if (query.isNullOrBlank()) {
            // If the query is blank, show all doctors
            adapter.submitList(doctorsList)
        } else {
            // Filter doctors based on the query
            val filteredDoctors = doctorsList.filter { doctor ->
                doctor.docNameEn.contains(query, ignoreCase = true) ||
                        doctor.docNameSn.contains(query, ignoreCase = true)
            }
            adapter.submitList(filteredDoctors)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    // Override the onBackPressed() method to block the back button
//    override fun onBackPressed() {
//        // Leave this method empty to block the back button
//    }
}
