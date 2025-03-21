package com.example.myapplication

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class shopActivity : AppCompatActivity() {

    private lateinit var homeButton: ImageButton
    private lateinit var shopButton: ImageButton
    private lateinit var explainationButton: ImageButton
    private lateinit var gamesButton: ImageButton
    private lateinit var livechatButton: ImageButton
    private lateinit var cartIndicator: TextView
    private lateinit var cartIcon: ImageButton
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

    // User data
    private lateinit var currentUsername: String
    private lateinit var currentUserId: String
    private lateinit var accountButton: ImageButton
    private var cartItemCount = 0

    // Product adapter
    private lateinit var productAdapter: ProductAdapter
    private val productList = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.shop)

        // Initialize UI components
        initializeUIComponents()

        // Set up navigation listeners
        setupNavigationListeners()

        // Initialize Firebase components
        initializeFirebase()

        // Load user data
        loadUserData(currentUserId)

        // Setup RecyclerView with GridLayout
        setupRecyclerView()

        // Load products from Firestore
        loadProducts()

        // Load cart count for badge
        loadCartCount()
    }

    private fun initializeUIComponents() {
        shopButton = findViewById(R.id.shop)
        explainationButton = findViewById(R.id.shar7)
        gamesButton = findViewById(R.id.games)
        homeButton = findViewById(R.id.homebutton)
        livechatButton = findViewById(R.id.livechat)
        accountButton = findViewById(R.id.account)
        cartIcon = findViewById(R.id.cart_icon)
        cartIndicator = findViewById(R.id.cart_count)
        productsRecyclerView = findViewById(R.id.products_recycler_view)
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container)
    }

    private fun setupNavigationListeners() {
        shopButton.setOnClickListener {
            // Already in shop activity, do nothing or refresh
        }

        explainationButton.setOnClickListener {
            val intent = Intent(this, explainationActivity::class.java)
            startActivity(intent)
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        gamesButton.setOnClickListener {
            val intent = Intent(this, gamesActivity::class.java)
            startActivity(intent)
        }

        livechatButton.setOnClickListener {
            val intent = Intent(this, chatRoomActivity::class.java)
            startActivity(intent)
        }

//        cartIcon.setOnClickListener {
//            val intent = Intent(this, CartActivity::class.java)
//            startActivity(intent)
//        }
    }

    private fun initializeFirebase() {
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            currentUserId = user.uid
            val userRef = database.getReference("users").child(currentUserId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        currentUsername = snapshot.child("name").getValue(String::class.java) ?: "Anonymous"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@shopActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Handle not logged in case
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Use this to ensure layout manager is correctly applied
    private fun setupRecyclerView() {
        val layoutManager = GridLayoutManager(this, 2)
        productsRecyclerView.layoutManager = layoutManager
        productAdapter = ProductAdapter(
            productList,
            onProductClick = { product ->
                // Navigate to product details
                val intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onAddToCart = { product ->
                addToCart(product)
            },
            onFavoriteClick = { product, isFavorite ->
                toggleFavorite(product, isFavorite)
            }
        )
        productsRecyclerView.adapter = productAdapter
    }

    // In your shopActivity
    private fun loadProducts() {
        // Start shimmer effect
        shimmerFrameLayout.visibility = View.VISIBLE
        shimmerFrameLayout.startShimmer()
        productsRecyclerView.visibility = View.GONE

        firestore.collection("products")
            .get()
            .addOnSuccessListener { result ->
                // Clear the list before adding new items
                productList.clear()

                for (document in result) {
                    val product = document.toObject(Product::class.java).apply {
                        id = document.id
                    }
                    productList.add(product)
                }

                // Check user favorites
                checkUserFavorites()

                // DEBUG: Log product count
                Log.d("ShopActivity", "Loaded ${productList.size} products")

                // Stop shimmer effect and display products
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
                productsRecyclerView.visibility = View.VISIBLE

                // Notify adapter with the new data
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Stop shimmer effect and show error
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
                Log.e(ContentValues.TAG, "Error getting products", exception)
                Toast.makeText(this, "Error loading products: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserFavorites() {
        firestore.collection("users")
            .document(currentUserId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { favorites ->
                val favoriteIds = favorites.documents.map { it.id }

                // Update favorite status for products
                productList.forEach { product ->
                    product.isFavorite = favoriteIds.contains(product.id)
                }

                productAdapter.notifyDataSetChanged()
            }
    }

    private fun toggleFavorite(product: Product, isFavorite: Boolean) {
        val userFavoritesRef = firestore.collection("users")
            .document(currentUserId)
            .collection("favorites")

        if (isFavorite) {
            // Add to favorites
            userFavoritesRef.document(product.id)
                .set(mapOf("timestamp" to System.currentTimeMillis()))
                .addOnSuccessListener {
                    product.isFavorite = true
                    productAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT).show()
                    Log.e(ContentValues.TAG, "Error adding to favorites", e)
                }
        } else {
            // Remove from favorites
            userFavoritesRef.document(product.id)
                .delete()
                .addOnSuccessListener {
                    product.isFavorite = false
                    productAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show()
                    Log.e(ContentValues.TAG, "Error removing from favorites", e)
                }
        }
    }

    private fun addToCart(product: Product) {
        val userCartRef = firestore.collection("users")
            .document(currentUserId)
            .collection("cart")

        // Check if product is already in cart
        userCartRef.document(product.id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Product exists in cart, increment quantity
                    val currentQuantity = document.getLong("quantity") ?: 0
                    userCartRef.document(product.id)
                        .update("quantity", currentQuantity + 1)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
                            loadCartCount()
                        }
                } else {
                    // Product does not exist in cart, add with quantity 1
                    val cartItem = hashMapOf(
                        "productId" to product.id,
                        "quantity" to 1,
                        "price" to product.price,
                        "name" to product.name,
                        "imageUrl" to product.imageUrl,
                        "timestamp" to System.currentTimeMillis()
                    )

                    userCartRef.document(product.id)
                        .set(cartItem)
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

    private fun loadUserData(userId: String?) {
        userId?.let {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(it)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val pfpIndex = snapshot.child("pfp").getValue(Int::class.java) ?: 0
                        updateProfilePicture(pfpIndex)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(ContentValues.TAG, "Error loading user data", error.toException())
                }
            })
        }
    }

    private fun updateProfilePicture(pfpIndex: Int) {
        val pfps = arrayOf(
            R.drawable.pfp1,
            R.drawable.pfp2,
            R.drawable.pfp3,
            R.drawable.pfp4,
            R.drawable.pfp5,
            R.drawable.pfp6,
        )

        val index = (pfpIndex - 1).coerceIn(0, pfps.size - 1)
        accountButton.setImageResource(pfps[index])
    }

    override fun onResume() {
        super.onResume()
        // Refresh cart count when returning to this activity
        loadCartCount()
    }

    data class Product(
        var id: String = "",
        val name: String = "",
        val price: Double = 0.0,
        val description: String = "",
        val imageUrl: String = "",
        val stock: Int = 0,
        val rating: Float = 0.0f,
        var isFavorite: Boolean = false
    )
}