package com.example.motivationalquote

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var tvGreeting: TextView

    // Interstitial
    private var interstitialAd: InterstitialAd? = null
    private val TAG = "MainActivity"

    // Banner - make it nullable to avoid lateinit issues
    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // If no user logged in → go to login
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // Initialize views
        tvGreeting = findViewById(R.id.tvGreeting)

        // Set dynamic greeting
        setGreetingMessage()

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}

        // Setup banner ad - safely initialize with null check
        adView = findViewById(R.id.bannerAd)
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)

        // Load an interstitial ad
        loadInterstitialAd()

        setupCategoryClickListeners()
        setupBottomNavigation()

        // Remember user is logged in
        sharedPrefs.edit().putBoolean("user_logged_in", true).apply()

        // Welcome message
        val userName = getUserName()
        Toast.makeText(this, "Welcome back, $userName!", Toast.LENGTH_SHORT).show()
    }

    private fun setGreetingMessage() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 5..11 -> "Good Morning."
            in 12..16 -> "Good Afternoon."
            in 17..20 -> "Good Evening."
            else -> "Good Night."
        }

        tvGreeting.text = greeting
    }

    private fun getUserName(): String {
        return auth.currentUser?.email?.substringBefore("@") ?: "User"
    }

    private fun setupCategoryClickListeners() {
        findViewById<CardView>(R.id.cardMotivate).setOnClickListener {
            navigateToQuotes("Motivate")
        }
        findViewById<CardView>(R.id.cardLove).setOnClickListener {
            navigateToQuotes("Love")
        }
        findViewById<CardView>(R.id.cardSuccess).setOnClickListener {
            navigateToQuotes("Success")
        }
        findViewById<CardView>(R.id.cardWisdom).setOnClickListener {
            navigateToQuotes("Wisdom")
        }
        findViewById<CardView>(R.id.cardLife).setOnClickListener {
            navigateToQuotes("Life")
        }
        findViewById<CardView>(R.id.cardHappy).setOnClickListener {
            navigateToQuotes("Happy")
        }
    }

    private fun setupBottomNavigation() {
        findViewById<android.widget.LinearLayout>(R.id.btnLike).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        // Show interstitial before Profile
        findViewById<android.widget.LinearLayout>(R.id.btnProfile).setOnClickListener {
            showInterstitialAdThenOpenProfile()
        }
    }

    private fun navigateToQuotes(category: String) {
        val intent = Intent(this, QuotesActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }

    // ---------------------------
    // Interstitial helpers
    // ---------------------------
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        val adUnit = "ca-app-pub-3940256099942544/1033173712" // Test ID

        InterstitialAd.load(this, adUnit, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d(TAG, "Interstitial loaded")
                interstitialAd = ad
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.w(TAG, "Interstitial failed to load: ${loadAdError.message}")
                interstitialAd = null
            }
        })
    }

    private fun showInterstitialAdThenOpenProfile() {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    openProfile()
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    openProfile()
                }
            }
            interstitialAd?.show(this)
        } else {
            openProfile()
            loadInterstitialAd()
        }
    }

    private fun openProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    override fun onStart() {
        super.onStart()
        val userSignedOut = sharedPrefs.getBoolean("user_signed_out", false)

        if (userSignedOut || auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        if (userSignedOut) {
            sharedPrefs.edit().putBoolean("user_signed_out", false).apply()
        }
    }

    override fun onResume() {
        super.onResume()
        adView?.resume()

        // Update greeting when returning to activity
        setGreetingMessage()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }
}