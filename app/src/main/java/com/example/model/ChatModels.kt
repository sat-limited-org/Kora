package com.example.model

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class ThemeMode(val title: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val avatarColorHex: Long = 0xFF0F6E48,
    val statusMessage: String = "Available",
    val isOnline: Boolean = false,
    val lastSeen: String = "recently"
) {
    val initials: String
        get() {
            val parts = name.trim().split(" ")
            return if (parts.size >= 2) {
                "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            } else {
                name.take(2).uppercase()
            }
        }
}

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String, // "me" for user, or contact.id
    val text: String,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.READ
) {
    val isFromMe: Boolean
        get() = senderId == "me"
}

data class Chat(
    val id: String,
    val contact: Contact,
    val lastMessage: Message,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false
)

data class UserProfile(
    val name: String = "Alex Rivera",
    val username: String = "@alex_rivera",
    val email: String = "user@satlimited.com",
    val organization: String = "SAT Limited",
    val phone: String = "+1 (555) 234-8976",
    val status: String = "Living in the moment 🌿 | Chatting on Kora",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val readReceiptsEnabled: Boolean = true,
    val enterIsSend: Boolean = false
)
