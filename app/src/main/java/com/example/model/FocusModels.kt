package com.example.model

data class FocusState(
    val isProtectionActive: Boolean = true,
    val isInsideZone: Boolean = false,
    val currentInstitution: EducationalInstitution? = null,
    val entryTimeMillis: Long = 0L,
    val sessionDurationSeconds: Long = 0L,
    val isPhoneLocked: Boolean = false,
    val nearestInstitution: EducationalInstitution? = null,
    val distanceToNearestMeters: Float = 0f,
    val userLatitude: Double = 35.8679, // مدرسة جمال عبد الجليل بأكودة (سوسة، تونس)
    val userLongitude: Double = 10.5645,
    val isSimulatedLocation: Boolean = false,
    val totalFocusMinutesToday: Int = 145,
    val savedDistractionsCount: Int = 28,
    val isDndActive: Boolean = false,
    val detectedDelegation: String = "أكودة", // المعتمدية المكتشفة حالياً
    val detectedState: String = "سوسة", // الولاية
    val detectedCountry: String = "تونس", // الدولة
    val institutionsInMyDelegation: List<EducationalInstitution> = emptyList()
)

data class FocusSession(
    val id: String,
    val institutionId: String,
    val institutionName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Long,
    val dateFormatted: String
)

data class UserSettings(
    val autoLockOnEntry: Boolean = true,
    val systemAdminLock: Boolean = false,
    val strictOverlayLock: Boolean = true,
    val silentModeEnabled: Boolean = true,
    val emergencyPin: String = "1234",
    val emergencyContact: String = "112",
    val simulationMode: Boolean = true,
    val soundProfile: SoundProfile = SoundProfile.SILENT,
    val isArabic: Boolean = true,
    val hideTimeForStudent: Boolean = true
)

enum class SoundProfile(val labelAr: String, val labelEn: String) {
    SILENT("صامت بالكامل", "Completely Silent"),
    VIBRATE("اهتزاز فقط", "Vibrate Only"),
    DO_NOT_DISTURB("عدم الإزعاج (DND)", "Do Not Disturb")
}

data class MotivationalQuote(
    val quoteAr: String,
    val quoteEn: String,
    val author: String
)

val STUDY_QUOTES = listOf(
    MotivationalQuote(
        quoteAr = "طلب العلم فريضة، والتركيز مفتاح الفهم والإتقان.",
        quoteEn = "Seeking knowledge requires profound focus and mastery.",
        author = "حكمة عربية"
    ),
    MotivationalQuote(
        quoteAr = "كل دقيقة تصرفها في المشتتات تسرق من مستقبلك الأكاديمي.",
        quoteEn = "Every minute spent on distractions steals from your academic future.",
        author = "إرشاد دراسي"
    ),
    MotivationalQuote(
        quoteAr = "من لم يذق مر التعلم ساعة تجرع ذل الجهل طول حياته.",
        quoteEn = "He who has not tasted the bitter patience of study tastes ignorance forever.",
        author = "الإمام الشافعي"
    ),
    MotivationalQuote(
        quoteAr = "الانضباط الذاتي هو الجسر الفاصل بين أهدافك وإنجازاتك.",
        quoteEn = "Self-discipline is the bridge between goals and accomplishment.",
        author = "جيم رون"
    ),
    MotivationalQuote(
        quoteAr = "العقل المنتبه في قاعة الدرس يصنع عباقرة الغد.",
        quoteEn = "An attentive mind in the classroom shapes tomorrow's geniuses.",
        author = "توجيه تربوي"
    )
)
