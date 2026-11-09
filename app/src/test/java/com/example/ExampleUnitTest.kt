package com.example

import com.example.data.InstitutionRepository
import com.example.model.EducationalInstitution
import com.example.model.InstitutionType
import com.example.util.ArabicSearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testInstitutionRepositoryCrud() {
        val repository = InstitutionRepository.getInstance()
        val initialCount = repository.institutions.value.size
        assertTrue(initialCount > 0)

        // Add
        val custom = EducationalInstitution(
            id = "test_custom_school_1",
            nameAr = "مدرسة التجربة النموذجية",
            nameEn = "Test Model School",
            country = "تونس",
            state = "أريانة",
            delegation = "حي التضامن",
            city = "أريانة",
            type = InstitutionType.HIGH_SCHOOL,
            latitude = 36.8500,
            longitude = 10.1500,
            radiusMeters = 300f
        )
        repository.addInstitution(custom)
        assertEquals(initialCount + 1, repository.institutions.value.size)

        // Update
        val updated = custom.copy(radiusMeters = 450f, nameAr = "مدرسة التجربة المحدثة")
        repository.updateInstitution(updated)
        val found = repository.institutions.value.find { it.id == custom.id }
        assertNotNull(found)
        assertEquals(450f, found?.radiusMeters)
        assertEquals("مدرسة التجربة المحدثة", found?.nameAr)

        // Delete
        repository.deleteInstitution(custom.id)
        assertEquals(initialCount, repository.institutions.value.size)
    }

    @Test
    fun testDistanceCalculation() {
        val dist = InstitutionRepository.calculateDistanceMeters(
            36.8317, 10.1472, // Tunis El Manar
            36.8436, 10.1969  // INSAT Charguia
        )
        assertTrue(dist > 2000f)
        assertTrue(dist < 7000f)

        val zeroDist = InstitutionRepository.calculateDistanceMeters(
            36.8317, 10.1472,
            36.8317, 10.1472
        )
        assertEquals(0f, zeroDist, 1f)
    }

    @Test
    fun testArabicNormalization() {
        val normalized1 = ArabicSearchEngine.normalizeArabic("جَامِعَةُ تُونِس المَنَارِ")
        assertEquals("جامعه تونس المنار", normalized1)

        val normalized2 = ArabicSearchEngine.normalizeArabic("أَرِيَانَة")
        assertEquals("اريانه", normalized2)
    }

    @Test
    fun testArabicTranslationAndTransliteration() {
        val trans = ArabicSearchEngine.translateAndTransliterate("جامعة")
        assertTrue(trans.translatedKeywords.contains("university"))

        val countryTrans = ArabicSearchEngine.translateAndTransliterate("تونس")
        assertTrue(countryTrans.translatedKeywords.contains("tunisia") || countryTrans.translatedKeywords.contains("tunis"))
    }

    @Test
    fun testHierarchicalSearchByCountryStateDelegation() {
        val repository = InstitutionRepository.getInstance()

        // 1. Search by Country = "تونس", State = "تونس", Delegation = "المنار"
        val results1 = repository.search(
            country = "تونس",
            state = "تونس",
            delegation = "المنار",
            queryName = ""
        )
        assertTrue(results1.isNotEmpty())
        assertTrue(results1.any { it.first.nameAr.contains("المنار") })

        // 2. Search by Country = "تونس", Delegation = "الشرقية"
        val results2 = repository.search(
            country = "تونس",
            state = "",
            delegation = "الشرقية",
            queryName = ""
        )
        assertTrue(results2.isNotEmpty())
        assertTrue(results2.any { it.first.nameAr.contains("INSAT") })

        // 3. Search with approximate name: "منار" (without "ال")
        val results3 = repository.search(
            country = "",
            state = "",
            delegation = "",
            queryName = "منار"
        )
        assertTrue(results3.isNotEmpty())
        assertTrue(results3.any { it.first.nameAr.contains("المنار") })
    }

    @Test
    fun testJamelAbdeljelilSchoolAndDelegation() {
        val repository = InstitutionRepository.getInstance()

        // Verify the user's preparatory school exists with exact coordinates and radius
        val jamelSchool = repository.institutions.value.find { it.id == "college_jamel_abdeljelil_akouda" }
        assertNotNull(jamelSchool)
        assertEquals("المدرسة الإعدادية جمال عبد الجليل أكودة", jamelSchool?.nameAr)
        assertEquals(35.8679, jamelSchool?.latitude ?: 0.0, 0.0001)
        assertEquals(10.5645, jamelSchool?.longitude ?: 0.0, 0.0001)
        assertEquals(160f, jamelSchool?.radiusMeters ?: 0f, 0.1f)
        assertEquals("أكودة", jamelSchool?.delegation)
        assertEquals("سوسة", jamelSchool?.state)
        assertTrue(jamelSchool?.isFavorite == true)

        // Test GPS delegation detection at the school's exact location
        val detected = repository.detectDelegationFromCoordinates(35.8679, 10.5645)
        assertEquals("أكودة", detected.delegation)
        assertEquals("سوسة", detected.state)

        // Test GPS delegation detection near Hergla
        val herglaDetected = repository.detectDelegationFromCoordinates(36.0319, 10.5089)
        assertEquals("هرقلة", herglaDetected.delegation)
        assertEquals("سوسة", herglaDetected.state)

        // Verify distance inside school radius (e.g. 50 meters away)
        val dist = InstitutionRepository.calculateDistanceMeters(35.8679, 10.5645, 35.8681, 10.5647)
        assertTrue("Distance should be within 160m radius", dist < 160f)
    }

    @Test
    fun testUniqueCodeSearch() {
        val repository = InstitutionRepository.getInstance()

        // 1. Exact match with hyphens
        val foundExact = repository.searchByUniqueCode("TN-AKD-JAMEL-01")
        assertNotNull(foundExact)
        assertEquals("college_jamel_abdeljelil_akouda", foundExact?.id)

        // 2. Case-insensitive and hyphen-free match
        val foundLoose = repository.searchByUniqueCode("tnakdjamel01")
        assertNotNull(foundLoose)
        assertEquals("college_jamel_abdeljelil_akouda", foundLoose?.id)

        // 3. Search by partial code
        val foundPartial = repository.searchByUniqueCode("JAMEL")
        assertNotNull(foundPartial)
        assertEquals("college_jamel_abdeljelil_akouda", foundPartial?.id)
    }

    @Test
    fun testHideTimeForStudent() {
        val settings = com.example.model.UserSettings()
        assertTrue("hideTimeForStudent should be true by default to avoid student distraction", settings.hideTimeForStudent)
    }
}

