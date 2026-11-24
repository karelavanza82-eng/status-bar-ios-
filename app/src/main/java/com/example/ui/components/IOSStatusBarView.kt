package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StatusBarSettings
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IOSStatusBarView(
    settings: StatusBarSettings,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = false,
    onDynamicIslandClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf("") }
    var batteryLevel by remember { mutableIntStateOf(69) }
    var isCharging by remember { mutableStateOf(false) }

    // Real battery level receiver
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("h:mm", Locale.getDefault())
            currentTime = sdf.format(Date())

            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
            batteryStatus?.let { intent ->
                val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryLevel = ((level / scale.toFloat()) * 100).toInt()
                }
                val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            }

            delay(1000)
        }
    }

    // Determine automatic icon / text color based on user colorMode setting & system theme
    val systemDark = isSystemInDarkTheme()
    val contentColor = when (settings.colorMode) {
        "Dark (White Icons)" -> Color.White
        "Light (Black Icons)" -> Color.Black
        else -> if (systemDark) Color.White else Color.Black
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(if (settings.transparent) Color.Transparent else Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = settings.paddingLeft.dp)
    ) {
        // Left Side: Time + Silent / DND Icon (Strictly vertically centered and aligned)
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = currentTime.ifEmpty { "12:12" },
                color = contentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp
            )

            Icon(
                imageVector = Icons.Default.NotificationsOff,
                contentDescription = "Silent Mode",
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
        }

        // Center Notch / Dynamic Island
        if (settings.notchStyle == "Dynamic Island") {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black)
                )
            }
        }

        // Right Side: Signal bars + 5G + Battery Pill (Strictly vertically aligned and leveled together)
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Signal bars (4 bars: 3 active, 1 dimmed)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.height(13.dp)
            ) {
                IOSSignalBar(heightPercent = 0.4f, active = true, color = contentColor)
                IOSSignalBar(heightPercent = 0.6f, active = true, color = contentColor)
                IOSSignalBar(heightPercent = 0.8f, active = true, color = contentColor)
                IOSSignalBar(heightPercent = 1.0f, active = false, color = contentColor)
            }

            Text(
                text = settings.networkType,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            )

            // Perfectly aligned battery pill matching reference screenshot
            ExactPreciseIOSBatteryView(batteryLevel = batteryLevel)
        }
    }
}

@Composable
fun IOSSignalBar(heightPercent: Float, active: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .width(3.dp)
            .height((13 * heightPercent).dp)
            .clip(RoundedCornerShape(1.dp))
            .background(if (active) color else color.copy(alpha = 0.3f))
    )
}

@Composable
fun ExactPreciseIOSBatteryView(batteryLevel: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Box(
            modifier = Modifier
                .height(15.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.Black)
                .padding(horizontal = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$batteryLevel",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // Battery tip nub on the right
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .height(5.dp)
                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp))
        )
    }
}
