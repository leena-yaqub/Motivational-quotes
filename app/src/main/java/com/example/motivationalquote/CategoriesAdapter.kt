package com.example.motivationalquote

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CategoriesAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryCard: CardView = itemView.findViewById(R.id.categoryCard)
        val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.categoryName.text = category.name
        holder.categoryIcon.setImageResource(category.iconRes)

        // Set card background color
        try {
            holder.categoryCard.setCardBackgroundColor(Color.parseColor(category.color))
        } catch (e: Exception) {
            holder.categoryCard.setCardBackgroundColor(Color.parseColor("#FF6B35"))
        }

        holder.categoryCard.setOnClickListener {
            onCategoryClick(category)
        }
    }

    override fun getItemCount(): Int = categories.size
}