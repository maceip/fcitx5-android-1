/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2023 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.ui.main.settings.theme

import android.os.Bundle
import androidx.preference.SwitchPreference
import org.fcitx.fcitx5.android.data.prefs.ManagedPreference
import org.fcitx.fcitx5.android.data.prefs.ManagedPreferenceFragment
import org.fcitx.fcitx5.android.data.theme.ThemeManager
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.R

class ThemeSettingsFragment : ManagedPreferenceFragment(ThemeManager.prefs) {

    private val followSystemDayNightTheme = ThemeManager.prefs.followSystemDayNightTheme

    private var resumed = false

    private lateinit var switchPreference: SwitchPreference

    // sync SwitchPreference's state when `followSystemDayNightTheme` changed in ThemeListFragment
    private val listener = ManagedPreference.OnChangeListener<Boolean> { _, v ->
        if (resumed) return@OnChangeListener
        switchPreference.isChecked = v
    }

    private lateinit var micPreference: SwitchPreference

    private val micListener = ManagedPreference.OnChangeListener<Boolean> { _, v ->
        if (resumed) return@OnChangeListener
        micPreference.isChecked = v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        followSystemDayNightTheme.registerOnChangeListener(listener)
        AppPrefs.getInstance().keyboard.rainbowMicEnabled.registerOnChangeListener(micListener)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        switchPreference = findPreference(followSystemDayNightTheme.key)!!
        
        val prefs = AppPrefs.getInstance()
        val screen = preferenceScreen

        // Core Glass Keyboard Toggle logic removed and placed into LiquidGlassSettingsFragment

        // Add Rainbow Mic preference
        micPreference = SwitchPreference(requireContext()).apply {
            key = AppPrefs.getInstance().keyboard.rainbowMicEnabled.key
            title = getString(org.fcitx.fcitx5.android.R.string.rainbow_mic_enabled)
            isChecked = AppPrefs.getInstance().keyboard.rainbowMicEnabled.getValue()
            setOnPreferenceChangeListener { _, newValue ->
                AppPrefs.getInstance().keyboard.rainbowMicEnabled.setValue(newValue as Boolean)
                true
            }
        }
        preferenceScreen.addPreference(micPreference)
    }

    override fun onResume() {
        super.onResume()
        resumed = true
    }

    override fun onPause() {
        super.onPause()
        resumed = false
    }

    override fun onDestroy() {
        followSystemDayNightTheme.unregisterOnChangeListener(listener)
        AppPrefs.getInstance().keyboard.rainbowMicEnabled.unregisterOnChangeListener(micListener)
        super.onDestroy()
    }
}
