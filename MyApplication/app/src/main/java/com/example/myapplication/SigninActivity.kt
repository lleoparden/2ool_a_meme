package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SigninActivity : AppCompatActivity() {

    private lateinit var googleSignInButton: ImageButton
    private lateinit var createAccountButton: ImageButton
    private lateinit var signInButton: ImageButton
    private lateinit var goSignInButton: ImageButton
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var backButton: ImageButton

    // Request code for Google Sign-In
    private val RC_SIGN_IN = 9001

    // Flag to track which layout is currently displayed
    private var isSignUpLayout = true

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Web Client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Start with sign-up layout
        setupSignUpLayout()
    }

    private fun setupSignUpLayout() {
        setContentView(R.layout.sign_up)
        isSignUpLayout = true

        // Initialize views
        googleSignInButton = findViewById(R.id.google_sign_up)
        createAccountButton = findViewById(R.id.create_account)
        emailEditText = findViewById(R.id.EmailAddress)
        passwordEditText = findViewById(R.id.password)
        nameEditText = findViewById(R.id.name)
        phoneEditText = findViewById(R.id.phone_number)
        goSignInButton = findViewById(R.id.go_sign_in)

        // Set up click listeners
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        createAccountButton.setOnClickListener {
            createUserWithEmailPassword()
        }

        goSignInButton.setOnClickListener {
            setupSignInLayout()
        }
    }

    private fun setupSignInLayout() {
        setContentView(R.layout.sign_in)
        isSignUpLayout = false

        // Initialize sign-in views
        signInButton = findViewById(R.id.sign_in)
        backButton = findViewById(R.id.go_back)
        emailEditText = findViewById(R.id.EmailAddress) // Make sure these IDs exist in sign_in layout
        passwordEditText = findViewById(R.id.password)
        googleSignInButton = findViewById(R.id.google_sign_in)

        backButton.setOnClickListener {
            setupSignUpLayout()
        }

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        signInButton.setOnClickListener {
            signInWithEmailPassword()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun createUserWithEmailPassword() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val name = nameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        // Validate input fields
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser

                    // Update user profile with name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                Log.d(TAG, "User profile updated.")
                            }
                        }

                    // Store additional user data in Firebase Database
                    storeUserData(user?.uid, name, email, phone,5)

                    updateUI(user)
                } else {
                    // If sign up fails, display a message to the user
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun signInWithEmailPassword() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        // Sign in with email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun storeUserData(userId: String?, name: String, email: String, phone: String,pfp:Int) {
        userId?.let {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(it)

            val userData = HashMap<String, Any>()
            userData["name"] = name
            userData["email"] = email
            userData["phone"] = phone
            userData["pfp"]=pfp

            userRef.setValue(userData)
                .addOnSuccessListener {
                    Log.d(TAG, "User data stored successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error storing user data", e)
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.idToken?.let { firebaseAuthWithGoogle(it) }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-in failed: ${e.message}", e)
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(this, "Sign-in successful", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser

                    // Check if this is a new user and store additional info if needed
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    if (isNewUser) {
                        // For Google sign-in, we might not have phone, so just store what we have
                        user?.let {
                            storeUserData(
                                it.uid,
                                it.displayName ?: "",
                                it.email ?: "",
                                "123-456-7890", // No phone number from Google Auth
                                5
                            )
                        }
                    }

                    updateUI(user)
                } else {
                    // Sign-in failed
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Sign-in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        // Update UI based on user state
        if (user != null) {
            // User is signed in, navigate to the next screen
            Toast.makeText(this, "Welcome, ${user.displayName}", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // User is signed out
            Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (!isSignUpLayout) {
            // If currently on sign-in layout, switch to sign-up
            setupSignUpLayout()
        } else {
            // Otherwise, perform normal back button behavior
            super.onBackPressed()
        }
    }

    companion object {
        private const val TAG = "SignInActivity"
    }
}