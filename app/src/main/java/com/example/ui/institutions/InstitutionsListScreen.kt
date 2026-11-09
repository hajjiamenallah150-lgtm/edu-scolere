package com.example.ui.institutions

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InstitutionRepository
import com.example.model.EducationalInstitution
import com.example.model.InstitutionType
import com.example.service.FocusManager
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusBluePrimary
import com.example.ui.theme.FocusBlueSecondary
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusRedAlert
import com.example.util.MatchResult

@Composable
fun InstitutionsListScreen(
    onNavigateToMapWithInstitution: (EducationalInstitution) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { InstitutionRepository.getInstance() }
    val focusManager = remember { FocusManager.getInstance() }

    val institutions by repository.institutions.collectAsState()
    val focusState by focusManager.focusState.collectAsState()

    // Hierarchical search states
    var searchCountry by remember { mutableStateOf("") }
    var searchState by remember { mutableStateOf("") }
    var searchDelegation by remember { mutableStateOf("") }
    var searchApproxName by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<InstitutionType?>(null) }

    // Dialog state for add or edit
    var showDialog by remember { mutableStateOf(false) }
    var showGeminiDialog by remember { mutableStateOf(false) }
    var institutionToEdit by remember { mutableStateOf<EducationalInstitution?>(null) }
    var institutionToDelete by remember { mutableStateOf<EducationalInstitution?>(null) }

    val availableCountries = remember(institutions) { repository.getAvailableCountries() }
    val availableStates = remember(institutions, searchCountry) { repository.getStatesForCountry(searchCountry) }
    val availableDelegations = remember(institutions, searchCountry, searchState) {
        repository.getDelegationsForState(searchCountry, searchState)
    }

    // Evaluate search results with fuzzy matching and Arabic normalization
    val searchResults = remember(institutions, searchCountry, searchState, searchDelegation, searchApproxName, selectedTypeFilter) {
        val rawResults = repository.search(
            country = searchCountry,
            state = searchState,
            delegation = searchDelegation,
            queryName = searchApproxName
        )
        if (selectedTypeFilter != null) {
            rawResults.filter { it.first.type == selectedTypeFilter }
        } else {
            rawResults
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    institutionToEdit = null
                    showDialog = true
                },
                modifier = Modifier.testTag("fab_add_institution"),
                containerColor = FocusEmerald,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة مؤسسة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Hierarchical Search Header (الدولة، الولاية، المعتمدية، اسم المؤسسة)
            HierarchicalSearchHeader(
                country = searchCountry,
                onCountryChange = { searchCountry = it },
                state = searchState,
                onStateChange = { searchState = it },
                delegation = searchDelegation,
                onDelegationChange = { searchDelegation = it },
                queryName = searchApproxName,
                onQueryNameChange = { searchApproxName = it },
                availableCountries = availableCountries,
                availableStates = availableStates,
                availableDelegations = availableDelegations,
                onResetFilters = {
                    searchCountry = ""
                    searchState = ""
                    searchDelegation = ""
                    searchApproxName = ""
                    selectedTypeFilter = null
                },
                onOpenGeminiSearch = {
                    showGeminiDialog = true
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Institution type filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("الكل (${institutions.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FocusBluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
                InstitutionType.entries.forEach { type ->
                    val count = institutions.count { it.type == type }
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == type) null else type
                        },
                        label = { Text("${type.labelAr} ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FocusBlueSecondary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Count and status summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نتائج البحث (${searchResults.size})",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "قفل الهاتف التلقائي مفعّل",
                    color = FocusEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد مؤسسة تعليمية مطابقة لمعايير البحث",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "جرب تخفيف الكلمات أو البحث باسم تقريبي، أو انقر على 'إضافة مؤسسة'",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults, key = { it.first.id }) { (inst, matchResult) ->
                        EnhancedInstitutionItemCard(
                            institution = inst,
                            matchResult = matchResult,
                            userLat = focusState.userLatitude,
                            userLon = focusState.userLongitude,
                            isCurrentlyInside = focusState.isInsideZone && focusState.currentInstitution?.id == inst.id,
                            onEdit = {
                                institutionToEdit = inst
                                showDialog = true
                            },
                            onDelete = {
                                institutionToDelete = inst
                            },
                            onViewOnMap = {
                                onNavigateToMapWithInstitution(inst)
                            },
                            onSimulateEnter = {
                                focusManager.simulateEnterInstitution(inst.id, context)
                            },
                            onToggleAutoLock = { enabled ->
                                repository.updateInstitution(inst.copy(autoLockEnabled = enabled))
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showDialog) {
        InstitutionEditDialog(
            institution = institutionToEdit,
            currentUserLat = focusState.userLatitude,
            currentUserLon = focusState.userLongitude,
            onDismiss = { showDialog = false },
            onSave = { saved ->
                if (institutionToEdit != null) {
                    repository.updateInstitution(saved)
                } else {
                    repository.addInstitution(saved)
                }
                showDialog = false
            }
        )
    }

    // Gemini AI Online Search Dialog
    if (showGeminiDialog) {
        GeminiSearchDialog(
            initialQuery = searchApproxName.ifBlank { searchDelegation },
            onDismiss = { showGeminiDialog = false },
            onInstitutionAdded = { added ->
                // Institution added to repository
            },
            onNavigateToInstitution = { inst ->
                showGeminiDialog = false
                onNavigateToMapWithInstitution(inst)
            }
        )
    }

    // Delete Confirmation Dialog
    institutionToDelete?.let { inst ->
        AlertDialog(
            onDismissRequest = { institutionToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = FocusRedAlert)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حذف المؤسسة التعليمية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف «${inst.nameAr}» من قائمة المراقبة؟ لن يتم قفل الهاتف تلقائياً عند التواجد بها بعد الحذف.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deleteInstitution(inst.id)
                        institutionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusRedAlert),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                TextButton(onClick = { institutionToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun EnhancedInstitutionItemCard(
    institution: EducationalInstitution,
    matchResult: MatchResult,
    userLat: Double,
    userLon: Double,
    isCurrentlyInside: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewOnMap: () -> Unit,
    onSimulateEnter: () -> Unit,
    onToggleAutoLock: (Boolean) -> Unit
) {
    val dist = InstitutionRepository.calculateDistanceMeters(
        userLat, userLon, institution.latitude, institution.longitude
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("institution_card_${institution.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyInside) Color(0xFF26122C) else Color(0xFF1E293B)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isCurrentlyInside) FocusRedAlert.copy(alpha = 0.6f) else Color(0xFF334155)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Match Result & Translation Tag if applicable
            if (matchResult.translationHint.isNotBlank() || matchResult.matchedField.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (matchResult.translationHint.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = FocusEmerald, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(matchResult.translationHint, color = FocusEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (matchResult.matchedField.isNotBlank()) {
                        Text(matchResult.matchedField, color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
            }

            // Top Row: Icon, Title, and Auto-Lock switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (institution.type) {
                                    InstitutionType.UNIVERSITY -> FocusBluePrimary.copy(alpha = 0.25f)
                                    InstitutionType.HIGH_SCHOOL -> FocusEmerald.copy(alpha = 0.25f)
                                    InstitutionType.LIBRARY -> FocusAmber.copy(alpha = 0.25f)
                                    else -> FocusBlueSecondary.copy(alpha = 0.25f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (institution.type) {
                                InstitutionType.UNIVERSITY -> Icons.Default.School
                                InstitutionType.LIBRARY -> Icons.Default.MenuBook
                                else -> Icons.Default.School
                            },
                            contentDescription = null,
                            tint = when (institution.type) {
                                InstitutionType.UNIVERSITY -> FocusBlueSecondary
                                InstitutionType.HIGH_SCHOOL -> FocusEmerald
                                InstitutionType.LIBRARY -> FocusAmber
                                else -> FocusBlueSecondary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = institution.nameAr,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${institution.country} • ولاية ${institution.state}" +
                                        (if (institution.delegation.isNotBlank()) " (معتمدية ${institution.delegation})" else ""),
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Switch(
                    checked = institution.autoLockEnabled,
                    onCheckedChange = onToggleAutoLock,
                    colors = SwitchDefaults.colors(checkedTrackColor = FocusEmerald),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location coordinates & Radius info badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = FocusRedAlert,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.3f, %.3f".format(institution.latitude, institution.longitude),
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "نطاق القفل: ${institution.radiusMeters.toInt()}م",
                    color = FocusAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (isCurrentlyInside) "أنت بداخلها!" else "${dist.toInt()}م",
                    color = if (isCurrentlyInside) FocusRedAlert else FocusEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simulate enter button
                Button(
                    onClick = onSimulateEnter,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentlyInside) FocusRedAlert else FocusBluePrimary
                    ),
                    modifier = Modifier.testTag("simulate_card_${institution.id}")
                ) {
                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isCurrentlyInside) "داخل الحرم (الهاتف مقفل)" else "محاكاة التواجد", fontSize = 11.sp)
                }

                Row {
                    // View on Map
                    IconButton(onClick = onViewOnMap) {
                        Icon(Icons.Default.Map, contentDescription = "عرض بالخريطة", tint = FocusBlueSecondary, modifier = Modifier.size(20.dp))
                    }

                    // Edit
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFFCBD5E1), modifier = Modifier.size(20.dp))
                    }

                    // Delete
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
