package com.example.util

import com.example.model.EducationalInstitution
import kotlin.math.max
import kotlin.math.min

data class SearchTranslation(
    val normalizedArabic: String,
    val translatedKeywords: List<String>,
    val transliteration: String
)

data class MatchResult(
    val isMatch: Boolean,
    val score: Double,
    val matchedField: String = "",
    val translationHint: String = ""
)

object ArabicSearchEngine {

    private val rawTranslationDictionary = mapOf(
        "جامعة" to listOf("university", "universite", "campus", "fac"),
        "كلية" to listOf("college", "faculty", "faculte"),
        "مدرسة" to listOf("school", "ecole", "lycee"),
        "معهد" to listOf("institute", "institut", "insat", "iset"),
        "مكتبة" to listOf("library", "bibliotheque"),
        "ثانوية" to listOf("high school", "lycee"),
        "نموذجية" to listOf("pilot", "model"),
        "تقنية" to listOf("technology", "technical", "polytechnique"),
        "علوم" to listOf("sciences", "science"),
        "طب" to listOf("medicine", "medecine"),
        "هندسة" to listOf("engineering", "ingenieur"),

        // Countries
        "تونس" to listOf("tunisia", "tunis", "tunisie"),
        "السعودية" to listOf("saudi", "ksa", "arabia"),
        "مصر" to listOf("egypt", "egypte"),
        "الجزائر" to listOf("algeria", "algerie"),
        "المغرب" to listOf("morocco", "maroc"),
        "الإمارات" to listOf("uae", "emirates"),

        // States / Governorates (الولايات والمحافظات)
        "أريانة" to listOf("ariana"),
        "بن عروس" to listOf("ben arous"),
        "منوبة" to listOf("manouba"),
        "سوسة" to listOf("sousse"),
        "صفاقس" to listOf("sfax"),
        "نابل" to listOf("nabeul"),
        "بنزرت" to listOf("bizerte"),
        "المنستير" to listOf("monastir"),
        "القيروان" to listOf("kairouan"),
        "الرياض" to listOf("riyadh"),
        "جدة" to listOf("jeddah"),
        "القاهرة" to listOf("cairo"),

        // Delegations / Districts (المعتمديات والدوائر)
        "المنار" to listOf("el manar", "manar"),
        "المرسى" to listOf("la marsa", "marsa"),
        "باردو" to listOf("bardo"),
        "الشرقية" to listOf("charguia"),
        "قرطاج" to listOf("carthage"),
        "حي الخضراء" to listOf("cite el khadra"),
        "باب سويقة" to listOf("bab souika"),
        "العليا" to listOf("olaya"),
        "الدرعية" to listOf("diriyah")
    )

    private val translationDictionary = rawTranslationDictionary.mapKeys { normalizeArabic(it.key) }

    fun normalizeArabic(text: String): String {
        if (text.isBlank()) return ""
        var s = text.trim().lowercase()

        // Remove diacritics / Tashkeel
        s = s.replace(Regex("[\u064B-\u065F\u0670]"), "")

        // Normalize Alef forms: أ, إ, آ, ٱ -> ا
        s = s.replace(Regex("[أإآٱ]"), "ا")

        // Normalize Taa Marbuta: ة -> ه
        s = s.replace("ة", "ه")

        // Normalize Yaa: ى -> ي
        s = s.replace("ى", "ي")

        // Tatweel
        s = s.replace("ـ", "")

        // Collapse whitespace
        s = s.replace(Regex("\\s+"), " ")

        return s
    }

    fun translateAndTransliterate(input: String): SearchTranslation {
        val normalized = normalizeArabic(input)
        val tokens = normalized.split(" ").filter { it.isNotBlank() }

        val translated = mutableListOf<String>()
        for (token in tokens) {
            translationDictionary[token]?.let { translated.addAll(it) }

            // Strip "ال"
            if (token.startsWith("ال") && token.length > 3) {
                val bare = token.removePrefix("ال")
                translationDictionary[bare]?.let { translated.addAll(it) }
            }
        }

        val translit = transliterateArabicToLatin(normalized)

        return SearchTranslation(
            normalizedArabic = normalized,
            translatedKeywords = translated.distinct(),
            transliteration = translit
        )
    }

    private fun transliterateArabicToLatin(arabic: String): String {
        val map = mapOf(
            'ا' to "a", 'ب' to "b", 'ت' to "t", 'ث' to "th", 'ج' to "j",
            'ح' to "h", 'خ' to "kh", 'د' to "d", 'ذ' to "dh", 'ر' to "r",
            'ز' to "z", 'س' to "s", 'ش' to "sh", 'ص' to "s", 'ض' to "d",
            'ط' to "t", 'ظ' to "z", 'ع' to "a", 'غ' to "gh", 'ف' to "f",
            'ق' to "q", 'ك' to "k", 'ل' to "l", 'م' to "m", 'ن' to "n",
            'ه' to "h", 'و' to "w", 'ي' to "y"
        )
        val sb = StringBuilder()
        for (ch in arabic) {
            sb.append(map[ch] ?: ch)
        }
        return sb.toString()
    }

    fun fuzzySimilarity(s1: String, s2: String): Double {
        val str1 = normalizeArabic(s1)
        val str2 = normalizeArabic(s2)

        if (str1 == str2) return 1.0
        if (str1.isEmpty() || str2.isEmpty()) return 0.0

        if (str1.contains(str2) || str2.contains(str1)) {
            val minLen = min(str1.length, str2.length).toDouble()
            val maxLen = max(str1.length, str2.length).toDouble()
            return 0.75 + (0.25 * (minLen / maxLen))
        }

        val distance = levenshteinDistance(str1, str2)
        val maxLen = max(str1.length, str2.length)
        val similarity = 1.0 - (distance.toDouble() / maxLen.toDouble())
        return similarity.coerceIn(0.0, 1.0)
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val len0 = lhs.length + 1
        val len1 = rhs.length + 1

        var cost = IntArray(len0)
        var newcost = IntArray(len0)

        for (i in 0 until len0) cost[i] = i

        for (j in 1 until len1) {
            newcost[0] = j
            for (i in 1 until len0) {
                val match = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                val costReplace = cost[i - 1] + match
                val costInsert = cost[i] + 1
                val costDelete = newcost[i - 1] + 1
                newcost[i] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newcost
            newcost = swap
        }
        return cost[len0 - 1]
    }

    fun evaluateInstitutionMatch(
        inst: EducationalInstitution,
        queryCountry: String,
        queryState: String,
        queryDelegation: String,
        queryName: String
    ): MatchResult {
        // 1. Country filter (الدولة)
        if (queryCountry.isNotBlank()) {
            val cSim = fuzzySimilarity(inst.country, queryCountry)
            val cTrans = translateAndTransliterate(queryCountry)
            val matchesCountry = cSim >= 0.65 ||
                    cTrans.translatedKeywords.any { inst.country.contains(it, ignoreCase = true) } ||
                    inst.nameEn.contains(queryCountry, ignoreCase = true)

            if (!matchesCountry) {
                return MatchResult(isMatch = false, score = 0.0)
            }
        }

        // 2. State filter (الولاية)
        if (queryState.isNotBlank()) {
            val sSim = max(
                fuzzySimilarity(inst.state, queryState),
                fuzzySimilarity(inst.city, queryState)
            )
            val sTrans = translateAndTransliterate(queryState)
            val matchesState = sSim >= 0.65 ||
                    sTrans.translatedKeywords.any {
                        inst.state.contains(it, ignoreCase = true) ||
                        inst.city.contains(it, ignoreCase = true) ||
                        inst.nameEn.contains(it, ignoreCase = true)
                    }

            if (!matchesState) {
                return MatchResult(isMatch = false, score = 0.0)
            }
        }

        // 3. Delegation filter (المعتمدية)
        if (queryDelegation.isNotBlank()) {
            val dSim = max(
                fuzzySimilarity(inst.delegation, queryDelegation),
                fuzzySimilarity(inst.description, queryDelegation)
            )
            val dTrans = translateAndTransliterate(queryDelegation)
            val matchesDelegation = dSim >= 0.6 ||
                    dTrans.translatedKeywords.any {
                        inst.delegation.contains(it, ignoreCase = true) ||
                        inst.nameEn.contains(it, ignoreCase = true) ||
                        inst.description.contains(it, ignoreCase = true)
                    } ||
                    inst.nameAr.contains(queryDelegation, ignoreCase = true)

            if (!matchesDelegation) {
                return MatchResult(isMatch = false, score = 0.0)
            }
        }

        // 4. Approximate Name (اسم قريب من المؤسسة)
        if (queryName.isBlank()) {
            return MatchResult(isMatch = true, score = 1.0, matchedField = "الموقع الإداري")
        }

        val nameTrans = translateAndTransliterate(queryName)
        val normQuery = nameTrans.normalizedArabic

        val simNameAr = fuzzySimilarity(inst.nameAr, queryName)

        val queryTokens = normQuery.split(" ").filter { it.length > 1 }
        val nameTokens = normalizeArabic(inst.nameAr).split(" ")
        var tokenMatchCount = 0
        for (qTok in queryTokens) {
            val bestTokSim = nameTokens.maxOfOrNull { fuzzySimilarity(it, qTok) } ?: 0.0
            if (bestTokSim >= 0.70) {
                tokenMatchCount++
            }
        }
        val tokenMatchRatio = if (queryTokens.isNotEmpty()) tokenMatchCount.toDouble() / queryTokens.size else 0.0

        val matchesEnglish = inst.nameEn.contains(queryName, ignoreCase = true) ||
                nameTrans.translatedKeywords.any { keyword ->
                    inst.nameEn.contains(keyword, ignoreCase = true) ||
                    inst.keywords.any { it.contains(keyword, ignoreCase = true) }
                }

        val matchesKeywords = inst.keywords.any { kw ->
            fuzzySimilarity(kw, queryName) >= 0.75 || kw.contains(normQuery, ignoreCase = true)
        }

        val totalScore = max(
            simNameAr,
            max(tokenMatchRatio, if (matchesEnglish || matchesKeywords) 0.85 else 0.0)
        )

        val isMatch = totalScore >= 0.45 || matchesEnglish || matchesKeywords

        val hint = if (nameTrans.translatedKeywords.isNotEmpty()) {
            "الترجمة المكتشفة: ${nameTrans.translatedKeywords.take(2).joinToString(", ")}"
        } else if (nameTrans.transliteration.isNotBlank()) {
            "النقل الصوتي: ${nameTrans.transliteration}"
        } else ""

        return MatchResult(
            isMatch = isMatch,
            score = totalScore,
            matchedField = if (simNameAr > 0.7) "تطابق اسم المؤسسة" else "بحث تقريبي وترجمة",
            translationHint = hint
        )
    }
}
