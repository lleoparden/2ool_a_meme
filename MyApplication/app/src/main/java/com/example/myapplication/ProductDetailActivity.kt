package com.example.myapplication

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var productImage: ImageView
    private lateinit var productName: TextView
    private lateinit var productPrice: TextView
    private lateinit var bottomPrice: TextView
    private lateinit var productDescription: TextView
    private lateinit var stockStatus: TextView
    private lateinit var productRating: RatingBar
    private lateinit var ratingValue: TextView
    private lateinit var favoriteButton: ImageButton
    private lateinit var addToCartButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var cartIcon: ImageButton
    private lateinit var cartIndicator: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserId: String
    private lateinit var productId: String
    private var cartItemCount = 0
    private var isFavorite = false
    private lateinit var currentProduct: shopActivity.Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Initialize UI components
        initializeUIComponents()

        // Initialize Firebase
        initializeFirebase()

        // Get product ID from intent
        productId = intent.getStringExtra("PRODUCT_ID") ?: ""
        if (productId.isEmpty()) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set up click listeners
        setupClickListeners()

        // Load product details
        loadProductDetails()

        // Load cart count
        loadCartCount()

        // Check if product is in favorites
        checkFavoriteStatus()
    }

    private fun initializeUIComponents() {
        productImage = findViewById(R.id.product_image)
        productName = findViewById(R.id.product_name)
        productPrice = findViewById(R.id.product_price)
        bottomPrice = findViewById(R.id.bottom_price)
        productDescription = findViewById(R.id.product_description)
        stockStatus = findViewById(R.id.stock_status)
        productRating = findViewById(R.id.product_rating)
        ratingValue = findViewById(R.id.rating_value)
        backButton = findViewById(R.id.back_button)
        favoriteButton = findViewById(R.id.favorite_button)
        addToCartButton = findViewById(R.id.add_to_cart_button)
        cartIcon = findViewById(R.id.cart_icon)
        cartIndicator = findViewById(R.id.cart_count)
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user != null) {
            currentUserId = user.uid
        } else {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            onBackPressed()
        }

        //TODO
//        cartIcon.setOnClickListener {
//            val intent = Intent(this, CartActivity::class.java)
//            startActivity(intent)
//        }

        addToCartButton.setOnClickListener {
            addToCart()
        }

        favoriteButton.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun loadProductDetails() {
        firestore.collection("products").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentProduct = document.toObject(shopActivity.Product::class.java)!!.apply {
                        id = document.id
                    }

                    // Update UI with product details
                    updateProductUI(currentProduct)
                } else {
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading product: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(ContentValues.TAG, "Error getting product", e)
                finish()
            }
    }

    private fun updateProductUI(product: shopActivity.Product) {
        productName.text = product.name
        productPrice.text = "${product.price}Egp"
        bottomPrice.text = "${product.price}Egp"
        productDescription.text = product.description

        // Set stock status
        if (product.stock > 0) {
            stockStatus.text = "In Stock (${product.stock} available)"
            stockStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            addToCartButton.isEnabled = true
        } else {
            stockStatus.text = "Out of Stock"
            stockStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            addToCartButton.isEnabled = false
        }

        // Set rating
        productRating.rating = product.rating
        ratingValue.text = product.rating.toString()

        // Load image with Glide
        Glide.with(this)
            .load(product.imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(productImage)
    }

    private fun checkFavoriteStatus() {
        firestore.collection("users")
            .document(currentUserId)
            .collection("favorites")
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                isFavorite = document.exists()
                updateFavoriteIcon()
            }
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_outline)
        }
    }

    private fun toggleFavorite() {
        val userFavoritesRef = firestore.collection("users")
            .document(currentUserId)
            .collection("favorites")
            .document(productId)

        if (isFavorite) {
            // Remove from favorites
            userFavoritesRef.delete()
                .addOnSuccessListener {
                    isFavorite = false
                    updateFavoriteIcon()
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show()
                    Log.e(ContentValues.TAG, "Error removing from favorites", e)
                }
        } else {
            // Add to favorites
            userFavoritesRef.set(mapOf(
                "timestamp" to System.currentTimeMillis(),
                "productId" to productId
            ))
                .addOnSuccessListener {
                    isFavorite = true
                    updateFavoriteIcon()
                    Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT).show()
                    Log.e(ContentValues.TAG, "Error adding to favorites", e)
                }
        }
    }

    private fun addToCart() {
        if (!::currentProduct.isInitialized) {
            Toast.makeText(this, "Product not loaded properly", Toast.LENGTH_SHORT).show()
            return
        }

        val userCartRef = firestore.collection("users")
            .document(currentUserId)
            .collection("cart")
            .document(productId)

        userCartRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Product already in cart, increment quantity
                    val currentQuantity = document.getLong("quantity") ?: 0

                    // Check if we have enough stock
                    if (currentQuantity + 1 > currentProduct.stock) {
                        Toast.makeText(this, "Not enough stock available", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    userCartRef.update("quantity", currentQuantity + 1)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
                            loadCartCount()
                        }
                } else {
                    // Product not in cart, add with quantity 1
                    val cartItem = hashMapOf(
                        "productId" to productId,
                        "quantity" to 1,
                        "price" to currentProduct.price,
                        "name" to currentProduct.name,
                        "imageUrl" to currentProduct.imageUrl,
                        "timestamp" to System.currentTimeMillis()
                    )

                    userCartRef.set(cartItem)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
                            loadCartCount()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                            Log.e(ContentValues.TAG, "Error adding to cart", e)
                        }
                }
            }
    }

    private fun loadCartCount() {
        firestore.collection("users")
            .document(currentUserId)
            .collection("cart")
            .get()
            .addOnSuccessListener { documents ->
                cartItemCount = documents.size()

                if (cartItemCount > 0) {
                    cartIndicator.visibility = View.VISIBLE
                    cartIndicator.text = cartItemCount.toString()
                } else {
                    cartIndicator.visibility = View.GONE
                }
            }
    }

    override fun onResume() {
        super.onResume()
        // Refresh cart count when returning to this activity
        loadCartCount()
    }
}