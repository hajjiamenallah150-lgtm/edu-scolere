package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.EducationalInstitution
import com.example.service.FocusManager
import com.example.service.SchoolZoneMonitorService
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.history.FocusHistoryScreen
import com.example.ui.institutions.InstitutionsListScreen
import com.example.ui.map.EduFocusMapView
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusBluePrimary
import com.example.ui.theme.FocusBlueSecondary
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusRedAlert
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.outlined.QrCode
import com.example.ui.uniqueid.UniqueIdSearchScreen

enum class MainTab(
    val labelAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("الرئيسية", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    MAP("الخريطة", Icons.Filled.Map, Icons.Outlined.Map),
    INSTITUTIONS("المؤسسات", Icons.Filled.School, Icons.Outlined.School),
    CODE_SEARCH("البحث بالمعرف", Icons.Filled.QrCode, Icons.Outlined.QrCode),
    SETTINGS("الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    val focusManager = remember { FocusManager.getInstance() }
    val authManager = remember { com.example.auth.AuthManager.getInstance(context) }
    val focusState by focusManager.focusState.collectAsState()
    val currentUser by authManager.currentUser.collectAsState()

    var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }

    // Request permissions on startup
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Restart service with granted permissions if needed
        if (focusState.isProtectionActive) {
            SchoolZoneMonitorService.start(context)
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionsLauncher.launch(missing.toTypedArray())
        }

        // Start background monitor service
        if (focusState.isProtectionActive) {
            SchoolZoneMonitorService.start(context)
        }
    }

    // BackHandler to navigate back to Dashboard
    BackHandler(enabled = currentTab != MainTab.DASHBOARD) {
        currentTab = MainTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF090D16),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF263E58))
                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "شعار التطبيق",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "hajji edu app",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "حارس تركيز المؤسسات التعليمية",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Google Account Quick Avatar
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (currentUser != null) FocusBluePrimary.copy(alpha = 0.2f)
                                        else Color(0xFF1E293B)
                                    )
                                    .clickable { currentTab = MainTab.SETTINGS }
                                    .padding(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "حساب Google",
                                    tint = if (currentUser != null) FocusBluePrimary else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Status Badge
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (focusState.isInsideZone) FocusRedAlert.copy(alpha = 0.2f)
                                        else FocusEmerald.copy(alpha = 0.2f)
                                    )
                                    .border(
                                        1.dp,
                                        if (focusState.isInsideZone) FocusRedAlert.copy(alpha = 0.6f)
                                        else FocusEmerald.copy(alpha = 0.6f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (focusState.isInsideZone) Icons.Default.Lock else Icons.Default.Security,
                                        contentDescription = null,
                                        tint = if (focusState.isInsideZone) FocusRedAlert else FocusEmerald,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (focusState.isInsideZone) "مقفل للدراسة" else "الحارس نشط",
                                        color = if (focusState.isInsideZone) FocusRedAlert else FocusEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
                MainTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.labelAr,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.labelAr,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = FocusBlueSecondary,
                            indicatorColor = FocusBluePrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    MainTab.DASHBOARD -> DashboardScreen(
                        onNavigateToMap = { currentTab = MainTab.MAP }
                    )
                    MainTab.MAP -> EduFocusMapView(
                        onInstitutionSelected = { /* handled inside map view */ }
                    )
                    MainTab.INSTITUTIONS -> InstitutionsListScreen(
                        onNavigateToMapWithInstitution = { _ ->
                            currentTab = MainTab.MAP
                        }
                    )
                    MainTab.CODE_SEARCH -> UniqueIdSearchScreen(
                        onNavigateToMapWithInstitution = { _ ->
                            currentTab = MainTab.MAP
                        }
                    )
                    MainTab.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}
