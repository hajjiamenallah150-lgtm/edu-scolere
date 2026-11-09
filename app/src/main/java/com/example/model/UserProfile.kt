package com.example.model

data class UserProfile(
    val isLinkedWithGoogle: Boolean = true,
    val displayName: String = "حجي أمان الله (Hajji Amenallah)",
    val email: String = "hajjiamenallah150@gmail.com",
    val schoolOrUniversity: String = "معهد 2 مارس بأكودة",
    val country: String = "تونس",
    val state: String = "سوسة",
    val delegation: String = "أكودة",
    val studentId: String = "EDU-2026-7841",
    val avatarInitial: String = "H"
)
