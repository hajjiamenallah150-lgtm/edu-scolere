package com.example.ui.dashboard

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InstitutionRepository
import com.example.service.FocusManager
import com.example.ui.lock.FocusLockActivity
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusBluePrimary
import com.example.ui.theme.FocusBlueSecondary
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusRedAlert

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.example.auth.AuthManager
import com.example.ui.institutions.GeminiSearchDialog

@Composable
fun DashboardScreen(
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val focusManager = remember { FocusManager.getInstance() }
    val repository = remember { InstitutionRepository.getInstance() }
    val authManager = remember { AuthManager.getInstance(context) }

    val focusState by focusManager.focusState.collectAsState()
    val userSettings by focusManager.userSettings.collectAsState()
    val institutions by repository.institutions.collectAsState()
    val currentUser by authManager.currentUser.collectAsState()
    val isAuthLoading by authManager.isLoading.collectAsState()

    var showGeminiDialog by remember { mutableStateOf(false) }

    val isAdminActive = focusManager.isDeviceAdminActive(context)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Google Account Linking Status Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_account_status_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentUser != null) Color(0xFF132238) else Color(0xFF1E293B)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (currentUser != null) FocusBluePrimary.copy(alpha = 0.5f) else Color(0xFF334155)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (currentUser != null) FocusBluePrimary.copy(alpha = 0.2f) else Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (currentUser != null) FocusBluePrimary else Color(0xFF94A3B8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.displayName ?: "ربط الحساب عبر Google",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (currentUser != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = FocusEmerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = currentUser?.email ?: "سجل بحسابك لمزامنة مؤسساتك وسجل التركيز سحابياً",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    if (currentUser == null) {
                        Button(
                            onClick = {
                                if (activity != null) {
                                    authManager.signInWithGoogle(activity, scope)
                                }
                            },
                            enabled = !isAuthLoading,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FocusBluePrimary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            )
                        ) {
                            Text("ربط Google", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FocusEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("مرتبط ✓", color = FocusEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))

            // Main Status Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("protection_status_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (focusState.isInsideZone) Color(0xFF1E1022) else Color(0xFF0F1E36)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (focusState.isInsideZone) FocusRedAlert.copy(alpha = 0.6f) else FocusBlueSecondary.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (focusState.isInsideZone) FocusRedAlert.copy(alpha = 0.2f)
                                        else FocusEmerald.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (focusState.isInsideZone) Icons.Default.Lock else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (focusState.isInsideZone) FocusRedAlert else FocusEmerald,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (focusState.isInsideZone) "الهاتف مقفل للدراسة" else "حارس المؤسسات التعليمية نشط",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (focusState.isInsideZone) "أنت داخل نطاق الحرم التعليمي" else "يراقب الموقع عبر الخريطة المحملة",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = focusState.isProtectionActive,
                            onCheckedChange = { focusManager.toggleProtection(context) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FocusEmerald
                            ),
                            modifier = Modifier.testTag("protection_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (focusState.isInsideZone) {
                        // Inside Zone Details
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF2D1533))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = focusState.currentInstitution?.nameAr ?: "المؤسسة التعليمية",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeOff,
                                            contentDescription = null,
                                            tint = FocusAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "صامت",
                                            color = FocusAmber,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (userSettings.hideTimeForStudent) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = FocusEmerald,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "الوقت محجوب عن التلميذ لضمان التركيز 🎯",
                                            color = FocusEmerald,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    val mins = focusState.sessionDurationSeconds / 60
                                    val secs = focusState.sessionDurationSeconds % 60
                                    Text(
                                        text = "وقت الجلسة الحالية: %02d:%02d دقيقة".format(mins, secs),
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(context, FocusLockActivity::class.java).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = FocusRedAlert)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("شاشة القفل", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { focusManager.simulateExitInstitution(context) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("محاكاة الخروج", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // Outside Zone Info
                        val nearest = focusState.nearestInstitution
                        val dist = focusState.distanceToNearestMeters.toInt()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF091322))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "أقرب مؤسسة تعليمية:",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "المسافة: $dist متر",
                                        color = FocusEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = nearest?.nameAr ?: "جامعة الملك سعود",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                val progress = ((1000f - dist.coerceIn(0, 1000)) / 1000f).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = FocusBlueSecondary,
                                    trackColor = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "عند الدخول للنطاق (${nearest?.radiusMeters?.toInt() ?: 250}م) سيتم إغلاق الهاتف فوراً",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // My Delegation Section (معتمديتي والمؤسسات التابعة لها)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("my_delegation_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131F33)),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, FocusBluePrimary.copy(alpha = 0.45f))
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FocusBluePrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = FocusBlueSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "معتمديتي: ${focusState.detectedDelegation}",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(FocusEmerald.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ولاية ${focusState.detectedState}",
                                            color = FocusEmerald,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "تم التحديد عبر GPS • ${focusState.institutionsInMyDelegation.size} مؤسسات بالمعتمدية",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Gemini AI Search Launcher
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFF7C4DFF), FocusBluePrimary))
                                )
                                .clickable { showGeminiDialog = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("dashboard_gemini_search_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("بحث جيميناي", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Delegation Switcher Chips
                    Text(
                        text = "تبديل المعتمدية أو الانتقال السريع:",
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
                        val delegationChips = listOf("أكودة", "هرقلة", "شط مريم", "حمام سوسة", "سوسة المدينة", "تونس العاصمة")
                        delegationChips.forEach { delName ->
                            val isSelected = focusState.detectedDelegation == delName
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) FocusBluePrimary else Color(0xFF1E293B))
                                    .border(
                                        1.dp,
                                        if (isSelected) FocusBlueSecondary else Color(0xFF334155),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        focusManager.switchDelegationManually(delName, context)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "📍 $delName",
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // List of institutions in this delegation
                    if (focusState.institutionsInMyDelegation.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            focusState.institutionsInMyDelegation.take(3).forEach { inst ->
                                val dist = InstitutionRepository.calculateDistanceMeters(
                                    focusState.userLatitude,
                                    focusState.userLongitude,
                                    inst.latitude,
                                    inst.longitude
                                ).toInt()
                                val isInside = dist <= inst.radiusMeters

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A263D)),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isInside) FocusRedAlert.copy(alpha = 0.6f) else Color(0xFF2E3E56)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = inst.nameAr,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(FocusBluePrimary.copy(alpha = 0.2f))
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(inst.type.labelAr, color = FocusBlueSecondary, fontSize = 10.sp)
                                                }
                                            }
                                            Text(
                                                text = if (isInside) "🔴 داخل الحرم - الهاتف مقفل تلقائياً"
                                                else "المسافة: $dist م • نطاق القفل: ${inst.radiusMeters}م",
                                                color = if (isInside) FocusRedAlert else Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                focusManager.simulateEnterInstitution(inst.id, context)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isInside) FocusRedAlert else FocusBluePrimary
                                            ),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                                horizontal = 10.dp,
                                                vertical = 4.dp
                                            )
                                        ) {
                                            Text(
                                                text = if (isInside) "داخل" else "دخول",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Focus hours
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = FocusEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${focusState.totalFocusMinutesToday} دقيقة",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "تركيز اليوم داخل الحرم",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }

                // Blocked distractions
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = FocusAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${focusState.savedDistractionsCount}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "مشتتات وإشعارات محجوبة",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }

                // Institutions count
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = FocusBlueSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${institutions.size}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "مؤسسة بالخريطة",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Live Simulator Section (CRITICAL FOR TESTING)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF152238))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = FocusEmerald,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "محاكي الدخول والخروج الفوري",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = "تجربة حية",
                            color = FocusEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "اضغط على أي مؤسسة لمحاكاة دخول الطالب إليها ومشاهدة قفل الهاتف وكتم الصوت فوراً، ثم جرب الخروج.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                focusManager.simulateEnterInstitution("tn_akouda_lycee", context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_enter_akouda_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FocusBluePrimary)
                        ) {
                            Text("معهد 2 مارس بأكودة", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                focusManager.simulateEnterInstitution("tn_hergla_lycee", context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_enter_hergla_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
                        ) {
                            Text("معهد هرقلة الثانوي", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                focusManager.simulateEnterInstitution("tn_chott_meriem_isa", context)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text("فلاحية شط مريم", fontSize = 11.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                focusManager.simulateExitInstitution(context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_exit_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FocusEmerald.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = FocusEmerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("محاكاة الخروج", fontSize = 11.sp, color = FocusEmerald)
                        }
                    }
                }
            }
        }

        // Device Admin Hardware Lock Setup Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = if (isAdminActive) FocusEmerald else FocusAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "قفل شاشة الهاتف الفعلي (Device Admin)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isAdminActive) "مفعلة - يطفئ شاشة النظام فعلياً" else "غير مفعلة - اضغط للتمكين",
                                    color = if (isAdminActive) FocusEmerald else FocusAmber,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (!isAdminActive) {
                            Button(
                                onClick = {
                                    if (activity != null) {
                                        focusManager.requestDeviceAdmin(activity)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FocusAmber),
                                modifier = Modifier.testTag("enable_device_admin_button")
                            ) {
                                Text("تفعيل", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "مفعل",
                                tint = FocusEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Map Discovery CTA Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMap() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FocusBluePrimary.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = FocusBlueSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "استكشاف الخريطة التعليمية",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "عرض وتعديل أنصاف أقطار المدارس والجامعات",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = FocusEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showGeminiDialog) {
        GeminiSearchDialog(
            initialQuery = focusState.detectedDelegation,
            onDismiss = { showGeminiDialog = false },
            onInstitutionAdded = {},
            onNavigateToInstitution = { onNavigateToMap() }
        )
    }
}

