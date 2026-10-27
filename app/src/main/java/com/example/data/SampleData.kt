package com.example.data

import com.example.model.Chat
import com.example.model.Contact
import com.example.model.Message
import com.example.model.MessageStatus

object SampleData {

    val contactAmara = Contact(
        id = "c_amara",
        name = "Amara Chen",
        phone = "+1 (555) 349-8812",
        avatarColorHex = 0xFF0D9488, // Teal
        statusMessage = "Designing the new experience 🎨",
        isOnline = true,
        lastSeen = "online"
    )

    val contactTariro = Contact(
        id = "c_tariro",
        name = "Tariro Moyo",
        phone = "+263 77 412 9081",
        avatarColorHex = 0xFF059669, // Emerald
        statusMessage = "Building great things step by step 🚀",
        isOnline = true,
        lastSeen = "online"
    )

    val contactMarcus = Contact(
        id = "c_marcus",
        name = "Marcus Vance",
        phone = "+1 (555) 782-1144",
        avatarColorHex = 0xFF2563EB, // Blue
        statusMessage = "In a meeting • Last seen 15m ago",
        isOnline = false,
        lastSeen = "today at 15:15"
    )

    val contactElena = Contact(
        id = "c_elena",
        name = "Elena Rostova",
        phone = "+44 7911 123456",
        avatarColorHex = 0xFF9333EA, // Purple
        statusMessage = "Exploring soundscapes & acoustic tunes 🎵",
        isOnline = false,
        lastSeen = "today at 14:02"
    )

    val contactZack = Contact(
        id = "c_zack",
        name = "Zack Miller",
        phone = "+1 (555) 601-9233",
        avatarColorHex = 0xFFEA580C, // Orange
        statusMessage = "Coffee first, code later ☕",
        isOnline = true,
        lastSeen = "online"
    )

    val contactFarai = Contact(
        id = "c_farai",
        name = "Farai Ndlovu",
        phone = "+27 82 555 0192",
        avatarColorHex = 0xFF16A34A, // Green
        statusMessage = "Travelling for the summit ✈️",
        isOnline = false,
        lastSeen = "yesterday at 21:40"
    )

    val contactSophia = Contact(
        id = "c_sophia",
        name = "Sophia Patel",
        phone = "+1 (555) 882-7711",
        avatarColorHex = 0xFFDB2777, // Pink
        statusMessage = "Reading a good sci-fi book 📚",
        isOnline = false,
        lastSeen = "yesterday at 18:22"
    )

    val contactDevTeam = Contact(
        id = "c_devteam",
        name = "Kora Mobile Team",
        phone = "+1 (555) 000-0100",
        avatarColorHex = 0xFF0F766E, // Dark teal
        statusMessage = "Sprint demo & release review 📱",
        isOnline = true,
        lastSeen = "online"
    )

    val initialContacts = listOf(
        contactAmara,
        contactTariro,
        contactMarcus,
        contactElena,
        contactZack,
        contactFarai,
        contactSophia,
        contactDevTeam
    )

    private val now = System.currentTimeMillis()
    private val minute = 60 * 1000L
    private val hour = 60 * minute

    val initialMessages: Map<String, List<Message>> = mapOf(
        contactAmara.id to listOf(
            Message("m_a1", contactAmara.id, contactAmara.id, "Hey Alex! Have you reviewed the new Kora design layouts?", now - 40 * minute),
            Message("m_a2", contactAmara.id, "me", "Yes! The green and dark theme palette looks so clean and refreshing.", now - 35 * minute, MessageStatus.READ),
            Message("m_a3", contactAmara.id, contactAmara.id, "Awesome! I really like how snappy the chat bubbles feel.", now - 20 * minute),
            Message("m_a4", contactAmara.id, "me", "Agreed, the typography and spacing are spot on.", now - 15 * minute, MessageStatus.READ),
            Message("m_a5", contactAmara.id, contactAmara.id, "Can you check the bottom navigation bar when you get a chance? ✨", now - 3 * minute)
        ),
        contactTariro.id to listOf(
            Message("m_t1", contactTariro.id, contactTariro.id, "Hey! Are you still up for coffee and chatting about the Android project?", now - 3 * hour),
            Message("m_t2", contactTariro.id, "me", "Definitely! Let's do 3:30 at the corner cafe.", now - 2 * hour, MessageStatus.READ),
            Message("m_t3", contactTariro.id, contactTariro.id, "Perfect! See you there ☕", now - 25 * minute)
        ),
        contactMarcus.id to listOf(
            Message("m_m1", contactMarcus.id, "me", "Marcus, did the build pass the unit test suite?", now - 5 * hour, MessageStatus.READ),
            Message("m_m2", contactMarcus.id, contactMarcus.id, "All green! Zero lint issues found.", now - 4 * hour)
        ),
        contactDevTeam.id to listOf(
            Message("m_d1", contactDevTeam.id, contactDevTeam.id, "Sprint demo reminder: 10:00 AM UTC tomorrow.", now - 10 * hour),
            Message("m_d2", contactDevTeam.id, "me", "Got it, I'll showcase the local prototype on Compose.", now - 8 * hour, MessageStatus.READ),
            Message("m_d3", contactDevTeam.id, contactDevTeam.id, "Looking forward to it! The UI screenshots look stellar 🚀", now - 1 * hour)
        ),
        contactElena.id to listOf(
            Message("m_e1", contactElena.id, contactElena.id, "Alex, listen to this new acoustic guitar track I recorded 🎶", now - 24 * hour),
            Message("m_e2", contactElena.id, "me", "Sounds wonderful! Really love the warm reverb.", now - 20 * hour, MessageStatus.READ)
        ),
        contactZack.id to listOf(
            Message("m_z1", contactZack.id, contactZack.id, "Yo! Any recommendations for Kotlin state flows?", now - 30 * hour),
            Message("m_z2", contactZack.id, "me", "Use collectAsStateWithLifecycle in Compose, works like a charm!", now - 28 * hour, MessageStatus.READ),
            Message("m_z3", contactZack.id, contactZack.id, "Thanks man, that fixed the recomposition issue! 🤝", now - 2 * hour)
        ),
        contactFarai.id to listOf(
            Message("m_f1", contactFarai.id, contactFarai.id, "Just landed in Cape Town! Catch up soon.", now - 36 * hour)
        )
    )

    fun getInitialChats(): List<Chat> {
        return listOf(
            Chat(
                id = contactAmara.id,
                contact = contactAmara,
                lastMessage = initialMessages[contactAmara.id]?.last()
                    ?: Message("def1", contactAmara.id, contactAmara.id, "Can you check the bottom navigation bar? ✨", now - 3 * minute),
                unreadCount = 1,
                isPinned = true
            ),
            Chat(
                id = contactTariro.id,
                contact = contactTariro,
                lastMessage = initialMessages[contactTariro.id]?.last()
                    ?: Message("def2", contactTariro.id, contactTariro.id, "Perfect! See you there ☕", now - 25 * minute),
                unreadCount = 0,
                isPinned = true
            ),
            Chat(
                id = contactDevTeam.id,
                contact = contactDevTeam,
                lastMessage = initialMessages[contactDevTeam.id]?.last()
                    ?: Message("def3", contactDevTeam.id, contactDevTeam.id, "Looking forward to it! 🚀", now - 1 * hour),
                unreadCount = 2,
                isPinned = false
            ),
            Chat(
                id = contactMarcus.id,
                contact = contactMarcus,
                lastMessage = initialMessages[contactMarcus.id]?.last()
                    ?: Message("def4", contactMarcus.id, contactMarcus.id, "All green! Zero lint issues found.", now - 4 * hour),
                unreadCount = 0,
                isPinned = false
            ),
            Chat(
                id = contactZack.id,
                contact = contactZack,
                lastMessage = initialMessages[contactZack.id]?.last()
                    ?: Message("def5", contactZack.id, contactZack.id, "Thanks man, that fixed it! 🤝", now - 2 * hour),
                unreadCount = 0,
                isPinned = false
            ),
            Chat(
                id = contactElena.id,
                contact = contactElena,
                lastMessage = initialMessages[contactElena.id]?.last()
                    ?: Message("def6", contactElena.id, contactElena.id, "Alex, listen to this new track 🎶", now - 24 * hour),
                unreadCount = 0,
                isPinned = false
            ),
            Chat(
                id = contactFarai.id,
                contact = contactFarai,
                lastMessage = initialMessages[contactFarai.id]?.last()
                    ?: Message("def7", contactFarai.id, contactFarai.id, "Just landed in Cape Town! Catch up soon.", now - 36 * hour),
                unreadCount = 0,
                isPinned = false
            )
        )
    }

    val simulatedReplies = listOf(
        "Sounds great! Thanks for letting me know 😊",
        "Got it! That looks super sleek and clean.",
        "Nice! The green branding really makes Kora stand out.",
        "Awesome, working on that right now! 👍",
        "Haha, absolutely! Let's talk more in a bit.",
        "Received loud and clear! 🍃",
        "Super cool! Loving how snappy this is.",
        "Indeed, let's keep testing all features!"
    )
}
