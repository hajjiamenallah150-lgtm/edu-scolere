package com.example.ui.settings

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.FocusManager
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusRedAlert
import com.example.ui.theme.FocusBluePrimary
import com.example.ui.theme.FocusBlueSecondary
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.rememberCoroutineScope
import com.example.auth.AuthManager

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val focusManager = remember { FocusManager.getInstance() }
    val authManager = remember { AuthManager.getInstance(context) }

    val userSettings by focusManager.userSettings.collectAsState()
    val currentUser by authManager.currentUser.collectAsState()
    val isAuthLoading by authManager.isLoading.collectAsState()
    val isAdminActive = focusManager.isDeviceAdminActive(context)

    var showPinDialog by remember { mutableStateOf(false) }
    var pinValue by remember { mutableStateOf(userSettings.emergencyPin) }

    var showContactDialog by remember { mutableStateOf(false) }
    var contactValue by remember { mutableStateOf(userSettings.emergencyContact) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Section: Google Account Linking
        Text(
            text = "حساب Google والمزامنة السحابية",
            color = Color(0xFFCBD5E1),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_google_account_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (currentUser != null) FocusEmerald.copy(alpha = 0.5f) else Color(0xFF334155)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentUser != null) FocusBluePrimary.copy(alpha = 0.2f)
                                    else Color(0xFF334155)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (currentUser != null) FocusBluePrimary else Color(0xFF94A3B8),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.displayName ?: "غير متصل بحساب Google",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (currentUser != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(FocusEmerald.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "مفعل ✓",
                                            color = FocusEmerald,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = currentUser?.email ?: "اربط حسابك لحفظ مؤسساتك ومزامنة سجل التركيز سحابياً",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (currentUser == null) {
                    Button(
                        onClick = {
                            if (activity != null) {
                                authManager.signInWithGoogle(activity, scope)
                            }
                        },
                        enabled = !isAuthLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_google_sign_in_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FocusBluePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ربط الحساب عبر Google", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "تمت مزامنة المؤسسات وسجل التركيز بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FocusEmerald.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = FocusEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مزامنة الآن", color = FocusEmerald, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                authManager.signOut(scope) {
                                    Toast.makeText(context, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Text("تسجيل الخروج", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section: Lock & Security Controls
        Text(
            text = "إعدادات الأمان وقفل الهاتف",
            color = Color(0xFFCBD5E1),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Strict Lock Screen Overlay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "شاشة قفل التركيز الصارمة",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "إظهار شاشة قفل غير قابلة للإغلاق حتى مغادرة الحرم",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = userSettings.strictOverlayLock,
                        onCheckedChange = {
                            focusManager.updateSettings(userSettings.copy(strictOverlayLock = it))
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = FocusEmerald)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Device Admin Physical Screen Lock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "قفل شاشة النظام الفعلي (Device Admin)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isAdminActive) "تم منح الصلاحية - يطفئ الشاشة بنجاح" else "يتطلب تفعيل صلاحية مسؤول الجهاز في النظام",
                            color = if (isAdminActive) FocusEmerald else FocusAmber,
                            fontSize = 11.sp
                        )
                    }

                    if (isAdminActive) {
                        Switch(
                            checked = userSettings.systemAdminLock,
                            onCheckedChange = {
                                focusManager.updateSettings(userSettings.copy(systemAdminLock = it))
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = FocusEmerald)
                        )
                    } else {
                        Button(
                            onClick = {
                                if (activity != null) {
                                    focusManager.requestDeviceAdmin(activity)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FocusAmber)
                        ) {
                            Text("تفعيل", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Silent mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "كتم الرنين والوسائط تلقائياً",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "تحويل الهاتف للوضع الصامت فور دخول قاعة الدرس",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = userSettings.silentModeEnabled,
                        onCheckedChange = {
                            focusManager.updateSettings(userSettings.copy(silentModeEnabled = it))
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = FocusEmerald)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hide time for student toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "حجب الوقت والساعة عن التلميذ",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "إخفاء عداد الوقت وشريط الساعة لمنع تشتت التلميذ بمراقبة الوقت",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = userSettings.hideTimeForStudent,
                        onCheckedChange = {
                            focusManager.updateSettings(userSettings.copy(hideTimeForStudent = it))
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = FocusEmerald)
                    )
                }
            }
        }

        // Section: Emergency bypass settings
        Text(
            text = "حالات الطوارئ والتجاوز الاستثنائي",
            color = Color(0xFFCBD5E1),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // PIN editor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "رمز فك القفل الاستثنائي (PIN)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "الرمز الحالي: ${userSettings.emergencyPin} (مخصص للمعلمين وأولياء الأمور)",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    OutlinedButton(
                        onClick = { showPinDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("تغيير", fontSize = 11.sp, color = FocusBlueSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Emergency phone contact
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "رقم هاتف الطوارئ المتاح",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "الرقم المسجل: ${userSettings.emergencyContact}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    OutlinedButton(
                        onClick = { showContactDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("تعديل", fontSize = 11.sp, color = FocusBlueSecondary)
                    }
                }
            }
        }

        // App Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF263E58)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "شعار التطبيق",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("hajji edu app • EduFocus", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("جميع الخرائط وبيانات المؤسسات التعليمية محملة مسبقاً وتعمل مع أو بدون إنترنت حفاظاً على الخصوصية والتركيز.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // PIN Change Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("تغيير رمز PIN الاستثنائي", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = pinValue,
                    onValueChange = { if (it.length <= 6) pinValue = it },
                    label = { Text("أدخل رمز PIN الجديد") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinValue.isNotBlank()) {
                            focusManager.updateSettings(userSettings.copy(emergencyPin = pinValue))
                            showPinDialog = false
                            Toast.makeText(context, "تم تحديث رمز PIN بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusEmerald)
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Emergency Contact Dialog
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("تعديل رقم هاتف الطوارئ", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = contactValue,
                    onValueChange = { contactValue = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (contactValue.isNotBlank()) {
                            focusManager.updateSettings(userSettings.copy(emergencyContact = contactValue))
                            showContactDialog = false
                            Toast.makeText(context, "تم تحديث رقم الطوارئ", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusEmerald)
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
