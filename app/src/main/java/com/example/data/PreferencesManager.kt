package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ios_status_bar_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_SERVICE_ACTIVE = booleanPreferencesKey("service_active")
        val KEY_NOTCH_STYLE = stringPreferencesKey("notch_style") // "Dynamic Island", "Notch", "Clean Bar"
        val KEY_CLOCK_POSITION = stringPreferencesKey("clock_position") // "Left", "Center", "Right"
        val KEY_CARRIER_NAME = stringPreferencesKey("carrier_name")
        val KEY_SHOW_SIGNAL = booleanPreferencesKey("show_signal")
        val KEY_SHOW_WIFI = booleanPreferencesKey("show_wifi")
        val KEY_SHOW_BATTERY = booleanPreferencesKey("show_battery")
        val KEY_NETWORK_TYPE = stringPreferencesKey("network_type") // "5G", "4G", "LTE"
        val KEY_TRANSPARENT = booleanPreferencesKey("transparent") // Always true by default
        val KEY_PADDING_LEFT = intPreferencesKey("padding_left")
        val KEY_PADDING_RIGHT = intPreferencesKey("padding_right")
        val KEY_COLOR_MODE = stringPreferencesKey("color_mode") // "Auto (System)", "Dark (White Icons)", "Light (Black Icons)"
    }

    val preferencesFlow: Flow<StatusBarSettings> = context.dataStore.data
        .map { prefs ->
            StatusBarSettings(
                isServiceActive = prefs[KEY_SERVICE_ACTIVE] ?: false,
                notchStyle = prefs[KEY_NOTCH_STYLE] ?: "Dynamic Island",
                clockPosition = prefs[KEY_CLOCK_POSITION] ?: "Left",
                carrierName = prefs[KEY_CARRIER_NAME] ?: "Carrier",
                showSignal = prefs[KEY_SHOW_SIGNAL] ?: true,
                showWifi = prefs[KEY_SHOW_WIFI] ?: true,
                showBattery = prefs[KEY_SHOW_BATTERY] ?: true,
                networkType = prefs[KEY_NETWORK_TYPE] ?: "5G",
                transparent = prefs[KEY_TRANSPARENT] ?: true,
                paddingLeft = prefs[KEY_PADDING_LEFT] ?: 16,
                paddingRight = prefs[KEY_PADDING_RIGHT] ?: 16,
                colorMode = prefs[KEY_COLOR_MODE] ?: "Auto (System)"
            )
        }

    suspend fun updateServiceActive(active: Boolean) {
        context.dataStore.edit { it[KEY_SERVICE_ACTIVE] = active }
    }

    suspend fun updateNotchStyle(style: String) {
        context.dataStore.edit { it[KEY_NOTCH_STYLE] = style }
    }

    suspend fun updateClockPosition(pos: String) {
        context.dataStore.edit { it[KEY_CLOCK_POSITION] = pos }
    }

    suspend fun updateCarrierName(name: String) {
        context.dataStore.edit { it[KEY_CARRIER_NAME] = name }
    }

    suspend fun updateShowSignal(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_SIGNAL] = show }
    }

    suspend fun updateShowWifi(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_WIFI] = show }
    }

    suspend fun updateShowBattery(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_BATTERY] = show }
    }

    suspend fun updateNetworkType(type: String) {
        context.dataStore.edit { it[KEY_NETWORK_TYPE] = type }
    }

    suspend fun updatePadding(left: Int, right: Int) {
        context.dataStore.edit {
            it[KEY_PADDING_LEFT] = left
            it[KEY_PADDING_RIGHT] = right
        }
    }

    suspend fun updateColorMode(mode: String) {
        context.dataStore.edit { it[KEY_COLOR_MODE] = mode }
    }
}

data class StatusBarSettings(
    val isServiceActive: Boolean,
    val notchStyle: String,
    val clockPosition: String,
    val carrierName: String,
    val showSignal: Boolean,
    val showWifi: Boolean,
    val showBattery: Boolean,
    val networkType: String,
    val transparent: Boolean,
    val paddingLeft: Int,
    val paddingRight: Int,
    val colorMode: String
)
