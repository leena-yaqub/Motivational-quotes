package com.example.motivationalquote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvFavoriteCount: TextView
    private lateinit var tvActiveDays: TextView
    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        setupBackButton()
        setupLogoutButton()
        initBannerAd()

        loadUserProfile()
        loadFavoriteCount()
        calculateActiveDays()
    }

    private fun initializeViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvFavoriteCount = findViewById(R.id.tvFavoriteCount)
        tvActiveDays = findViewById(R.id.tvActiveDays)
        adView = findViewById(R.id.bannerAd)
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }
    }

    private fun setupLogoutButton() {
        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes") { _, _ -> performLogout() }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun performLogout() {
        try {
            auth.signOut()
            Prefs.markSignedOut(this) // mark in prefs so app won't auto-login
        } catch (e: Exception) {
            Log.w("ProfileActivity", "Error signing out: ${e.message}", e)
        }

        Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun initBannerAd() {
        // Initialize Mobile Ads if not already initialized elsewhere
        MobileAds.initialize(this) {}
        adView?.loadAd(AdRequest.Builder().build())
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            tvUserEmail.text = user.email ?: "user@example.com"

            // Load name from Firestore
            firestore.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "User"
                        tvUserName.text = name
                    } else {
                        tvUserName.text = "User"
                    }
                }
                .addOnFailureListener {
                    tvUserName.text = "User"
                }
        } else {
            // No user — show defaults
            tvUserName.text = "Guest User"
            tvUserEmail.text = "guest@example.com"
            tvFavoriteCount.text = "0"
            tvActiveDays.text = "1"
        }
    }

    private fun loadFavoriteCount() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            tvFavoriteCount.text = "0"
            return
        }

        firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                tvFavoriteCount.text = documents.size().toString()
            }
            .addOnFailureListener {
                tvFavoriteCount.text = "0"
            }
    }

    private fun calculateActiveDays() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
                        val daysSinceCreation = ((System.currentTimeMillis() - createdAt) / (1000 * 60 * 60 * 24)).toInt()
                        tvActiveDays.text = maxOf(1, daysSinceCreation).toString()
                    } else {
                        tvActiveDays.text = "1"
                    }
                }
                .addOnFailureListener {
                    tvActiveDays.text = "1"
                }
        } else {
            tvActiveDays.text = "1"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up adview if needed
        adView?.destroy()
    }
}
