package com.example.motivationalquote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private val quotes: MutableList<Quote>,
    private val onFavoriteClick: (position: Int, quote: Quote) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuote: TextView = itemView.findViewById(R.id.tvQuote)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)

        fun bind(quote: Quote) {
            tvQuote.text = quote.text
            tvAuthor.text = "— ${quote.author}"

            // Always show as filled heart since these are favorites
            ivFavorite.setImageResource(R.drawable.ic_favorite_filled)

            ivFavorite.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onFavoriteClick(pos, quotes[pos])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quote, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(quotes[position])
    }

    override fun getItemCount(): Int = quotes.size
}