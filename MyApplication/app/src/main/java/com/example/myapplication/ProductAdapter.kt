package com.example.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class ProductAdapter(
    private val products: List<shopActivity.Product>,
    private val onProductClick: (shopActivity.Product) -> Unit,
    private val onAddToCart: (shopActivity.Product) -> Unit,
    private val onFavoriteClick: (shopActivity.Product, Boolean) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productCard: CardView = itemView.findViewById(R.id.product_card)
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
        val addToCartButton: ImageButton = itemView.findViewById(R.id.add_to_cart_button)
        val favoriteButton: ImageButton = itemView.findViewById(R.id.favorite_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        // Set product name and price
        holder.productName.text = product.name ?: "Unnamed Product"
        holder.productPrice.text = if (product.price > 0) "${product.price}Egp" else "Price not available"

        // Safely load images
        try {
            Glide.with(holder.productImage.context)
                .load(product.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.productImage)
        } catch (e: Exception) {
            Log.e("ProductAdapter", "Error loading image for product ${product.id}", e)
            // Set a default image in case of error
            holder.productImage.setImageResource(R.drawable.placeholder_image)
        }

        // Update favorite button state
        if (product.isFavorite) {
            holder.favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            holder.favoriteButton.setImageResource(R.drawable.ic_favorite_outline)
        }

        // Set click listeners
        holder.productCard.setOnClickListener {
            onProductClick(product)
        }

        holder.productImage.setOnClickListener {
            onProductClick(product)
        }

        holder.productName.setOnClickListener {
            onProductClick(product)
        }

        holder.productPrice.setOnClickListener {
            onProductClick(product)
        }

        holder.addToCartButton.setOnClickListener {
            onAddToCart(product)
        }

        holder.favoriteButton.setOnClickListener {
            onFavoriteClick(product, !product.isFavorite)
        }
    }

    override fun getItemCount() = products.size
}