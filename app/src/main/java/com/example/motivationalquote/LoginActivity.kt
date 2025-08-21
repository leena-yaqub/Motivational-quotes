package com.example.motivationalquote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvGuestMode: TextView
    private lateinit var adView: AdView   // banner ad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // ✅ Initialize Mobile Ads SDK (safe to call multiple times)
        MobileAds.initialize(this) {}

        // ✅ Load banner ad
        adView = findViewById(R.id.bannerAd)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Check if user is already signed in and preferences allow skip
        if (auth.currentUser != null && Prefs.shouldSkipAuth(this)) {
            navigateToMain()
            return
        }

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvGuestMode = findViewById(R.id.tvGuestMode)
    }

    private fun setupClickListeners() {
        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                signInUser(email, password)
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvGuestMode.setOnClickListener {
            // Allow guest access without authentication
            Toast.makeText(this, "Continuing as guest", Toast.LENGTH_SHORT).show()
            navigateToMain()
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email"
            etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return false
        }

        return true
    }

    private fun signInUser(email: String, password: String) {
        btnSignIn.isEnabled = false
        btnSignIn.text = "Signing in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                btnSignIn.isEnabled = true
                btnSignIn.text = "SIGN IN"

                if (task.isSuccessful) {
                    Log.d("LoginActivity", "signInWithEmail:success")

                    // Mark user as logged in
                    Prefs.markLoggedIn(this)

                    val user = auth.currentUser
                    val userName = user?.email?.substringBefore("@") ?: "User"
                    Toast.makeText(this, "Welcome back, $userName!", Toast.LENGTH_SHORT).show()

                    navigateToMain()
                } else {
                    Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                    handleSignInError(task.exception)
                }
            }
    }

    private fun handleSignInError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> {
                when (exception.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email address"
                    "ERROR_USER_DISABLED" -> "This account has been disabled"
                    else -> "Account not found"
                }
            }
            is FirebaseAuthInvalidCredentialsException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Invalid email address format"
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                    else -> "Invalid credentials"
                }
            }
            else -> exception?.message ?: "Sign in failed. Please try again."
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()

        // Clear any sign-out flags when returning to login
        val prefs = Prefs.get(this)
        if (prefs.getBoolean("user_signed_out", false)) {
            prefs.edit().putBoolean("user_signed_out", false).apply()
        }
    }
}
