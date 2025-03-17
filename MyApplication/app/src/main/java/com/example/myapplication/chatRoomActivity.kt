package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class chatRoomActivity : AppCompatActivity() {

    private lateinit var homeButton: ImageButton
    private lateinit var shopButton: ImageButton
    private lateinit var explainationButton: ImageButton
    private lateinit var gamesButton: ImageButton
    private lateinit var liveChatButton: ImageButton
    private lateinit var accountButton: ImageButton

    // Chat components
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messagesList: ArrayList<Message>
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var messagesRef: DatabaseReference

    // User data
    private lateinit var currentUsername: String
    private lateinit var currentUserId: String
    private var lastSeenTimestamp: Long = 0

    // Swear words list (can be expanded)
    private val swearWords = listOf("fuck", "shit", "ass", "bitch", "hell","FUCK","cunt")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.chatroom)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        messagesRef = database.getReference("messages")

        // Check if user is authenticated
        if (auth.currentUser == null) {
            // Redirect to login
            startActivity(Intent(this, SigninActivity::class.java))
            finish()
            return
        }

        // Get current user info
        currentUserId = auth.currentUser!!.uid
        val userRef = database.getReference("users").child(currentUserId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentUsername = snapshot.child("name").getValue(String::class.java) ?: "Anonymous"
                    setupLastSeen()
                    loadMessages()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@chatRoomActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })

        // Initialize UI components
        shopButton = findViewById(R.id.shop)
        explainationButton = findViewById(R.id.shar7)
        gamesButton = findViewById(R.id.games)
        homeButton = findViewById(R.id.homebutton)
        liveChatButton = findViewById(R.id.livechat)
        accountButton = findViewById(R.id.account)

        recyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        // Setup RecyclerView
        messagesList = ArrayList()
        messageAdapter = MessageAdapter(this, messagesList, currentUserId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        // Set up the navigation buttons
        setupNavigation()

        // Set up chat functionality
        setupChatFunctionality()
    }

    private fun setupNavigation() {
        shopButton.setOnClickListener {
            val intent = Intent(this, shopActivity::class.java)
            startActivity(intent)
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

        // The livechat button would just refresh/reload this activity
        liveChatButton.setOnClickListener {
            val intent = Intent(this, chatRoomActivity::class.java)
            startActivity(intent)
            finish()
        }

//        accountButton.setOnClickListener {
//            val intent = Intent(this, ProfileActivity::class.java)
//            startActivity(intent)
//        }
    }

    private fun setupChatFunctionality() {
        // Send message button action
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text.clear()
            }
        }
    }

    private fun setupLastSeen() {
        // Update last seen on app open
        lastSeenTimestamp = System.currentTimeMillis()

        // Store last seen timestamp in Firebase
        val userPresenceRef = database.getReference("user_presence").child(currentUserId)
        userPresenceRef.child("last_seen").setValue(lastSeenTimestamp)

        // Set up online status
        userPresenceRef.child("online").setValue(true)

        // Set up disconnect handler
        userPresenceRef.child("online").onDisconnect().setValue(false)
        userPresenceRef.child("last_seen").onDisconnect().setValue(ServerValue.TIMESTAMP)
    }

    private fun loadMessages() {
        // Query messages after the user's last seen timestamp
        messagesRef.orderByChild("timestamp").startAt(lastSeenTimestamp.toDouble())
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message::class.java)
                    if (message != null) {
                        // Process message visibility
                        if (shouldShowMessage(message)) {
                            messagesList.add(message)
                            messageAdapter.notifyItemInserted(messagesList.size - 1)
                            recyclerView.scrollToPosition(messagesList.size - 1)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle edited messages if needed
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Handle deleted messages if needed
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Not needed for this implementation
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@chatRoomActivity, "Failed to load messages", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun shouldShowMessage(message: Message): Boolean {
        // Show public messages
        if (!message.isPrivate) {
            return true
        }

        // Show private messages where user is sender or recipient
        return message.senderId == currentUserId || message.mentionedUsers.contains(currentUsername)
    }

    private fun sendMessage(text: String) {
        // Process text for mentions
        val mentionedUsers = ArrayList<String>()
        var isPrivate = false
        var processedText = text

        // Check for @mentions
        if (text.contains("@")) {
            val words = text.split(" ")
            for (word in words) {
                if (word.startsWith("@") && word.length > 1) {
                    val username = word.substring(1)
                    mentionedUsers.add(username)
                    isPrivate = true
                }
            }
        }

        // Filter swear words
        processedText = filterSwearWords(processedText)

        // Create message object
        val message = Message(
            id = UUID.randomUUID().toString(),
            text = processedText,
            senderId = currentUserId,
            senderName = currentUsername,
            timestamp = System.currentTimeMillis(),
            imageUrl = null,
            isPrivate = isPrivate,
            mentionedUsers = mentionedUsers
        )

        // Save message to Firebase
        messagesRef.child(message.id).setValue(message)
            .addOnSuccessListener {
                // Message sent successfully
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterSwearWords(text: String): String {
        var filteredText = text
        for (word in swearWords) {
            val regex = "\\b$word\\b".toRegex(RegexOption.IGNORE_CASE)
            val replacement = "*".repeat(word.length)
            filteredText = filteredText.replace(regex, replacement)
        }
        return filteredText
    }

    override fun onPause() {
        super.onPause()
        // Update last seen when user leaves the app
        val userPresenceRef = database.getReference("user_presence").child(currentUserId)
        userPresenceRef.child("online").setValue(false)
        userPresenceRef.child("last_seen").setValue(ServerValue.TIMESTAMP)
    }

    override fun onResume() {
        super.onResume()
        // Update online status when user returns
        val userPresenceRef = database.getReference("user_presence").child(currentUserId)
        userPresenceRef.child("online").setValue(true)
    }
}

// Message data class
data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: Long = 0,
    val imageUrl: String? = null,
    val isPrivate: Boolean = false,
    val mentionedUsers: List<String> = listOf()
)

// MessageAdapter for RecyclerView
class MessageAdapter(
    private val context: chatRoomActivity,
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val senderName: TextView = view.findViewById(R.id.messageSender)
        val messageText: TextView = view.findViewById(R.id.messageText)
        val messageTime: TextView = view.findViewById(R.id.messageTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = if (viewType == VIEW_TYPE_SENT) {
            layoutInflater.inflate(R.layout.message_sent, parent, false)
        } else {
            layoutInflater.inflate(R.layout.message_received, parent, false)
        }
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        // Set message text and sender
        holder.senderName.text = message.senderName
        holder.messageText.text = message.text

        // Format and set timestamp
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeString = sdf.format(Date(message.timestamp))
        holder.messageTime.text = timeString
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }
}