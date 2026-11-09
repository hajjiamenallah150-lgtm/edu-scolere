package com.example.model

enum class InstitutionType(val labelAr: String, val labelEn: String) {
    UNIVERSITY("جامعة", "University"),
    HIGH_SCHOOL("مدرسة ثانوية", "High School"),
    PREP_SCHOOL("مدرسة إعدادية", "Preparatory School"),
    PRIMARY_SCHOOL("مدرسة ابتدائية", "Primary School"),
    COLLEGE("كلية تقنية", "College"),
    INSTITUTE("معهد تعليمي", "Institute"),
    VOCATIONAL_TRAINING("تكوين مهني", "Vocational Training"),
    LIBRARY("مكتبة عامة", "Public Library")
}

data class EducationalInstitution(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val country: String = "تونس", // الدولة
    val state: String = "سوسة", // الولاية / المحافظة
    val delegation: String = "أكودة", // المعتمدية / الدائرة
    val city: String = "أكودة", // المدينة
    val type: InstitutionType,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 160f,
    val uniqueCode: String = "", // المعرف الوحيد للمؤسسة التعليمية
    val isFavorite: Boolean = false,
    val isPreloaded: Boolean = true,
    val autoLockEnabled: Boolean = true,
    val silentModeEnabled: Boolean = true,
    val activeStartHour: Int = 7,
    val activeStartMinute: Int = 30,
    val activeEndHour: Int = 16,
    val activeEndMinute: Int = 0,
    val activeDays: List<Int> = listOf(1, 2, 3, 4, 5),
    val description: String = "",
    val keywords: List<String> = emptyList()
)
