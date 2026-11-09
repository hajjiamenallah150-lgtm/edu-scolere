package com.example.ui.institutions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.EducationalInstitution
import com.example.model.InstitutionType
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusBluePrimary
import com.example.ui.theme.FocusBlueSecondary
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusRedAlert

@Composable
fun InstitutionEditDialog(
    institution: EducationalInstitution?, // null for create, non-null for edit
    currentUserLat: Double,
    currentUserLon: Double,
    onDismiss: () -> Unit,
    onSave: (EducationalInstitution) -> Unit
) {
    val isEditing = institution != null

    var nameAr by remember { mutableStateOf(institution?.nameAr ?: "") }
    var nameEn by remember { mutableStateOf(institution?.nameEn ?: "") }
    var country by remember { mutableStateOf(institution?.country ?: "تونس") }
    var state by remember { mutableStateOf(institution?.state ?: "تونس") }
    var delegation by remember { mutableStateOf(institution?.delegation ?: "المنار") }
    var city by remember { mutableStateOf(institution?.city ?: "تونس") }
    var type by remember { mutableStateOf(institution?.type ?: InstitutionType.UNIVERSITY) }
    var latitude by remember { mutableDoubleStateOf(institution?.latitude ?: currentUserLat) }
    var longitude by remember { mutableDoubleStateOf(institution?.longitude ?: currentUserLon) }
    var radiusMeters by remember { mutableFloatStateOf(institution?.radiusMeters ?: 300f) }
    var autoLockEnabled by remember { mutableStateOf(institution?.autoLockEnabled ?: true) }
    var silentModeEnabled by remember { mutableStateOf(institution?.silentModeEnabled ?: true) }
    var description by remember { mutableStateOf(institution?.description ?: "") }

    var showMapPicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FocusEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.EditLocation else Icons.Default.AddLocation,
                                contentDescription = null,
                                tint = FocusEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEditing) "تعديل المؤسسة التعليمية" else "إضافة مؤسسة تعليمية جديدة",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name in Arabic
                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("اسم المؤسسة (بالعربية)") },
                    placeholder = { Text("مثال: جامعة تونس المنار أو معهد INSAT") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("institution_name_ar_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Name in English
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("اسم المؤسسة (بالإنجليزية أو الفرنسية)") },
                    placeholder = { Text("e.g. Tunis El Manar University") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Geographic Hierarchy Card (الدولة، الولاية، المعتمدية)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "التقسيم الجغرافي والإداري (لتسهيل البحث)",
                            color = FocusEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = country,
                                onValueChange = { country = it },
                                label = { Text("الدولة") },
                                placeholder = { Text("تونس، السعودية...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = state,
                                onValueChange = { state = it },
                                label = { Text("الولاية / المحافظة") },
                                placeholder = { Text("تونس، أريانة، صفاقس...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = delegation,
                                onValueChange = { delegation = it },
                                label = { Text("المعتمدية / الدائرة") },
                                placeholder = { Text("المنار، الشرقية، المرسى...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("المدينة") },
                                placeholder = { Text("تونس، سوسة...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Institution Type Selector
                Text(
                    text = "نوع المؤسسة:",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InstitutionType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.labelAr, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FocusBlueSecondary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Geolocation Section (Interactive map picker)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = FocusRedAlert, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("الموقع الجغرافي على الخريطة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Button(
                                onClick = { showMapPicker = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FocusBluePrimary),
                                modifier = Modifier.testTag("open_map_picker_button")
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تحديد على الخريطة", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = "%.4f".format(latitude),
                                onValueChange = {
                                    it.toDoubleOrNull()?.let { lat -> latitude = lat }
                                },
                                label = { Text("خط العرض Lat") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = "%.4f".format(longitude),
                                onValueChange = {
                                    it.toDoubleOrNull()?.let { lon -> longitude = lon }
                                },
                                label = { Text("خط الطول Lon") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                latitude = currentUserLat
                                longitude = currentUserLon
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = FocusEmerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استخدام موقعي الحالي", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Radius Setting
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "نصف قطر حظر الهاتف ودراسة الحرم:",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${radiusMeters.toInt()} متر",
                            color = FocusAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Slider(
                        value = radiusMeters,
                        onValueChange = { radiusMeters = it },
                        valueRange = 50f..1500f,
                        steps = 28,
                        colors = SliderDefaults.colors(
                            thumbColor = FocusAmber,
                            activeTrackColor = FocusAmber
                        ),
                        modifier = Modifier.testTag("radius_slider")
                    )
                    Text(
                        text = "عند دخول الطالب هذا النطاق، يتم قفل الهاتف وكتم الصوت تلقائياً.",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-lock toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("قفل الهاتف التلقائي", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("إغلاق الهاتف فور الدخول إلى الحرم", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoLockEnabled,
                        onCheckedChange = { autoLockEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = FocusEmerald)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Silent mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("كتم الصوت والإشعارات", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("وضع الهاتف في الحالة الصامتة أثناء الحصص", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = silentModeEnabled,
                        onCheckedChange = { silentModeEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = FocusEmerald)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons (Cancel / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text("إلغاء", color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (nameAr.isNotBlank()) {
                                val saved = EducationalInstitution(
                                    id = institution?.id ?: "inst_${System.currentTimeMillis()}",
                                    nameAr = nameAr.trim(),
                                    nameEn = if (nameEn.isNotBlank()) nameEn.trim() else nameAr.trim(),
                                    country = country.trim().ifEmpty { "تونس" },
                                    state = state.trim().ifEmpty { "تونس" },
                                    delegation = delegation.trim(),
                                    city = city.trim().ifEmpty { state.trim() },
                                    type = type,
                                    latitude = latitude,
                                    longitude = longitude,
                                    radiusMeters = radiusMeters,
                                    isPreloaded = institution?.isPreloaded ?: false,
                                    autoLockEnabled = autoLockEnabled,
                                    silentModeEnabled = silentModeEnabled,
                                    description = description
                                )
                                onSave(saved)
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_institution_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusEmerald),
                        enabled = nameAr.isNotBlank()
                    ) {
                        Text(if (isEditing) "حفظ التعديلات" else "إضافة المؤسسة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Full-screen interactive map location picker dialog
    if (showMapPicker) {
        Dialog(
            onDismissRequest = { showMapPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                var pickLat by remember { mutableDoubleStateOf(latitude) }
                var pickLon by remember { mutableDoubleStateOf(longitude) }
                var mapZoom by remember { mutableFloatStateOf(4000f) }

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("تحديد موقع المؤسسة التعليمية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("انقر على أي نقطة على الخريطة لتثبيت موقع الحرم", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        IconButton(onClick = { showMapPicker = false }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        val centerX = size.width / 2f
                                        val centerY = size.height / 2f
                                        val dX = offset.x - centerX
                                        val dY = offset.y - centerY
                                        pickLon += (dX / mapZoom)
                                        pickLat -= (dY / mapZoom)
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        mapZoom = (mapZoom * zoom).coerceIn(1500f, 15000f)
                                        val dLon = pan.x / mapZoom
                                        val dLat = pan.y / mapZoom
                                        pickLon -= dLon
                                        pickLat += dLat
                                    }
                                }
                        ) {
                            drawRect(Color(0xFF0A101D))

                            val step = 60.dp.toPx()
                            var x = 0f
                            while (x < size.width) {
                                drawLine(Color(0xFF1E293B), Offset(x, 0f), Offset(x, size.height), 1f)
                                x += step
                            }
                            var y = 0f
                            while (y < size.height) {
                                drawLine(Color(0xFF1E293B), Offset(0f, y), Offset(size.width, y), 1f)
                                y += step
                            }

                            val center = Offset(size.width / 2f, size.height / 2f)

                            val radPx = ((radiusMeters / 111000f) * mapZoom).coerceAtLeast(30f)
                            drawCircle(
                                color = FocusAmber.copy(alpha = 0.2f),
                                radius = radPx,
                                center = center
                            )
                            drawCircle(
                                color = FocusAmber,
                                radius = radPx,
                                center = center,
                                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f))
                            )

                            drawCircle(color = FocusRedAlert, radius = 14f, center = center)
                            drawCircle(color = Color.White, radius = 5f, center = center)
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B).copy(alpha = 0.9f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "الإحداثيات: %.5f, %.5f".format(pickLat, pickLon),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showMapPicker = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }
                        Button(
                            onClick = {
                                latitude = pickLat
                                longitude = pickLon
                                showMapPicker = false
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = FocusEmerald)
                        ) {
                            Text("تثبيت الموقع المحدد", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
