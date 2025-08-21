package com.example.motivationalquote

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motivationalquote.databinding.ActivityQuotesBinding
import com.google.firebase.auth.FirebaseAuth  // ADDED: Import FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class QuotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuotesBinding
    private lateinit var quotesAdapter: QuotesAdapter
    private val quotesList = mutableListOf<Quote>()
    private val allQuotesList = mutableListOf<Quote>()
    private var selectedCategory: String = "All"

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()  // ADDED: FirebaseAuth for user management

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get category from intent - FIXED to handle both "category" and "CATEGORY"
        selectedCategory = intent.getStringExtra("CATEGORY") ?: intent.getStringExtra("category") ?: "All"

        // Set up toolbar with category name and back button
        setupToolbar()

        // Setup RecyclerView with adapter
        quotesAdapter = QuotesAdapter(quotesList) { position, quote ->
            toggleFavorite(position, quote)
        }

        binding.rvQuotes.apply {
            layoutManager = LinearLayoutManager(this@QuotesActivity)
            adapter = quotesAdapter
        }

        // Load quotes - try Firebase first, then fallback to sample data
        loadQuotes()
    }

    private fun setupToolbar() {
        // Set category name in toolbar - FIXED to match MainActivity categories
        binding.tvCategory.text = when(selectedCategory) {
            "Success" -> "Success Quotes"
            "Motivate" -> "Motivational Quotes"  // FIXED: Changed from "Motivation" to "Motivate"
            "Happy" -> "Happiness Quotes"        // FIXED: Changed from "Happiness" to "Happy"
            "Wisdom" -> "Wisdom Quotes"
            "Love" -> "Love Quotes"
            "Life" -> "Life Quotes"
            else -> "All Quotes"
        }

        // Set up back button click
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun loadQuotes() {
        // Load sample quotes directly
        loadSampleQuotes()

        // Optional: Try to load from Firebase too
        db.collection("quotes")
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    allQuotesList.clear()
                    for (doc in result) {
                        val quote = Quote(
                            id = doc.id,
                            text = doc.getString("text") ?: "",
                            author = doc.getString("author") ?: "",
                            category = doc.getString("category") ?: "",
                            isFavorite = false
                        )
                        allQuotesList.add(quote)
                    }
                    filterQuotesByCategory()
                }
            }
            .addOnFailureListener { e ->
                Log.e("QuotesActivity", "Error fetching quotes", e)
                // Sample data is already loaded, just show it
            }
    }

    private fun filterQuotesByCategory() {
        quotesList.clear()

        if (selectedCategory == "All") {
            quotesList.addAll(allQuotesList)
        } else {
            quotesList.addAll(allQuotesList.filter { it.category == selectedCategory })
        }

        quotesAdapter.notifyDataSetChanged()

        if (quotesList.isEmpty()) {
            Toast.makeText(this, "No quotes found for $selectedCategory", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Loaded ${quotesList.size} $selectedCategory quotes", Toast.LENGTH_SHORT).show()
        }
    }

    // Sample data with 6 categories × 5 quotes each = 30 quotes total
    private fun loadSampleQuotes() {
        quotesList.clear()

        // SUCCESS QUOTES
        quotesList.addAll(listOf(
            Quote("1", "Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill", "Success", false),
            Quote("2", "The way to get started is to quit talking and begin doing.", "Walt Disney", "Success", false),
            Quote("3", "Don't be afraid to give up the good to go for the great.", "John D. Rockefeller", "Success", false),
            Quote("4", "Innovation distinguishes between a leader and a follower.", "Steve Jobs", "Success", false),
            Quote("5", "The only impossible journey is the one you never begin.", "Tony Robbins", "Success", false)
        ))

        // MOTIVATION QUOTES
        quotesList.addAll(listOf(
            Quote("6", "Believe you can and you're halfway there.", "Theodore Roosevelt", "Motivation", false),
            Quote("7", "The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt", "Motivation", false),
            Quote("8", "It is during our darkest moments that we must focus to see the light.", "Aristotle", "Motivation", false),
            Quote("9", "You are never too old to set another goal or to dream a new dream.", "C.S. Lewis", "Motivation", false),
            Quote("10", "Life is what happens to you while you're busy making other plans.", "John Lennon", "Motivation", false)
        ))

        // HAPPINESS QUOTES
        quotesList.addAll(listOf(
            Quote("11", "The purpose of our lives is to be happy.", "Dalai Lama", "Happiness", false),
            Quote("12", "Happiness is not something ready made. It comes from your own actions.", "Dalai Lama", "Happiness", false),
            Quote("13", "Very little is needed to make a happy life; it is all within yourself, in your way of thinking.", "Marcus Aurelius", "Happiness", false),
            Quote("14", "The best way to cheer yourself up is to try to cheer somebody else up.", "Mark Twain", "Happiness", false),
            Quote("15", "Happiness is when what you think, what you say, and what you do are in harmony.", "Mahatma Gandhi", "Happiness", false)
        ))

        // WISDOM QUOTES
        quotesList.addAll(listOf(
            Quote("16", "The only way to do great work is to love what you do.", "Steve Jobs", "Wisdom", false),
            Quote("17", "In the middle of difficulty lies opportunity.", "Albert Einstein", "Wisdom", false),
            Quote("18", "Yesterday is history, tomorrow is a mystery, today is a gift of God, which is why we call it the present.", "Bill Keane", "Wisdom", false),
            Quote("19", "Be yourself; everyone else is already taken.", "Oscar Wilde", "Wisdom", false),
            Quote("20", "Two things are infinite: the universe and human stupidity; and I'm not sure about the universe.", "Albert Einstein", "Wisdom", false)
        ))

        // LOVE QUOTES
        quotesList.addAll(listOf(
            Quote("21", "Being deeply loved by someone gives you strength, while loving someone deeply gives you courage.", "Lao Tzu", "Love", false),
            Quote("22", "The best thing to hold onto in life is each other.", "Audrey Hepburn", "Love", false),
            Quote("23", "Love is composed of a single soul inhabiting two bodies.", "Aristotle", "Love", false),
            Quote("24", "Where there is love there is life.", "Mahatma Gandhi", "Love", false),
            Quote("25", "You know you're in love when you can't fall asleep because reality is finally better than your dreams.", "Dr. Seuss", "Love", false)
        ))

        // LIFE QUOTES
        quotesList.addAll(listOf(
            Quote("26", "Life is really simple, but we insist on making it complicated.", "Confucius", "Life", false),
            Quote("27", "The purpose of life is not to be happy. It is to be useful, to be honorable, to be compassionate.", "Ralph Waldo Emerson", "Life", false),
            Quote("28", "In the end, we will remember not the words of our enemies, but the silence of our friends.", "Martin Luther King Jr.", "Life", false),
            Quote("29", "Life is 10% what happens to you and 90% how you react to it.", "Charles R. Swindoll", "Life", false),
            Quote("30", "The only way to make sense out of change is to plunge into it, move with it, and join the dance.", "Alan Watts", "Life", false)
        ))

        quotesAdapter.notifyDataSetChanged()
        Toast.makeText(this, "Loaded ${quotesList.size} sample quotes", Toast.LENGTH_SHORT).show()
    }

    private fun toggleFavorite(position: Int, quote: Quote) {
        quote.isFavorite = !quote.isFavorite
        quotesAdapter.notifyItemChanged(position)

        if (quote.isFavorite) {
            saveToFavorites(quote)
        } else {
            removeFromFavorites(quote.id)
        }
    }

    private fun saveToFavorites(quote: Quote) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please sign in to save favorites", Toast.LENGTH_SHORT).show()
            return
        }

        // Save to user-specific favorites collection
        db.collection("users")
            .document(user.uid)
            .collection("favorites")
            .document(quote.id)
            .set(quote)
            .addOnSuccessListener {
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
                Log.d("QuotesActivity", "Quote saved to favorites: ${quote.text}")
            }
            .addOnFailureListener { e ->
                Log.e("QuotesActivity", "Failed to add favorite", e)
                Toast.makeText(this, "Failed to save favorite", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFromFavorites(quoteId: String) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please sign in to manage favorites", Toast.LENGTH_SHORT).show()
            return
        }

        // Remove from user-specific favorites collection
        db.collection("users")
            .document(user.uid)
            .collection("favorites")
            .document(quoteId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                Log.d("QuotesActivity", "Quote removed from favorites")
            }
            .addOnFailureListener { e ->
                Log.e("QuotesActivity", "Failed to remove favorite", e)
                Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
            }
    }
}