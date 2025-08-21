package com.example.motivationalquote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuotesAdapter(
    private val quotes: MutableList<Quote>,
    private val onFavoriteClick: (position: Int, quote: Quote) -> Unit
) : RecyclerView.Adapter<QuotesAdapter.QuoteViewHolder>() {

    inner class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuote: TextView = itemView.findViewById(R.id.tvQuote)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)

        fun bind(quote: Quote) {
            tvQuote.text = quote.text
            tvAuthor.text = "— ${quote.author}"

            val iconRes = if (quote.isFavorite) {
                R.drawable.ic_favorite_filled   // 🔴 red heart
            } else {
                R.drawable.ic_favorite_outline  // ⚪ outline
            }
            ivFavorite.setImageResource(iconRes)

            ivFavorite.setOnClickListener {
                val pos = adapterPosition  // FIXED: Changed from bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onFavoriteClick(pos, quotes[pos])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quote, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(quotes[position])
    }

    override fun getItemCount(): Int = quotes.size
}