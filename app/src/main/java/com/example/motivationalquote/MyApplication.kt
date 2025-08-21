package com.example.motivationalquote

import android.app.Application
import androidx.multidex.MultiDex
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Enable multidex
        MultiDex.install(this)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Configure Firestore settings
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        // Initialize Mobile Ads
        MobileAds.initialize(this) {}
    }
}