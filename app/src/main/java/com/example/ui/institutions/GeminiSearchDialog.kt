package com.example.ui.institutions

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.GeminiSearchResult
import com.example.ai.GeminiSearchService
import com.example.data.InstitutionRepository
import com.example.model.EducationalInstitution
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusBluePrimary
import com.example.ui.theme.FocusBlueSecondary
import com.example.ui.theme.FocusDarkSurface
import com.example.ui.theme.FocusDarkSurfaceVariant
import com.example.ui.theme.FocusEmerald
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeminiSearchDialog(
    initialQuery: String = "",
    onDismiss: () -> Unit,
    onInstitutionAdded: (EducationalInstitution) -> Unit,
    onNavigateToInstitution: (EducationalInstitution) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geminiService = remember { GeminiSearchService.getInstance() }
    val repo = remember { InstitutionRepository.getInstance() }

    var query by remember { mutableStateOf(initialQuery) }
    var isLoading by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<GeminiSearchResult?>(null) }
    var isOnline by remember { mutableStateOf(geminiService.isInternetAvailable(context)) }
    var addedIds by remember { mutableStateOf(setOf<String>()) }
    var isPaidMode by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf("gemini-3.8-flash") }

    val quickQueries = listOf(
        "إعدادية جمال عبد الجليل بأكودة ⭐",
        "مؤسسات معتمدية أكودة 🏫",
        "مدارس ومعاهد هرقلة 🌊",
        "جامعات ولاية سوسة 🎓",
        "المعهد الفلاحي بشط مريم 🌾",
        "مراكز التكوين المهني 🔧"
    )

    fun executeSearch(q: String) {
        if (q.isBlank()) return
        query = q
        isLoading = true
        scope.launch {
            val res = geminiService.searchWithGemini(
                query = q,
                context = context,
                modelName = if (isPaidMode) "gemini-4-argon" else selectedModel,
                isPaidMode = isPaidMode
            )
            searchResult = res
            isOnline = res.isOnline
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        isOnline = geminiService.isInternetAvailable(context)
        if (initialQuery.isNotBlank()) {
            executeSearch(initialQuery)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = FocusDarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Gemini Gradient
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF7C4DFF), FocusBluePrimary, Color(0xFF00E5FF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "بحث جيميناي الذكي",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF7C4DFF).copy(alpha = 0.25f))
                                        .border(1.dp, Color(0xFFB388FF).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AI",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFB388FF)
                                    )
                                }
                            }
                            Text(
                                text = "البحث والاستكشاف بالذكاء الاصطناعي أثناء الاتصال",
                                fontSize = 12.sp,
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFF90A4AE)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Network & Model Status Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isOnline) FocusEmerald.copy(alpha = 0.12f)
                            else FocusAmber.copy(alpha = 0.12f)
                        )
                        .border(
                            1.dp,
                            if (isOnline) FocusEmerald.copy(alpha = 0.35f)
                            else FocusAmber.copy(alpha = 0.35f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (isOnline) FocusEmerald else FocusAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOnline) "متصل بالإنترنت • Gemini 3.8 Flash متاح مباشرة"
                            else "غير متصل بالإنترنت • يعمل المحرك العصبي المحلي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isOnline) FocusEmerald else FocusAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Model Selector: Flash vs Paid Argon Pro Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isPaidMode) FocusBluePrimary else Color(0xFF263238))
                            .border(1.dp, if (!isPaidMode) FocusBlueSecondary else Color(0xFF37474F), RoundedCornerShape(10.dp))
                            .clickable {
                                isPaidMode = false
                                selectedModel = "gemini-3.8-flash"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ Flash 3.8 (سريع)",
                            fontSize = 11.sp,
                            fontWeight = if (!isPaidMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isPaidMode) Color.White else Color(0xFFB0BEC5)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isPaidMode) Brush.horizontalGradient(listOf(Color(0xFF7C4DFF), Color(0xFFD97706)))
                                else Brush.linearGradient(listOf(Color(0xFF263238), Color(0xFF263238)))
                            )
                            .border(
                                1.dp,
                                if (isPaidMode) FocusAmber else Color(0xFF37474F),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                isPaidMode = true
                                selectedModel = "gemini-4-argon"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💎 Gemini 4 Argon (مدفوع Pro)",
                            fontSize = 11.sp,
                            fontWeight = if (isPaidMode) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isPaidMode) Color.White else Color(0xFFB0BEC5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Input with Action Button
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            "اكتب أي شيء: مثل مدارس أكودة، معاهد هرقلة، كليات سوسة...",
                            fontSize = 13.sp,
                            color = Color(0xFF78909C)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = FocusBluePrimary
                        )
                    },
                    trailingIcon = {
                        Button(
                            onClick = { executeSearch(query) },
                            enabled = query.isNotBlank() && !isLoading,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FocusBluePrimary
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text("بحث AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusBluePrimary,
                        unfocusedBorderColor = FocusBlueSecondary.copy(alpha = 0.4f),
                        focusedContainerColor = FocusDarkSurfaceVariant,
                        unfocusedContainerColor = FocusDarkSurfaceVariant,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick query suggestion chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickQueries.forEach { chipQuery ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(FocusDarkSurfaceVariant)
                                .border(1.dp, Color(0xFF37474F), RoundedCornerShape(16.dp))
                                .clickable { executeSearch(chipQuery.replace(Regex("[^\u0600-\u06FF ]"), "").trim()) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chipQuery,
                                fontSize = 11.sp,
                                color = Color(0xFFCFD8DC)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Results or Loading state
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = FocusBluePrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جاري الاتصال والبحث بواسطة Gemini AI...",
                                fontSize = 13.sp,
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }
                } else if (searchResult != null) {
                    val res = searchResult!!

                    // AI Explanation Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF263238))
                            .border(1.dp, Color(0xFF455A64), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFFB388FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = res.aiExplanationAr,
                                    fontSize = 12.sp,
                                    color = Color(0xFFECEFF1),
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "تم العثور على ${res.institutions.size} مؤسسة تعليمية",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FocusEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(res.institutions) { inst ->
                            val alreadyInRepo = repo.institutions.value.any {
                                it.id == inst.id || it.nameAr == inst.nameAr
                            }
                            val isAdded = alreadyInRepo || addedIds.contains(inst.id)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = FocusDarkSurfaceVariant
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isAdded) FocusEmerald.copy(alpha = 0.5f) else Color(0xFF37474F)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = inst.nameAr,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            if (inst.nameEn.isNotBlank()) {
                                                Text(
                                                    text = inst.nameEn,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF90A4AE)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(FocusBluePrimary.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = inst.type.labelAr,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FocusBluePrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Location breadcrumb
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = FocusAmber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${inst.country} • ولاية ${inst.state} • معتمدية ${inst.delegation}",
                                            fontSize = 12.sp,
                                            color = Color(0xFFCFD8DC)
                                        )
                                    }

                                    if (inst.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = inst.description,
                                            fontSize = 11.sp,
                                            color = Color(0xFF90A4AE),
                                            lineHeight = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                onNavigateToInstitution(inst)
                                                onDismiss()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                                horizontal = 10.dp,
                                                vertical = 6.dp
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = FocusBluePrimary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("عرض بالخريطة", fontSize = 11.sp)
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                if (!isAdded) {
                                                    repo.addInstitution(inst)
                                                    addedIds = addedIds + inst.id
                                                    onInstitutionAdded(inst)
                                                }
                                            },
                                            enabled = !isAdded,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isAdded) FocusEmerald else FocusBluePrimary,
                                                disabledContainerColor = FocusEmerald.copy(alpha = 0.7f),
                                                disabledContentColor = Color.White
                                            ),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            )
                                        ) {
                                            Icon(
                                                imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isAdded) "مضافة بالقائمة ✓" else "إضافة لقائمتي",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Empty state encouragement
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color(0xFF455A64),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ابحث عن أي مؤسسة بالذكاء الاصطناعي",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF90A4AE)
                            )
                            Text(
                                text = "جرّب كتابة اسم معتمديتك كـ 'أكودة' أو 'هرقلة' أو نوع المؤسسة",
                                fontSize = 12.sp,
                                color = Color(0xFF607D8B)
                            )
                        }
                    }
                }
            }
        }
    }
}
