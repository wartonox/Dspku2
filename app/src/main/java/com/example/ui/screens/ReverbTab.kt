package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ReverbConfig
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ReverbTab(
    config: ReverbConfig,
    onRoomSizeChange: (Float) -> Unit,
    onDecayTimeChange: (Float) -> Unit,
    onWetDryChange: (Float) -> Unit,
    onDampingChange: (Float) -> Unit,
    onPreDelayChange: (Float) -> Unit,
    onDiffusionChange: (Float) -> Unit,
    onSelectPreset: (name: String, room: Float, decay: Float, wet: Float, damp: Float) -> Unit,
    onToggleEnabled: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp)
            .testTag("reverb_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main Reverb Hardware Module
        RackModuleCard(
            title = "Acoustic Room Simulator",
            subtitle = "SCHROEDER-MOORER REVERB ENGINE // MODEL REV-800",
            isEnabled = config.enabled,
            onToggleEnabled = onToggleEnabled,
            onReset = onReset,
            accentColor = NeonAmber,
            testTag = "reverb_rack_card"
        ) {
            // Room Acoustic Type Preset Chips
            Text(
                text = "ACOUSTIC SPACE GEOMETRY",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val spaces = listOf(
                "Studio Live" to listOf(0.40f, 1.2f, 0.25f, 0.35f),
                "Vocal Plate" to listOf(0.50f, 1.6f, 0.32f, 0.20f),
                "Cathedral Vault" to listOf(0.95f, 4.5f, 0.55f, 0.50f),
                "Club Arena" to listOf(0.65f, 2.2f, 0.35f, 0.40f),
                "Warm Chamber" to listOf(0.35f, 1.0f, 0.20f, 0.60f),
                "Concert Hall" to listOf(0.80f, 3.0f, 0.45f, 0.30f),
                "Space Ambient" to listOf(1.00f, 7.5f, 0.65f, 0.15f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                spaces.forEach { (name, params) ->
                    val isSelected = config.presetName == name
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onSelectPreset(name, params[0], params[1], params[2], params[3])
                        },
                        label = {
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonAmber.copy(alpha = 0.2f),
                            selectedLabelColor = NeonAmber
                        ),
                        modifier = Modifier.testTag("reverb_chip_$name")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Acoustic Dials: Decay Time, Room Size, Wet/Dry Mix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotaryKnob(
                    value = config.decayTimeSec,
                    onValueChange = onDecayTimeChange,
                    range = 0.2f..8.0f,
                    defaultValue = 1.8f,
                    label = "DECAY TIME",
                    valueFormatter = { "%.1f s".format(it) },
                    accentColor = NeonAmber,
                    size = 68.dp,
                    testTag = "knob_rev_decay"
                )

                RotaryKnob(
                    value = config.roomSize,
                    onValueChange = onRoomSizeChange,
                    range = 0f..1f,
                    defaultValue = 0.55f,
                    label = "ROOM SIZE",
                    valueFormatter = { "%.0f %%".format(it * 100f) },
                    accentColor = NeonAmber,
                    size = 68.dp,
                    testTag = "knob_rev_room"
                )

                RotaryKnob(
                    value = config.wetDryMix,
                    onValueChange = onWetDryChange,
                    range = 0f..1f,
                    defaultValue = 0.35f,
                    label = "WET / DRY MIX",
                    valueFormatter = { "%.0f %%".format(it * 100f) },
                    accentColor = NeonAmber,
                    size = 68.dp,
                    testTag = "knob_rev_wetdry"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Damping & Pre-Delay Dials
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotaryKnob(
                    value = config.hfDamping,
                    onValueChange = onDampingChange,
                    range = 0f..1f,
                    defaultValue = 0.40f,
                    label = "HF DAMPING",
                    valueFormatter = { "%.0f %%".format(it * 100f) },
                    accentColor = NeonAmber,
                    size = 60.dp,
                    testTag = "knob_rev_damp"
                )

                RotaryKnob(
                    value = config.preDelayMs,
                    onValueChange = onPreDelayChange,
                    range = 0f..120f,
                    defaultValue = 20f,
                    label = "PRE-DELAY",
                    valueFormatter = { "%.0f ms".format(it) },
                    accentColor = NeonAmber,
                    size = 60.dp,
                    testTag = "knob_rev_predelay"
                )

                RotaryKnob(
                    value = config.diffusion,
                    onValueChange = onDiffusionChange,
                    range = 0f..1f,
                    defaultValue = 0.70f,
                    label = "DIFFUSION",
                    valueFormatter = { "%.0f %%".format(it * 100f) },
                    accentColor = NeonAmber,
                    size = 60.dp,
                    testTag = "knob_rev_diff"
                )
            }
        }
    }
}
