package com.example.data

import com.example.model.EducationalInstitution
import com.example.model.FocusSession
import com.example.model.InstitutionType
import com.example.util.ArabicSearchEngine
import com.example.util.MatchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class DelegationInfo(
    val country: String,
    val state: String,
    val delegation: String,
    val centerLat: Double,
    val centerLon: Double,
    val distanceMeters: Float
)

class InstitutionRepository private constructor() {

    private val _institutions = MutableStateFlow<List<EducationalInstitution>>(defaultInstitutions)
    val institutions: StateFlow<List<EducationalInstitution>> = _institutions.asStateFlow()

    private val _sessions = MutableStateFlow<List<FocusSession>>(defaultSessions)
    val sessions: StateFlow<List<FocusSession>> = _sessions.asStateFlow()

    val delegationCenters: List<DelegationInfo> get() = Companion.delegationCenters

    fun addInstitution(institution: EducationalInstitution) {
        _institutions.value = listOf(institution) + _institutions.value
    }

    fun updateInstitution(updated: EducationalInstitution) {
        _institutions.value = _institutions.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    fun deleteInstitution(id: String) {
        _institutions.value = _institutions.value.filter { it.id != id }
    }

    fun addSession(session: FocusSession) {
        _sessions.value = listOf(session) + _sessions.value
    }

    /**
     * Hierarchical search with Arabic normalization, translation, and fuzzy matching.
     */
    fun search(
        country: String = "",
        state: String = "",
        delegation: String = "",
        queryName: String = ""
    ): List<Pair<EducationalInstitution, MatchResult>> {
        val all = _institutions.value
        if (country.isBlank() && state.isBlank() && delegation.isBlank() && queryName.isBlank()) {
            return all.map { it to MatchResult(isMatch = true, score = 1.0) }
        }

        val results = mutableListOf<Pair<EducationalInstitution, MatchResult>>()
        for (inst in all) {
            val eval = ArabicSearchEngine.evaluateInstitutionMatch(
                inst = inst,
                queryCountry = country,
                queryState = state,
                queryDelegation = delegation,
                queryName = queryName
            )
            if (eval.isMatch) {
                results.add(inst to eval)
            }
        }
        return results.sortedByDescending { it.second.score }
    }

    fun searchByUniqueCode(code: String): EducationalInstitution? {
        if (code.isBlank()) return null
        val clean = code.trim().replace("-", "").replace(" ", "").lowercase()
        return _institutions.value.find {
            val instCode = it.uniqueCode.replace("-", "").replace(" ", "").lowercase()
            val instId = it.id.replace("-", "").replace("_", "").lowercase()
            (instCode.isNotBlank() && (instCode == clean || instCode.contains(clean) || clean.contains(instCode))) ||
                    instId.contains(clean)
        }
    }

    fun getInstitutionsInDelegation(delegationName: String): List<EducationalInstitution> {
        if (delegationName.isBlank()) return emptyList()
        val norm = ArabicSearchEngine.normalizeArabic(delegationName)
        return _institutions.value.filter {
            val instDel = ArabicSearchEngine.normalizeArabic(it.delegation)
            instDel.contains(norm) || norm.contains(instDel) ||
                    ArabicSearchEngine.fuzzySimilarity(it.delegation, delegationName) >= 0.7
        }
    }

    /**
     * Automatically detects the user's current delegation and governorate/state
     * based on their GPS coordinates.
     */
    fun detectDelegationFromCoordinates(lat: Double, lon: Double): DelegationInfo {
        var closest = delegationCenters.first()
        var minDistance = Float.MAX_VALUE

        for (del in delegationCenters) {
            val d = calculateDistanceMeters(lat, lon, del.centerLat, del.centerLon)
            if (d < minDistance) {
                minDistance = d
                closest = del
            }
        }

        return closest.copy(distanceMeters = minDistance)
    }

    fun getAvailableCountries(): List<String> {
        return _institutions.value.map { it.country }.filter { it.isNotBlank() }.distinct()
    }

    fun getStatesForCountry(country: String): List<String> {
        return _institutions.value
            .filter { country.isBlank() || it.country.equals(country, ignoreCase = true) }
            .map { it.state }
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun getDelegationsForState(country: String, state: String): List<String> {
        return _institutions.value
            .filter {
                (country.isBlank() || it.country.equals(country, ignoreCase = true)) &&
                (state.isBlank() || it.state.equals(state, ignoreCase = true))
            }
            .map { it.delegation }
            .filter { it.isNotBlank() }
            .distinct()
    }

    companion object {
        @Volatile
        private var instance: InstitutionRepository? = null

        fun getInstance(): InstitutionRepository {
            return instance ?: synchronized(this) {
                instance ?: InstitutionRepository().also { instance = it }
            }
        }

        fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
            val earthRadius = 6371000.0 // meters
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return (earthRadius * c).toFloat()
        }

        val delegationCenters = listOf(
            DelegationInfo("تونس", "سوسة", "أكودة", 35.8706, 10.5694, 0f),
            DelegationInfo("تونس", "سوسة", "هرقلة", 36.0319, 10.5089, 0f),
            DelegationInfo("تونس", "سوسة", "شط مريم", 35.9190, 10.5590, 0f),
            DelegationInfo("تونس", "سوسة", "حمام سوسة", 35.8580, 10.5980, 0f),
            DelegationInfo("تونس", "سوسة", "سوسة جوهرة", 35.8256, 10.6369, 0f),
            DelegationInfo("تونس", "تونس", "المنار", 36.8317, 10.1472, 0f),
            DelegationInfo("تونس", "تونس", "الشرقية", 36.8436, 10.1969, 0f),
            DelegationInfo("تونس", "تونس", "باب سويقة", 36.7997, 10.1658, 0f),
            DelegationInfo("تونس", "تونس", "حي الخضراء", 36.8242, 10.1803, 0f),
            DelegationInfo("تونس", "أريانة", "أريانة المدينة", 36.8621, 10.1952, 0f),
            DelegationInfo("تونس", "صفاقس", "صفاقس المدينة", 34.7406, 10.7603, 0f),
            DelegationInfo("السعودية", "الرياض", "الدرعية", 24.7164, 46.6198, 0f),
            DelegationInfo("السعودية", "الرياض", "العليا", 24.6865, 46.6872, 0f)
        )

        private val defaultInstitutions = listOf(
            // ==========================================
            // مدرسة المستخدم: المدرسة الإعدادية جمال عبد الجليل بأكودة
            // ==========================================
            EducationalInstitution(
                id = "college_jamel_abdeljelil_akouda",
                uniqueCode = "TN-AKD-JAMEL-01",
                nameAr = "المدرسة الإعدادية جمال عبد الجليل أكودة",
                nameEn = "Collège Djamel Abdeljelil Akouda",
                country = "تونس",
                state = "سوسة",
                delegation = "أكودة",
                city = "أكودة",
                type = InstitutionType.PREP_SCHOOL,
                latitude = 35.8679,
                longitude = 10.5645,
                radiusMeters = 160f,
                isFavorite = true,
                description = "المدرسة الإعدادية جمال عبد الجليل بأكودة (مدرستي) - مدرسة الطالب الأساسية، نطاق قفل الهاتف التلقائي وكتم الإشعارات مفعل على مساحة المدرسة (160م)",
                keywords = listOf("جمال عبد الجليل", "اكودة", "اعدادية", "مدرستي", "jamel abdeljelil", "35.8679", "10.5645")
            ),

            // ==========================================
            // معتمدية أكودة - ولاية سوسة (تونس)
            // ==========================================
            EducationalInstitution(
                id = "lycee_akouda_2mars",
                uniqueCode = "TN-AKD-2MARS-02",
                nameAr = "معهد 2 مارس 1934 بأكودة",
                nameEn = "Lycée 2 Mars 1934 Akouda",
                country = "تونس",
                state = "سوسة",
                delegation = "أكودة",
                city = "أكودة",
                type = InstitutionType.HIGH_SCHOOL,
                latitude = 35.8715,
                longitude = 10.5685,
                radiusMeters = 250f,
                description = "شارع البيئة، أكودة - المعهد الثانوي الرئيسي بأكودة وقاعات امتحانات الباكالوريا",
                keywords = listOf("akouda", "2 mars", "lycee akouda", "اكودة", "اكوده")
            ),
            EducationalInstitution(
                id = "college_ibn_khaldoun_akouda",
                nameAr = "المدرسة الإعدادية ابن خلدون بأكودة",
                nameEn = "Collège Ibn Khaldoun Akouda",
                country = "تونس",
                state = "سوسة",
                delegation = "أكودة",
                city = "أكودة",
                type = InstitutionType.HIGH_SCHOOL,
                latitude = 35.8682,
                longitude = 10.5641,
                radiusMeters = 220f,
                description = "حي الرمانة، أكودة - إعدادية ابن خلدون وملاعب الأنشطة",
                keywords = listOf("ibn khaldoun", "college akouda", "ابن خلدون", "اكودة")
            ),
            EducationalInstitution(
                id = "ecole_habib_thameur_akouda",
                nameAr = "المدرسة الابتدائية الحبيب ثامر بأكودة",
                nameEn = "Habib Thameur Primary School Akouda",
                country = "تونس",
                state = "سوسة",
                delegation = "أكودة",
                city = "أكودة",
                type = InstitutionType.HIGH_SCHOOL,
                latitude = 35.8695,
                longitude = 10.5670,
                radiusMeters = 180f,
                description = "وسط مدينة أكودة - قاعات التدريس وساحة المدرسة",
                keywords = listOf("ecole", "akouda", "habib thameur", "اكودة")
            ),
            EducationalInstitution(
                id = "centre_formation_akouda",
                nameAr = "مركز التكوين والتدريب المهني بأكودة",
                nameEn = "Vocational Training Center of Akouda",
                country = "تونس",
                state = "سوسة",
                delegation = "أكودة",
                city = "أكودة",
                type = InstitutionType.INSTITUTE,
                latitude = 35.8752,
                longitude = 10.5731,
                radiusMeters = 240f,
                description = "المنطقة الحرفية بأكودة - ورشات التقنية والميكانيك والإلكترونيك",
                keywords = listOf("formation", "centre", "akouda", "تكوين مهني")
            ),

            // ==========================================
            // معتمدية هرقلة - ولاية سوسة (تونس)
            // ==========================================
            EducationalInstitution(
                id = "lycee_hergla",
                nameAr = "معهد هرقلة الثانوي",
                nameEn = "Lycée Secondaire de Hergla",
                country = "تونس",
                state = "سوسة",
                delegation = "هرقلة",
                city = "هرقلة",
                type = InstitutionType.HIGH_SCHOOL,
                latitude = 36.0305,
                longitude = 10.5052,
                radiusMeters = 250f,
                description = "مدخل مدينة هرقلة - الفصول الثانوية ومختبرات العلوم",
                keywords = listOf("hergla", "lycee hergla", "هرقلة", "هرقله")
            ),
            EducationalInstitution(
                id = "college_hergla",
                nameAr = "المدرسة الإعدادية بهرقلة",
                nameEn = "Collège de Hergla",
                country = "تونس",
                state = "سوسة",
                delegation = "هرقلة",
                city = "هرقلة",
                type = InstitutionType.HIGH_SCHOOL,
                latitude = 36.0280,
                longitude = 10.5020,
                radiusMeters = 200f,
                description = "شارع الحبيب بورقيبة، هرقلة - الإعدادية النموذجية بهرقلة",
                keywords = listOf("college hergla", "hergla", "اعدادية هرقلة", "هرقلة")
            ),
            EducationalInstitution(
                id = "ecole_republique_hergla",
                nameAr = "المدرسة الابتدائية شارع الجمهورية هرقلة",
                nameEn = "Primary School Avenue de la République Hergla",
                country = "تونس",
                state = "سوسة",
                delegation = "هرقلة",
                city = "هرقلة",
                type = InstitutionType.HIGH_SCHOOL,
                latitude = 36.0330,
                longitude = 10.5100,
                radiusMeters = 180f,
                description = "كورنيش هرقلة والميناء القديم - المدرسة الابتدائية المركزية",
                keywords = listOf("ecole", "republique", "hergla", "هرقلة")
            ),

            // ==========================================
            // المعتمديات المجاورة بسوسة (شط مريم، حمام سوسة)
            // ==========================================
            EducationalInstitution(
                id = "isa_chott_meriem",
                nameAr = "المعهد العالي للعلوم الفلاحية بشط مريم",
                nameEn = "ISA Chott Meriem Agricultural Institute",
                country = "تونس",
                state = "سوسة",
                delegation = "شط مريم",
                city = "شط مريم",
                type = InstitutionType.COLLEGE,
                latitude = 35.9190,
                longitude = 10.5590,
                radiusMeters = 350f,
                description = "شط مريم، ولاية سوسة (بين أكودة وهرقلة) - الحرم الجامعي الفلاحي والمختبرات",
                keywords = listOf("chott meriem", "isa", "agronomie", "شط مريم", "سوسة")
            ),
            EducationalInstitution(
                id = "lycee_hammam_sousse",
                nameAr = "معهد حمام سوسة الثانوي",
                nameEn = "Lycée Hammam Sousse",
                country = "تونس",
                state = "سوسة",
                delegation = "حمام سوسة",
                city = "حمام سوسة",
                type = InstitutionType.HIGH_SCHOOL,
                latitude = 35.8580,
                longitude = 10.5980,
                radiusMeters = 280f,
                description = "شارع الجمهورية، حمام سوسة (قرب أكودة)",
                keywords = listOf("hammam sousse", "lycee", "حمام سوسة")
            ),
            EducationalInstitution(
                id = "sousse_uni",
                nameAr = "جامعة سوسة - الحرم الجامعي",
                nameEn = "University of Sousse",
                country = "تونس",
                state = "سوسة",
                delegation = "سوسة جوهرة",
                city = "سوسة",
                type = InstitutionType.UNIVERSITY,
                latitude = 35.8256,
                longitude = 10.6369,
                radiusMeters = 350f,
                description = "حي الرياض، سوسة - كليات الآداب والعلوم والطب",
                keywords = listOf("sousse", "universite", "faculte de medecine", "جامعة سوسة")
            ),

            // ==========================================
            // ولاية تونس
            // ==========================================
            EducationalInstitution(
                id = "tunis_manar_uni",
                nameAr = "جامعة تونس المنار",
                nameEn = "Tunis El Manar University",
                country = "تونس",
                state = "تونس",
                delegation = "المنار",
                city = "تونس",
                type = InstitutionType.UNIVERSITY,
                latitude = 36.8317,
                longitude = 10.1472,
                radiusMeters = 400f,
                description = "المركب الجامعي المنار - كلية العلوم والرياضيات والتقنيات والمكتبة المركزية",
                keywords = listOf("manar", "faculte des sciences", "utm", "el manar")
            ),
            EducationalInstitution(
                id = "insat_tunis",
                nameAr = "المعهد العالي للعلوم التطبيقية والتكنولوجيا (INSAT)",
                nameEn = "INSAT - National Institute of Applied Sciences",
                country = "تونس",
                state = "تونس",
                delegation = "الشرقية",
                city = "تونس",
                type = InstitutionType.INSTITUTE,
                latitude = 36.8436,
                longitude = 10.1969,
                radiusMeters = 300f,
                description = "المركز العمراني الشمالي، الشرقية - تكنولوجيا وهندسة البرمجيات والشبكات",
                keywords = listOf("insat", "centre urbain nord", "institut", "charguia")
            ),
            EducationalInstitution(
                id = "enit_tunis",
                nameAr = "المدرسة الوطنية للمهندسين بتونس (ENIT)",
                nameEn = "National Engineering School of Tunis (ENIT)",
                country = "تونس",
                state = "تونس",
                delegation = "المنار",
                city = "تونس",
                type = InstitutionType.COLLEGE,
                latitude = 36.8306,
                longitude = 10.1481,
                radiusMeters = 280f,
                description = "حرم المنار - أقدم مدرسة مهندسين في تونس",
                keywords = listOf("enit", "ingenieurs", "el belvedere")
            ),
            EducationalInstitution(
                id = "sadiki_high_school",
                nameAr = "المعهد الصادقي (ثانوية الصادقية التاريخية)",
                nameEn = "Sadiki College High School",
                country = "تونس",
                state = "تونس",
                delegation = "باب سويقة",
                city = "تونس",
                type = InstitutionType.HIGH_SCHOOL,
                latitude = 36.7997,
                longitude = 10.1658,
                radiusMeters = 200f,
                description = "ساحة القصبة - المدرسة العريقة لتخريج النخب التونسية",
                keywords = listOf("sadiki", "college sadiki", "kasbah", "lycee")
            ),
            EducationalInstitution(
                id = "bourguiba_institute",
                nameAr = "معهد بورقيبة للغات الحية (IBLV)",
                nameEn = "Bourguiba Institute of Modern Languages",
                country = "تونس",
                state = "تونس",
                delegation = "حي الخضراء",
                city = "تونس",
                type = InstitutionType.INSTITUTE,
                latitude = 36.8242,
                longitude = 10.1803,
                radiusMeters = 180f,
                description = "شارع الحرية - معهد تدريس اللغات والترجمة",
                keywords = listOf("iblv", "bourguiba", "langues", "khadra")
            ),
            EducationalInstitution(
                id = "lycee_pilote_ariana",
                nameAr = "المعهد النموذجي بأريانة",
                nameEn = "Ariana Pilot High School",
                country = "تونس",
                state = "أريانة",
                delegation = "أريانة المدينة",
                city = "أريانة",
                type = InstitutionType.HIGH_SCHOOL,
                latitude = 36.8621,
                longitude = 10.1952,
                radiusMeters = 220f,
                description = "شارع الاستقلال، أريانة - نخب الطلاب المتفوقين",
                keywords = listOf("lycee pilote", "ariana", "pilote")
            ),
            EducationalInstitution(
                id = "sfax_uni",
                nameAr = "جامعة صفاقس - مجمع الكليات",
                nameEn = "University of Sfax",
                country = "تونس",
                state = "صفاقس",
                delegation = "صفاقس المدينة",
                city = "صفاقس",
                type = InstitutionType.UNIVERSITY,
                latitude = 34.7406,
                longitude = 10.7603,
                radiusMeters = 450f,
                description = "طريق المطار وطريق سكرة - كليات العلوم والطب والهندسة",
                keywords = listOf("sfax", "universite de sfax", "enis")
            ),

            // ==========================================
            // السعودية ومصر
            // ==========================================
            EducationalInstitution(
                id = "ksu_main",
                nameAr = "جامعة الملك سعود - الحرم الرئيسي",
                nameEn = "King Saud University - Main Campus",
                country = "السعودية",
                state = "الرياض",
                delegation = "الدرعية",
                city = "الرياض",
                type = InstitutionType.UNIVERSITY,
                latitude = 24.7164,
                longitude = 46.6198,
                radiusMeters = 500f,
                description = "الحرم الجامعي الرئيسي، الدرعية - كليات الهندسة والعلوم والطب",
                keywords = listOf("ksu", "king saud", "riyadh", "diriyah")
            ),
            EducationalInstitution(
                id = "cairo_uni",
                nameAr = "جامعة القاهرة - الحرم التاريخي",
                nameEn = "Cairo University",
                country = "مصر",
                state = "الجيزة",
                delegation = "الدقي",
                city = "القاهرة",
                type = InstitutionType.UNIVERSITY,
                latitude = 30.0263,
                longitude = 31.2117,
                radiusMeters = 550f,
                description = "مجمع الكليات الرئيسي وقبة الجامعة",
                keywords = listOf("cairo university", "giza", "dokki")
            )
        )

        private val defaultSessions = listOf(
            FocusSession(
                id = "s_1",
                institutionId = "lycee_akouda_2mars",
                institutionName = "معهد 2 مارس 1934 بأكودة",
                startTimeMillis = System.currentTimeMillis() - 86400000L - 10800000L,
                endTimeMillis = System.currentTimeMillis() - 86400000L,
                durationMinutes = 180L,
                dateFormatted = "أمس"
            ),
            FocusSession(
                id = "s_2",
                institutionId = "lycee_hergla",
                institutionName = "معهد هرقلة الثانوي",
                startTimeMillis = System.currentTimeMillis() - (86400000L * 2) - 7200000L,
                endTimeMillis = System.currentTimeMillis() - (86400000L * 2),
                durationMinutes = 120L,
                dateFormatted = "منذ يومين"
            )
        )
    }
}
