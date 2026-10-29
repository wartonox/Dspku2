package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Studio Hardware Rack Enclosure with bolts, faceplate, and power/bypass switch
 */
@Composable
fun RackModuleCard(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggleEnabled: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonCyan,
    onReset: (() -> Unit)? = null,
    testTag: String = "rack_module",
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(StudioCard)
            .border(1.dp, StudioRackBorder, RoundedCornerShape(10.dp))
            .padding(1.dp)
    ) {
        // Faceplate Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioRackPanel)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Module Title & Status LED
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Illuminated Status LED
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isEnabled) accentColor else LedOff)
                        .border(1.dp, if (isEnabled) accentColor.copy(alpha = 0.5f) else StudioRackBorder, CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = title.uppercase(),
                        color = if (isEnabled) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Power / Bypass Switch & Reset
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onReset != null) {
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("${testTag}_reset")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Module Defaults",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Power / Bypass Switch Button
                Row(
                    modifier = Modifier
                        .testTag("${testTag}_power_toggle")
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isEnabled) accentColor.copy(alpha = 0.15f) else Color(0xFF0F172A))
                        .border(
                            1.dp,
                            if (isEnabled) accentColor.copy(alpha = 0.6f) else StudioRackBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onToggleEnabled() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = if (isEnabled) "Bypass Module" else "Engage Module",
                        tint = if (isEnabled) accentColor else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEnabled) "ACTIVE" else "BYPASS",
                        color = if (isEnabled) accentColor else TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Module Body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            content()
        }
    }
}
