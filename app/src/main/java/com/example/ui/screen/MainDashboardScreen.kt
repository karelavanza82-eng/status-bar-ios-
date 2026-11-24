package com.example.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.data.StatusBarSettings
import com.example.ui.components.IOSStatusBarView
import com.example.service.IOSOverlayService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(context) }
    val settingsFlow = preferencesManager.preferencesFlow.collectAsState(
        initial = StatusBarSettings(
            isServiceActive = false,
            notchStyle = "Dynamic Island",
            clockPosition = "Left",
            carrierName = "Carrier",
            showSignal = true,
            showWifi = true,
            showBattery = true,
            networkType = "5G",
            transparent = true,
            paddingLeft = 16,
            paddingRight = 16,
            colorMode = "Auto (System)"
        )
    )
    val settings = settingsFlow.value

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Preview & Overlay, 1: Layout, 2: Style & Color
    var carrierInput by remember { mutableStateOf(settings.carrierName) }

    LaunchedEffect(settings.carrierName) {
        carrierInput = settings.carrierName
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(context)) {
            scope.launch {
                preferencesManager.updateServiceActive(true)
                startOverlayService(context)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF3B82F6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text("iOS Status Bar Pro", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Preview") },
                    label = { Text("Preview") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Layout") },
                    label = { Text("Tata Letak") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Palette, contentDescription = "Style & Color") },
                    label = { Text("Gaya & Warna") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    PreviewSimulatorSection(
                        settings = settings,
                        onToggleService = { active ->
                            if (active) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    overlayPermissionLauncher.launch(intent)
                                } else {
                                    scope.launch {
                                        preferencesManager.updateServiceActive(true)
                                        startOverlayService(context)
                                    }
                                }
                            } else {
                                scope.launch {
                                    preferencesManager.updateServiceActive(false)
                                    stopOverlayService(context)
                                }
                            }
                        }
                    )
                }
                1 -> {
                    LayoutCustomizerSection(
                        settings = settings,
                        onClockPositionChanged = { scope.launch { preferencesManager.updateClockPosition(it) } },
                        carrierName = carrierInput,
                        onCarrierChanged = {
                            carrierInput = it
                            scope.launch { preferencesManager.updateCarrierName(it) }
                        },
                        onSignalToggled = { scope.launch { preferencesManager.updateShowSignal(it) } },
                        onWifiToggled = { scope.launch { preferencesManager.updateShowWifi(it) } },
                        onBatteryToggled = { scope.launch { preferencesManager.updateShowBattery(it) } },
                        onNetworkChanged = { scope.launch { preferencesManager.updateNetworkType(it) } },
                        onPaddingChanged = { left, right -> scope.launch { preferencesManager.updatePadding(left, right) } }
                    )
                }
                2 -> {
                    StyleCustomizerSection(
                        settings = settings,
                        onNotchStyleChanged = { scope.launch { preferencesManager.updateNotchStyle(it) } },
                        onColorModeChanged = { scope.launch { preferencesManager.updateColorMode(it) } }
                    )
                }
            }
        }
    }
}

@Composable
fun PreviewSimulatorSection(
    settings: StatusBarSettings,
    onToggleService: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Floating Service", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Tampilkan iOS Status Bar di atas layar", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = settings.isServiceActive,
                    onCheckedChange = onToggleService
                )
            }

            HorizontalDivider()

            // Live Simulator Phone Frame with dynamic background testing (Dark vs Light)
            var isSimulatorDark by remember { mutableStateOf(true) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Uji Warna Latar Belakang (Test Background)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isSimulatorDark,
                        onClick = { isSimulatorDark = true },
                        label = { Text("Latar Hitam") }
                    )
                    FilterChip(
                        selected = !isSimulatorDark,
                        onClick = { isSimulatorDark = false },
                        label = { Text("Latar Terang") }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        if (isSimulatorDark)
                            Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
                        else
                            Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1)))
                    )
            ) {
                // Modified settings for preview simulator based on test background
                val previewSettings = settings.copy(
                    colorMode = if (settings.colorMode == "Auto (System)") {
                        if (isSimulatorDark) "Dark (White Icons)" else "Light (Black Icons)"
                    } else settings.colorMode
                )

                IOSStatusBarView(
                    settings = previewSettings,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1:55",
                        color = if (isSimulatorDark) Color.White else Color.Black,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = if (isSimulatorDark) "Latar Belakang Hitam -> Ikon Putih Otomatis" else "Latar Belakang Terang -> Ikon Hitam Otomatis",
                        color = if (isSimulatorDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Baterai Asli (Real Battery %)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Persentase baterai sesuai dengan perangkat Anda secara presisi dengan ukuran yang lebih besar & jelas.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun LayoutCustomizerSection(
    settings: StatusBarSettings,
    onClockPositionChanged: (String) -> Unit,
    carrierName: String,
    onCarrierChanged: (String) -> Unit,
    onSignalToggled: (Boolean) -> Unit,
    onWifiToggled: (Boolean) -> Unit,
    onBatteryToggled: (Boolean) -> Unit,
    onNetworkChanged: (String) -> Unit,
    onPaddingChanged: (Int, Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Atur Tata Letak Kanan & Kiri", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Sesuaikan posisi jam, operator, dan ikon sesuai keinginan Anda.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Posisi Jam (Clock Position)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Left", "Center", "Right").forEach { pos ->
                        FilterChip(
                            selected = settings.clockPosition == pos,
                            onClick = { onClockPositionChanged(pos) },
                            label = { Text(pos) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nama Operator & Jaringan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = carrierName,
                    onValueChange = onCarrierChanged,
                    label = { Text("Nama Operator (e.g. Telkomsel, Indosat)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("5G", "4G", "LTE", "Wi-Fi").forEach { net ->
                        FilterChip(
                            selected = settings.networkType == net,
                            onClick = { onNetworkChanged(net) },
                            label = { Text(net) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tampilkan Ikon Kanan & Kiri", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sinyal & Operator")
                    Switch(checked = settings.showSignal, onCheckedChange = onSignalToggled)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wi-Fi")
                    Switch(checked = settings.showWifi, onCheckedChange = onWifiToggled)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Baterai & Persentase")
                    Switch(checked = settings.showBattery, onCheckedChange = onBatteryToggled)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Padding Kiri & Kanan (${settings.paddingLeft} dp)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Slider(
                    value = settings.paddingLeft.toFloat(),
                    onValueChange = { onPaddingChanged(it.toInt(), it.toInt()) },
                    valueRange = 0f..40f
                )
            }
        }
    }
}

@Composable
fun StyleCustomizerSection(
    settings: StatusBarSettings,
    onNotchStyleChanged: (String) -> Unit,
    onColorModeChanged: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Pilih Gaya Notch & Warna Otomatis", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Sesuaikan tampilan dan adaptasi warna otomatis sesuai latar belakang.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Color Adaptation Mode
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Adaptasi Warna Otomatis (Auto Contrast)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Latar gelap -> Ikon putih. Latar terang -> Ikon hitam.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                listOf(
                    "Auto (System)" to "Otomatis menyesuaikan sistem / latar belakang",
                    "Dark (White Icons)" to "Selalu Putih (Cocok untuk latar hitam)",
                    "Light (Black Icons)" to "Selalu Hitam (Cocok untuk latar terang)"
                ).forEach { (mode, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onColorModeChanged(mode) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mode, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        RadioButton(
                            selected = settings.colorMode == mode,
                            onClick = { onColorModeChanged(mode) }
                        )
                    }
                }
            }
        }

        val styles = listOf(
            "Dynamic Island" to "Kapsul interaktif ala iPhone 15/16 Pro",
            "Notch" to "Poni klasik ala iPhone 14 Pro",
            "Clean Bar" to "Tanpa poni, bersih dan luas"
        )

        styles.forEach { (style, desc) ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (settings.notchStyle == style)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNotchStyleChanged(style) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(style, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    RadioButton(
                        selected = settings.notchStyle == style,
                        onClick = { onNotchStyleChanged(style) }
                    )
                }
            }
        }
    }
}

private fun startOverlayService(context: Context) {
    val intent = Intent(context, IOSOverlayService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}

private fun stopOverlayService(context: Context) {
    val intent = Intent(context, IOSOverlayService::class.java)
    context.stopService(intent)
}
