package com.example.motivationalquote

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var rvQuotes: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var quotesAdapter: FavoritesAdapter  // CHANGED: Use FavoritesAdapter
    private val quotes = mutableListOf<Quote>()
    private lateinit var adView: AdView
    private val TAG = "FavoritesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        initializeComponents()
        setupToolbar()
        setupRecyclerView()

        // Initialize and load AdMob banner
        MobileAds.initialize(this) {}
        adView = findViewById(R.id.bannerAd)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        loadFavoriteQuotes()
    }

    private fun initializeComponents() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        rvQuotes = findViewById(R.id.rvQuotes)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
        findViewById<TextView>(R.id.titleText).text = "Favorites"
    }

    private fun setupRecyclerView() {
        quotesAdapter = FavoritesAdapter(quotes) { position, quote ->  // CHANGED: Use FavoritesAdapter
            removeFromFavorites(position, quote)
        }
        rvQuotes.apply {
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
            adapter = quotesAdapter
        }
    }

    private fun loadFavoriteQuotes() {
        val user = auth.currentUser
        if (user == null) {
            Log.d(TAG, "User is null, showing empty state")
            showEmptyState()
            Toast.makeText(this, "Please sign in to view favorites", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Loading favorites for user: ${user.uid}")

        // Show loading state
        tvEmpty.text = "Loading favorites..."
        tvEmpty.visibility = View.VISIBLE
        rvQuotes.visibility = View.GONE

        firestore.collection("users")
            .document(user.uid)
            .collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Successfully loaded ${documents.size()} favorites from Firebase")
                quotes.clear()

                for (document in documents) {
                    try {
                        val text = document.getString("text") ?: ""
                        val author = document.getString("author") ?: "Unknown"
                        val category = document.getString("category") ?: ""

                        val quote = Quote(
                            id = document.id,
                            text = text,
                            author = author,
                            category = category,
                            isFavorite = true  // IMPORTANT: Set as favorite since these are from favorites collection
                        )
                        quotes.add(quote)
                        Log.d(TAG, "Added favorite quote: $text by $author")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing document: ${e.message}")
                    }
                }

                if (quotes.isEmpty()) {
                    Log.d(TAG, "No favorites found, showing empty state")
                    showEmptyState()
                } else {
                    Log.d(TAG, "Showing ${quotes.size} favorites")
                    hideEmptyState()
                    quotesAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting favorites: ${exception.message}")
                tvEmpty.text = "Error loading favorites"
                showEmptyState()
                Toast.makeText(this, "Error loading favorites: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFromFavorites(position: Int, quote: Quote) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please sign in to manage favorites", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Removing quote from favorites: ${quote.text}")

        firestore.collection("users")
            .document(user.uid)
            .collection("favorites")
            .document(quote.id)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Successfully removed quote from Firebase")
                quotes.removeAt(position)
                quotesAdapter.notifyItemRemoved(position)
                quotesAdapter.notifyItemRangeChanged(position, quotes.size)
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()

                if (quotes.isEmpty()) {
                    showEmptyState()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error removing favorite: ${exception.message}")
                Toast.makeText(this, "Error removing favorite", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEmptyState() {
        tvEmpty.text = "No Favorites Yet\n\nTo add favorites, go to any category and tap the heart icon on quotes you like!"
        tvEmpty.visibility = View.VISIBLE
        rvQuotes.visibility = View.GONE
    }

    private fun hideEmptyState() {
        tvEmpty.visibility = View.GONE
        rvQuotes.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
        // Reload favorites when activity resumes
        Log.d(TAG, "Activity resumed, reloading favorites")
        loadFavoriteQuotes()
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}