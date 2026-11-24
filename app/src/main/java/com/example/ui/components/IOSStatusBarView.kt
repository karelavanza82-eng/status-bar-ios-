package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.drawscope.Stroke
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
    var currentTime by remember { mutableStateOf("") }
    var batteryLevel by remember { mutableIntStateOf(69) }
    var expandedDynamicIsland by remember { mutableStateOf(false) }

    // Live clock ticker
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("h:mm", Locale.getDefault())
            currentTime = sdf.format(Date())
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(if (settings.transparent) Color.Transparent else Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = settings.paddingLeft.dp)
    ) {
        // Left Side: Time + Silent / DND Icon (Matching exact user reference image)
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = currentTime.ifEmpty { "1:55" },
                color = Color.Black, // Matches exact dark text on light background in reference
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp
            )

            // Silent / DND bell-slash icon as seen in user reference
            Icon(
                imageVector = Icons.Default.NotificationsOff,
                contentDescription = "Silent Mode",
                tint = Color.Black,
                modifier = Modifier.size(15.dp)
            )
        }

        // Center Notch / Dynamic Island (If enabled)
        if (settings.notchStyle == "Dynamic Island") {
            Box(
                modifier = Modifier.align(Alignment.Center),
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

        // Right Side: Signal bars + 5G + Exact iOS Battery Pill (Matching user reference image)
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Signal bars (4 bars: 3 active, 1 dimmed as in reference)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.height(12.dp)
            ) {
                IOSSignalBar(heightPercent = 0.4f, active = true)
                IOSSignalBar(heightPercent = 0.6f, active = true)
                IOSSignalBar(heightPercent = 0.8f, active = true)
                IOSSignalBar(heightPercent = 1.0f, active = false) // dimmed 4th bar
            }

            Text(
                text = settings.networkType,
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            )

            // Exact iOS Battery Pill from reference: Black capsule with white text inside
            ExactIOSBatteryView(batteryLevel = batteryLevel)
        }
    }
}

@Composable
fun IOSSignalBar(heightPercent: Float, active: Boolean) {
    Box(
        modifier = Modifier
            .width(3.dp)
            .height((12 * heightPercent).dp)
            .clip(RoundedCornerShape(1.dp))
            .background(if (active) Color.Black else Color.Black.copy(alpha = 0.3f))
    )
}

@Composable
fun ExactIOSBatteryView(batteryLevel: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        // Black pill container with white percentage text inside
        Box(
            modifier = Modifier
                .height(13.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black)
                .padding(horizontal = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$batteryLevel",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // Battery tip nub on the right
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .height(4.dp)
                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp))
        )
    }
}
