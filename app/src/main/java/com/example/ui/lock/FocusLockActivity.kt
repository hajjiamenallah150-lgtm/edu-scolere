package com.example.ui.lock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.STUDY_QUOTES
import com.example.service.FocusManager
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusBluePrimary
import com.example.ui.theme.FocusBlueSecondary
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusRedAlert
import com.example.ui.theme.MyApplicationTheme

class FocusLockActivity : ComponentActivity() {

    private val focusManager = FocusManager.getInstance()

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == FocusManager.ACTION_ZONE_UNLOCKED) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure window for lock screen display and hide system status bar clock
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val filter = IntentFilter(FocusManager.ACTION_ZONE_UNLOCKED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(unlockReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(unlockReceiver, filter)
        }

        setContent {
            MyApplicationTheme(darkTheme = true) {
                FocusLockScreen(
                    onEmergencyCall = {
                        val settings = focusManager.userSettings.value
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${settings.emergencyContact}")
                        }
                        startActivity(intent)
                    },
                    onExitSimulation = {
                        focusManager.simulateExitInstitution(this@FocusLockActivity)
                        finish()
                    },
                    onEmergencyUnlockSuccess = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(unlockReceiver)
        } catch (_: Exception) {}
    }
}

@Composable
fun FocusLockScreen(
    onEmergencyCall: () -> Unit,
    onExitSimulation: () -> Unit,
    onEmergencyUnlockSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = FocusManager.getInstance()
    val focusState by focusManager.focusState.collectAsState()
    val userSettings by focusManager.userSettings.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinText by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // Intercept back button to prevent circumventing lock screen
    BackHandler {
        Toast.makeText(
            context,
            "الهاتف مقفل حتى الخروج من الحرم التعليمي لضمان التركيز",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val quoteIndex = remember { mutableIntStateOf((STUDY_QUOTES.indices).random()) }
    val quote = STUDY_QUOTES[quoteIndex.intValue]

    val mins = focusState.sessionDurationSeconds / 60
    val secs = focusState.sessionDurationSeconds % 60
    val hours = mins / 60
    val displayMins = mins % 60
    val timerString = if (hours > 0) {
        "%02d:%02d:%02d".format(hours, displayMins, secs)
    } else {
        "%02d:%02d".format(mins, secs)
    }

    val inst = focusState.currentInstitution

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF070B14)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B132B),
                            Color(0xFF070B14),
                            Color(0xFF04060A)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 36.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Badge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF132238))
                            .border(1.dp, FocusEmerald.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = FocusEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حرم تعليمي محمي • وضع التركيز الصارم",
                                color = FocusEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF263E58))
                            .border(1.5.dp, FocusEmerald.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "شعار التطبيق",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "الهاتف مقفل للدراسة",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "تم كتم الإشعارات والمشتتات لضمان أعلى درجات الانتباه",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Center Pulsing Timer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(200.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        FocusBluePrimary.copy(alpha = 0.35f),
                                        FocusEmerald.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(3.dp, FocusEmerald, CircleShape)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (userSettings.hideTimeForStudent) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "الوقت محجوب",
                                    tint = FocusEmerald,
                                    modifier = Modifier.size(34.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "الوقت محجوب",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "التركيز على الدرس",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "مراقبة الساعة معطلة",
                                    color = FocusEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = FocusEmerald,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = timerString,
                                    color = Color.White,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "مدة التركيز الحالية",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Institution details card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF111C33)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = FocusAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = inst?.nameAr ?: "المؤسسة التعليمية",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "نطاق الحرم: ${inst?.radiusMeters?.toInt() ?: 250}م • ${inst?.city ?: "الموقع الأكاديمي"}",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0B1325))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "« ${quote.quoteAr} »",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 13.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "— ${quote.author}",
                                        color = FocusAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔓 سيفتح الهاتف تلقائياً فور الابتعاد عن محيط المؤسسة",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Emergency Pin Unlock
                        OutlinedButton(
                            onClick = { showPinDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("emergency_pin_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFCBD5E1)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("رمز المعلم / ولي الأمر", fontSize = 11.sp)
                        }

                        // Emergency Call
                        Button(
                            onClick = onEmergencyCall,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("emergency_call_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FocusRedAlert
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("طوارئ (${userSettings.emergencyContact})", fontSize = 11.sp)
                        }
                    }

                    // Test/Simulation exit button (very useful for emulator & demo)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onExitSimulation,
                        modifier = Modifier.testTag("simulate_exit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "محاكاة الخروج لفك القفل (تجريبي)",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinError = false
                pinText = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = FocusAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "فك القفل الاستثنائي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "أدخل رمز الأمان المخصص للمعلم أو ولي الأمر (الرمز الافتراضي: 1234)",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = {
                            if (it.length <= 6) {
                                pinText = it
                                pinError = false
                            }
                        },
                        label = { Text("رمز PIN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pinError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_input_field")
                    )
                    if (pinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الرمز المدخل غير صحيح! حاول مرة أخرى.",
                            color = FocusRedAlert,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = focusManager.unlockEmergency(pinText, context)
                        if (success) {
                            showPinDialog = false
                            onEmergencyUnlockSuccess()
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusEmerald),
                    modifier = Modifier.testTag("confirm_pin_button")
                ) {
                    Text("فك القفل")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPinDialog = false
                        pinError = false
                        pinText = ""
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}
