package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.example.ui.theme.FocusRedAlert
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.ArabicSearchEngine
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusBluePrimary
import com.example.ui.theme.FocusBlueSecondary
import com.example.ui.theme.FocusEmerald

@Composable
fun HierarchicalSearchHeader(
    country: String,
    onCountryChange: (String) -> Unit,
    state: String,
    onStateChange: (String) -> Unit,
    delegation: String,
    onDelegationChange: (String) -> Unit,
    queryName: String,
    onQueryNameChange: (String) -> Unit,
    availableCountries: List<String>,
    availableStates: List<String>,
    availableDelegations: List<String>,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAdvancedExpanded by remember { mutableStateOf(false) }

    // Live translation calculation
    val nameTranslation = remember(queryName) {
        if (queryName.isNotBlank()) ArabicSearchEngine.translateAndTransliterate(queryName) else null
    }

    val hasActiveFilters = country.isNotBlank() || state.isNotBlank() || delegation.isNotBlank() || queryName.isNotBlank()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAdvancedExpanded || hasActiveFilters) FocusBlueSecondary.copy(alpha = 0.5f) else Color(0xFF334155)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main Top Bar (Approximate Name + Toggle button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = FocusBlueSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = queryName,
                    onValueChange = onQueryNameChange,
                    placeholder = {
                        Text(
                            text = "ابحث باسم قريب للمؤسسة (بالعربية أو ترجمتها)...",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_institution_name_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                if (queryName.isNotEmpty()) {
                    IconButton(onClick = { onQueryNameChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "مسح",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Advanced Search Toggle Button (الدولة، الولاية، المعتمدية)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAdvancedExpanded) FocusBluePrimary else Color(0xFF0F172A))
                        .clickable { isAdvancedExpanded = !isAdvancedExpanded }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("toggle_advanced_search_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (isAdvancedExpanded) Color.White else FocusBlueSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAdvancedExpanded) "إخفاء" else "تصفية جغرافية",
                            color = if (isAdvancedExpanded) Color.White else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Live Translation & Transliteration Hint Card
            if (nameTranslation != null && (nameTranslation.translatedKeywords.isNotEmpty() || nameTranslation.transliteration.isNotBlank())) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0B1426))
                        .border(1.dp, FocusEmerald.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = FocusEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row {
                                Text(
                                    text = "التعرف الذكي والترجمة: ",
                                    color = FocusEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = nameTranslation.translatedKeywords.take(3).joinToString(" • ").ifEmpty { nameTranslation.transliteration },
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Expandable Advanced Geographic Filter Fields (الدولة، الولاية، المعتمدية)
            AnimatedVisibility(
                visible = isAdvancedExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "البحث الجغرافي والإداري (الدولة ⬅️ الولاية ⬅️ المعتمدية)",
                        color = FocusAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. الدولة (Country)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = FocusBlueSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الدولة (Country):", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = country,
                            onValueChange = onCountryChange,
                            placeholder = { Text("مثال: تونس، السعودية، مصر...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_country_input")
                        )

                        // Quick Country Suggestion Chips
                        if (availableCountries.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableCountries.forEach { c ->
                                    FilterChip(
                                        selected = country.equals(c, ignoreCase = true),
                                        onClick = { onCountryChange(if (country.equals(c, ignoreCase = true)) "" else c) },
                                        label = { Text(c, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = FocusBluePrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. الولاية / المحافظة (State) & 3. المعتمدية (Delegation)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // الولاية
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = FocusEmerald, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الولاية / المحافظة:", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = state,
                                onValueChange = onStateChange,
                                placeholder = { Text("تونس، أريانة، صفاقس...", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_state_input")
                            )
                        }

                        // المعتمدية
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = FocusAmber, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("المعتمدية / الدائرة:", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = delegation,
                                onValueChange = onDelegationChange,
                                placeholder = { Text("المنار، الشرقية، المرسى...", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("search_delegation_input")
                            )
                        }
                    }

                    // Quick state suggestions chips
                    if (availableStates.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableStates.take(6).forEach { st ->
                                FilterChip(
                                    selected = state.equals(st, ignoreCase = true),
                                    onClick = { onStateChange(if (state.equals(st, ignoreCase = true)) "" else st) },
                                    label = { Text(st, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FocusEmerald,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reset and summary row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نظام بحث صوتي وتقريبي يتعرف على الأخطاء الإملائية والترجمات",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            modifier = Modifier.weight(1f)
                        )

                        if (hasActiveFilters) {
                            TextButton(
                                onClick = onResetFilters,
                                modifier = Modifier.testTag("reset_search_filters_button")
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = FocusRedAlert)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إعادة ضبط", color = FocusRedAlert, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
