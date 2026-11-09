package com.example.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.School
import android.widget.Toast
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduFocusMapView(
    modifier: Modifier = Modifier,
    onInstitutionSelected: ((EducationalInstitution) -> Unit)? = null
) {
    val context = LocalContext.current
    val repository = remember { InstitutionRepository.getInstance() }
    val focusManager = remember { FocusManager.getInstance() }

    val institutions by repository.institutions.collectAsState()
    val focusState by focusManager.focusState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<InstitutionType?>(null) }
    var selectedInstitution by remember { mutableStateOf<EducationalInstitution?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Map Pan and Zoom states
    // Reference center in Akouda, Sousse (Collège Djamel Abdeljelil: 35.8679, 10.5645)
    var centerLat by remember { mutableFloatStateOf(35.8679f) }
    var centerLon by remember { mutableFloatStateOf(10.5645f) }
    var zoomLevel by remember { mutableFloatStateOf(8000f) } // Pixels per degree

    // Online Internet Map Mode
    var isOnlineMapMode by remember { mutableStateOf(false) }
    var isSatelliteMode by remember { mutableStateOf(false) }

    // Pulse animation for GPS beacon
    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar"
    )

    // Filter institutions using Arabic search engine & normalization
    val filteredInstitutions = remember(institutions, searchQuery, selectedTypeFilter) {
        val matches = if (searchQuery.isBlank()) {
            institutions
        } else {
            repository.search(queryName = searchQuery).map { it.first }
        }
        if (selectedTypeFilter != null) {
            matches.filter { it.type == selectedTypeFilter }
        } else {
            matches
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f

        // Helper function to map lat/lon to Canvas X/Y
        fun coordsToPoint(lat: Double, lon: Double): Offset {
            val dLon = (lon - centerLon.toDouble()).toFloat()
            val dLat = (centerLat.toDouble() - lat).toFloat()
            val x = centerX + dLon * zoomLevel
            val y = centerY + dLat * zoomLevel
            return Offset(x, y)
        }

        fun radiusToPixels(radiusMeters: Float): Float {
            // Approx 111,000 meters per degree latitude
            val degrees = radiusMeters / 111000f
            return (degrees * zoomLevel).coerceAtLeast(18f)
        }

        // Offline Vector Map Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomLevel = (zoomLevel * zoom).coerceIn(1200f, 15000f)
                        val dLon = pan.x / zoomLevel
                        val dLat = pan.y / zoomLevel
                        centerLon -= dLon
                        centerLat += dLat
                    }
                }
                .testTag("interactive_campus_map_canvas")
        ) {
            // Map Base Color (Deep dark slate for high contrast and modern styling)
            drawRect(Color(0xFF0F172A))

            // Grid pattern representing offline map tile coordinates
            val gridStep = 80.dp.toPx()
            val offsetX = (centerX % gridStep)
            val offsetY = (centerY % gridStep)

            var x = offsetX - gridStep
            while (x < size.width + gridStep) {
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += gridStep
            }

            var y = offsetY - gridStep
            while (y < size.height + gridStep) {
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Decorative Campus Highway / Main Arteries
            val roadPath1 = Path().apply {
                moveTo(0f, size.height * 0.35f)
                cubicTo(
                    size.width * 0.3f, size.height * 0.45f,
                    size.width * 0.7f, size.height * 0.25f,
                    size.width, size.height * 0.4f
                )
            }
            drawPath(roadPath1, Color(0xFF1E3A5F), style = Stroke(width = 8f))

            val roadPath2 = Path().apply {
                moveTo(size.width * 0.4f, 0f)
                cubicTo(
                    size.width * 0.45f, size.height * 0.4f,
                    size.width * 0.55f, size.height * 0.6f,
                    size.width * 0.6f, size.height
                )
            }
            drawPath(roadPath2, Color(0xFF1E3A5F), style = Stroke(width = 6f))

            // Draw Geofencing Perimeters for each educational institution
            for (inst in filteredInstitutions) {
                val pt = coordsToPoint(inst.latitude, inst.longitude)
                val radPx = radiusToPixels(inst.radiusMeters)
                val isSelected = selectedInstitution?.id == inst.id
                val isUserInside = focusState.isInsideZone && focusState.currentInstitution?.id == inst.id

                val zoneColor = when {
                    isUserInside -> FocusRedAlert
                    isSelected -> FocusAmber
                    inst.type == InstitutionType.UNIVERSITY -> FocusBlueSecondary
                    inst.type == InstitutionType.HIGH_SCHOOL -> FocusEmerald
                    else -> FocusAmber
                }

                // Semi-transparent Geofence Circle
                drawCircle(
                    color = zoneColor.copy(alpha = if (isUserInside) 0.25f else 0.12f),
                    radius = radPx,
                    center = pt
                )

                // Perimeter dashed outline
                drawCircle(
                    color = zoneColor.copy(alpha = if (isSelected || isUserInside) 0.9f else 0.4f),
                    radius = radPx,
                    center = pt,
                    style = Stroke(
                        width = if (isSelected || isUserInside) 3f else 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )

                // Center Pin Base
                drawCircle(
                    color = zoneColor,
                    radius = if (isSelected) 14f else 10f,
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 6f else 4f,
                    center = pt
                )

                // Selected highlight aura
                if (isSelected) {
                    drawCircle(
                        color = FocusAmber.copy(alpha = 0.4f),
                        radius = 24f,
                        center = pt,
                        style = Stroke(width = 2f)
                    )
                }
            }

            // Draw Student/User Current Location Pin
            val userPt = coordsToPoint(focusState.userLatitude, focusState.userLongitude)
            val isInsideAny = focusState.isInsideZone

            // Radar beacon waves
            drawCircle(
                color = (if (isInsideAny) FocusRedAlert else FocusEmerald).copy(alpha = (1.6f - radarPulse).coerceIn(0f, 0.4f)),
                radius = 22f * radarPulse,
                center = userPt
            )

            // Inner location ring
            drawCircle(
                color = Color.White,
                radius = 11f,
                center = userPt
            )
            drawCircle(
                color = if (isInsideAny) FocusRedAlert else FocusEmerald,
                radius = 8f,
                center = userPt
            )
        }

        // Top Search & Filter Bar Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
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
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "ابحث عن مدرسة أو جامعة أو كلية...",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_institution_input"),
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "مسح",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips Row
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
        }

        // Top Status Badge: "Preloaded Offline Map"
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 116.dp, end = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isOnlineMapMode) FocusBluePrimary.copy(alpha = 0.25f)
                    else Color(0xFF0F172A).copy(alpha = 0.9f)
                )
                .border(
                    1.dp,
                    if (isOnlineMapMode) FocusBlueSecondary else Color(0xFF334155),
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    isOnlineMapMode = !isOnlineMapMode
                    Toast.makeText(
                        context,
                        if (isOnlineMapMode) "تم تفعيل خريطة الإنترنت عبر الشبكة 🌐" else "تم تفعيل الخريطة المحملة مسبقاً (دون إنترنت) 📴",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("toggle_online_map_badge")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isOnlineMapMode) Icons.Default.Public else Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = if (isOnlineMapMode) Color(0xFF38BDF8) else FocusEmerald,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOnlineMapMode) "خريطة الإنترنت نشطة 🌐" else "خريطة محملة دون إنترنت 📴",
                    color = if (isOnlineMapMode) Color.White else Color(0xFFCBD5E1),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Map Control Floating Buttons (Zoom in, Zoom out, My School, Recenter, Online Toggle, Add)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // My School Shortcut (Collège Djamel Abdeljelil Akouda: 35.8679, 10.5645)
            FloatingActionButton(
                onClick = {
                    centerLat = 35.8679f
                    centerLon = 10.5645f
                    zoomLevel = 9500f
                    val jamelSchool = repository.institutions.value.find {
                        it.id == "college_jamel_abdeljelil_akouda" || it.nameAr.contains("جمال عبد الجليل")
                    }
                    if (jamelSchool != null) {
                        selectedInstitution = jamelSchool
                    }
                    Toast.makeText(context, "تم الانتقال إلى إعدادية جمال عبد الجليل بأكودة ⭐", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("go_to_my_school_button"),
                containerColor = FocusAmber,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Star, contentDescription = "مدرستي: جمال عبد الجليل أكودة")
            }

            // Online / Offline Map Mode Toggle Button
            FloatingActionButton(
                onClick = {
                    isOnlineMapMode = !isOnlineMapMode
                    Toast.makeText(
                        context,
                        if (isOnlineMapMode) "خريطة الإنترنت نشطة 🌐" else "الخريطة المحملة مسبقاً 📴",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("online_map_fab"),
                containerColor = if (isOnlineMapMode) Color(0xFF0284C7) else Color(0xFF1E293B),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = if (isOnlineMapMode) Icons.Default.Public else Icons.Default.CloudDone,
                    contentDescription = "تبديل خريطة الإنترنت"
                )
            }

            FloatingActionButton(
                onClick = { zoomLevel = (zoomLevel * 1.35f).coerceAtMost(15000f) },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("zoom_in_button"),
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "تكبير")
            }

            FloatingActionButton(
                onClick = { zoomLevel = (zoomLevel / 1.35f).coerceAtLeast(1200f) },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("zoom_out_button"),
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "تصغير")
            }

            FloatingActionButton(
                onClick = {
                    centerLat = focusState.userLatitude.toFloat()
                    centerLon = focusState.userLongitude.toFloat()
                },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("recenter_button"),
                containerColor = FocusBluePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "موقعي")
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("add_institution_button"),
                containerColor = FocusEmerald,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = "إضافة مؤسسة")
            }
        }

        // Bottom Selected Institution Details Card
        AnimatedVisibility(
            visible = selectedInstitution != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            selectedInstitution?.let { inst ->
                val dist = InstitutionRepository.calculateDistanceMeters(
                    focusState.userLatitude,
                    focusState.userLongitude,
                    inst.latitude,
                    inst.longitude
                )
                val isInsideThis = dist <= inst.radiusMeters

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FocusBluePrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (inst.type) {
                                            InstitutionType.UNIVERSITY -> Icons.Default.School
                                            InstitutionType.LIBRARY -> Icons.Default.MenuBook
                                            else -> Icons.Default.School
                                        },
                                        contentDescription = null,
                                        tint = FocusBlueSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = inst.nameAr,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${inst.type.labelAr} • ${inst.city}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            IconButton(onClick = { selectedInstitution = null }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF94A3B8))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status info pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isInsideThis) "📍 أنت حالياً داخل نطاق الحرم!" else "تبعد عنها: ${dist.toInt()} متر",
                                color = if (isInsideThis) FocusEmerald else Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "نصف القطر: ${inst.radiusMeters.toInt()}م",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Simulate Walk/Entry + Radius adjustment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    focusManager.simulateEnterInstitution(inst.id, context)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("simulate_enter_institution_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isInsideThis) FocusEmerald else FocusBluePrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsWalk,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isInsideThis) "داخل النطاق" else "محاكاة الدخول وقفل الهاتف",
                                    fontSize = 11.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    // Focus map center on this institution
                                    centerLat = inst.latitude.toFloat()
                                    centerLon = inst.longitude.toFloat()
                                    zoomLevel = 6000f
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Institution Quick Selector horizontal strip if none selected
        if (selectedInstitution == null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
            ) {
                Text(
                    text = "المؤسسات التعليمية القريبة (اضغط للمعاينة والتجربة):",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredInstitutions.take(6).forEach { inst ->
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable {
                                    selectedInstitution = inst
                                    centerLat = inst.latitude.toFloat()
                                    centerLon = inst.longitude.toFloat()
                                    onInstitutionSelected?.invoke(inst)
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E293B).copy(alpha = 0.95f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = inst.nameAr,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = inst.city,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                    val d = InstitutionRepository.calculateDistanceMeters(
                                        focusState.userLatitude,
                                        focusState.userLongitude,
                                        inst.latitude,
                                        inst.longitude
                                    )
                                    Text(
                                        text = "${d.toInt()}م",
                                        color = FocusEmerald,
                                        fontSize = 10.sp,
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

    // Add New Institution Dialog
    if (showAddDialog) {
        var nameAr by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("الرياض") }
        var type by remember { mutableStateOf(InstitutionType.HIGH_SCHOOL) }
        var radius by remember { mutableFloatStateOf(250f) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = FocusEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إضافة مؤسسة تعليمية جديدة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "حدد اسم المؤسسة ونصف قطر الحظر لمنع استخدام الهاتف داخلها.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text("اسم المدرسة أو الجامعة") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_name_field")
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("المدينة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "نطاق حظر الهاتف: ${radius.toInt()} متر",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FocusAmber
                    )
                    Slider(
                        value = radius,
                        onValueChange = { radius = it },
                        valueRange = 100f..1000f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = FocusAmber,
                            activeTrackColor = FocusAmber
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameAr.isNotBlank()) {
                            val newInst = EducationalInstitution(
                                id = "custom_${System.currentTimeMillis()}",
                                nameAr = nameAr,
                                nameEn = nameAr,
                                city = city,
                                type = type,
                                latitude = focusState.userLatitude,
                                longitude = focusState.userLongitude,
                                radiusMeters = radius,
                                isPreloaded = false
                            )
                            repository.addInstitution(newInst)
                            selectedInstitution = newInst
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusEmerald),
                    modifier = Modifier.testTag("save_new_institution_button")
                ) {
                    Text("حفظ وتفعيل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
