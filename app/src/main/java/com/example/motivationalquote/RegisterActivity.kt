package com.example.motivationalquote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSignUp: MaterialButton
    private lateinit var tvSignIn: android.widget.TextView
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvSignIn = findViewById(R.id.tvSignIn)

        btnSignUp.setOnClickListener {
            performRegister()
        }

        tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun performRegister() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }

        btnSignUp.isEnabled = false
        btnSignUp.text = "Creating Account..."

        Log.d(TAG, "Attempting to create account with email: $email")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Account created successfully")
                    val user = auth.currentUser

                    if (user != null) {
                        val userData = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "userId" to user.uid,
                            "createdAt" to System.currentTimeMillis()
                        )

                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error creating user document: ${e.message}")
                                Toast.makeText(this, "Account created but profile setup failed", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                    }
                } else {
                    Log.e(TAG, "Register failed: ${task.exception?.message}")
                    when (task.exception) {
                        is FirebaseAuthUserCollisionException -> {
                            etEmail.error = "This email is already registered."
                            etEmail.requestFocus()
                            Toast.makeText(this, "Account already exists. Please sign in.", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            val errorMessage = task.exception?.message ?: "Registration failed"
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                    btnSignUp.isEnabled = true
                    btnSignUp.text = "SIGN UP"
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Register exception: ${exception.message}")
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                btnSignUp.isEnabled = true
                btnSignUp.text = "SIGN UP"
            }
    }
}
