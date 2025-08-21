package com.example.motivationalquote

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private var appOpenAd: AppOpenAd? = null
    private var isAdShowing = false
    private var isSplashFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {
            // Ads SDK initialized, now load the app open ad
            Handler(Looper.getMainLooper()).postDelayed({
                isSplashFinished = true
                loadAppOpenAd()
            }, 200)
        }
    }

    private fun loadAppOpenAd() {
        try {
            val adRequest = AdRequest.Builder().build()

            AppOpenAd.load(
                this,
                "ca-app-pub-3940256099942544/9257395921", // Test AppOpenAd ID
                adRequest,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        showAppOpenAd()
                    }

                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                        // If ad fails, go directly to next screen
                        goNext()
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            goNext()
        }
    }

    private fun showAppOpenAd() {
        try {
            if (appOpenAd != null && !isAdShowing && !isFinishing) {
                isAdShowing = true
                appOpenAd?.show(this)

                appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        isAdShowing = false
                        goNext()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        isAdShowing = false
                        goNext()
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Ad showed successfully
                    }
                }
            } else {
                goNext()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            goNext()
        }
    }

    private fun goNext() {
        // Make sure splash screen has been shown for minimum time
        if (!isSplashFinished) {
            Handler(Looper.getMainLooper()).postDelayed({
                navigateToNextScreen()
            }, 200)
        } else {
            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        try {
            // Check if user logged in or should skip auth
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val goHome = (firebaseUser != null) || Prefs.shouldSkipAuth(this)

            val next = if (goHome) MainActivity::class.java else LoginActivity::class.java

            startActivity(Intent(this, next).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to login if anything goes wrong
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        // Clean up ad reference to prevent memory leaks
        appOpenAd = null
        super.onDestroy()
    }
}