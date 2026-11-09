package com.example.ui.uniqueid

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InstitutionRepository
import com.example.model.EducationalInstitution
import com.example.service.FocusManager
import com.example.ui.institutions.GeminiSearchDialog
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusBluePrimary
import com.example.ui.theme.FocusBlueSecondary
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusRedAlert

@Composable
fun UniqueIdSearchScreen(
    onNavigateToMapWithInstitution: (EducationalInstitution) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val repository = remember { InstitutionRepository.getInstance() }
    val focusManager = remember { FocusManager.getInstance() }

    val institutions by repository.institutions.collectAsState()
    val focusState by focusManager.focusState.collectAsState()

    var inputCode by remember { mutableStateOf("TN-AKD-JAMEL-01") }
    var searchedInstitution by remember { mutableStateOf<EducationalInstitution?>(repository.searchByUniqueCode("TN-AKD-JAMEL-01")) }
    var showNotFound by remember { mutableStateOf(false) }
    var showGeminiDialog by remember { mutableStateOf(false) }

    val sampleCodes = listOf(
        "TN-AKD-JAMEL-01" to "إعدادية جمال عبد الجليل بأكودة ⭐",
        "TN-AKD-2MARS-02" to "معهد 2 مارس بأكودة",
        "TN-HRG-LYCEE-01" to "معهد هرقلة الثانوي",
        "TN-CHM-ISA-01" to "فلاحية شط مريم",
        "college_ibn_khaldoun_akouda" to "إعدادية ابن خلدون"
    )

    fun performSearch(code: String) {
        val trimmed = code.trim()
        val found = repository.searchByUniqueCode(trimmed) ?: institutions.find {
            it.uniqueCode.equals(trimmed, ignoreCase = true) ||
                    it.id.equals(trimmed, ignoreCase = true) ||
                    it.nameAr.contains(trimmed)
        }
        searchedInstitution = found
        showNotFound = found == null && trimmed.isNotBlank()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Hero Card: National Educational Code
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("unique_id_hero_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, FocusBluePrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(FocusBluePrimary, Color(0xFF0284C7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "البحث بالمعرف الوحيد للمؤسسة",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "الرمز التربوي الموحد (Identifiant Unique) لتحديد المدرسة بدقة",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FocusEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("مباشر ✓", color = FocusEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Input Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "أدخل المعرف الوحيد للمؤسسة:",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = {
                        inputCode = it
                        performSearch(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unique_code_input_field"),
                    placeholder = {
                        Text("مثال: TN-AKD-JAMEL-01", color = Color(0xFF64748B), fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = FocusBluePrimary
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    inputCode = clip.trim()
                                    performSearch(inputCode)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "لصق من الحافظة",
                                    tint = FocusBlueSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Button(
                                onClick = { performSearch(inputCode) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FocusBluePrimary),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                ),
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .testTag("search_code_button")
                            ) {
                                Text("تأكيد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusBluePrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick sample code chips
                Text(
                    text = "رموز سريعة للمعاينة المباشرة:",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleCodes.forEach { (code, label) ->
                        val isSelected = inputCode == code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) FocusBluePrimary else Color(0xFF0F172A))
                                .border(
                                    1.dp,
                                    if (isSelected) FocusBlueSecondary else Color(0xFF334155),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    inputCode = code
                                    performSearch(code)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Result Card: Stylized National Educational ID Card
        if (searchedInstitution != null) {
            val inst = searchedInstitution!!
            val dist = InstitutionRepository.calculateDistanceMeters(
                focusState.userLatitude,
                focusState.userLongitude,
                inst.latitude,
                inst.longitude
            ).toInt()
            val isInside = dist <= inst.radiusMeters

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("matched_institution_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF132238)),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (inst.isFavorite) FocusAmber.copy(alpha = 0.8f) else FocusBluePrimary.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Top Bar of Educational ID Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (inst.isFavorite) FocusAmber.copy(alpha = 0.2f)
                                        else FocusBluePrimary.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (inst.isFavorite) Icons.Default.Star else Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (inst.isFavorite) FocusAmber else FocusBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "المعرف المعتمد: ",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = inst.uniqueCode.ifBlank { inst.id },
                                        color = Color(0xFF38BDF8),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                }
                                if (inst.isFavorite) {
                                    Text(
                                        text = "⭐ مدرسة المستخدم المفضلة (أدرس فيها)",
                                        color = FocusAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Inside or Outside Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isInside) FocusRedAlert.copy(alpha = 0.2f)
                                    else FocusEmerald.copy(alpha = 0.15f)
                                )
                                .border(
                                    1.dp,
                                    if (isInside) FocusRedAlert else FocusEmerald,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isInside) "داخل النطاق (مقفل)" else "خارج النطاق",
                                color = if (isInside) FocusRedAlert else FocusEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // School Name
                    Text(
                        text = inst.nameAr,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    if (inst.nameEn.isNotBlank()) {
                        Text(
                            text = inst.nameEn,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Geographic Specs Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0B1426))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("المعتمدية والولاية", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text("${inst.delegation} • ${inst.state}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("إحداثيات GPS", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text("${"%.4f".format(inst.latitude)}, ${"%.4f".format(inst.longitude)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("مساحة القفل", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text("${inst.radiusMeters.toInt()} متر", color = FocusEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (inst.description.isNotBlank()) {
                        Text(
                            text = inst.description,
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                focusManager.simulateEnterInstitution(inst.id, context)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isInside) FocusRedAlert else FocusBluePrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isInside) "داخل الحرم" else "محاكاة الدخول", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onNavigateToMapWithInstitution(inst)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FocusBlueSecondary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = FocusBlueSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("عرض بالخريطة", color = FocusBlueSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (showNotFound) {
            // Not Found card with AI helper
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF23161C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, FocusRedAlert.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "لم يتم العثور على مؤسسة مسجلة بالرمز: $inputCode",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يمكنك البحث عن اسم المدرسة بواسطة محرك Gemini AI أو إضافتها يدوياً للقائمة.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showGeminiDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusBluePrimary)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("البحث بواسطة Gemini AI", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showGeminiDialog) {
        GeminiSearchDialog(
            initialQuery = inputCode,
            onDismiss = { showGeminiDialog = false },
            onInstitutionAdded = { added ->
                searchedInstitution = added
                inputCode = added.uniqueCode.ifBlank { added.id }
            },
            onNavigateToInstitution = { inst ->
                showGeminiDialog = false
                onNavigateToMapWithInstitution(inst)
            }
        )
    }
}
