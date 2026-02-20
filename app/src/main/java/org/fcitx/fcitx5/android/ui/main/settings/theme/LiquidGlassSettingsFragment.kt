/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.ui.main.settings.theme

import android.os.Bundle
import androidx.preference.SwitchPreference
import org.fcitx.fcitx5.android.data.prefs.ManagedPreference
import org.fcitx.fcitx5.android.data.prefs.ManagedPreferenceFragment
import org.fcitx.fcitx5.android.data.theme.ThemeManager
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.R

class LiquidGlassSettingsFragment : ManagedPreferenceFragment(ThemeManager.prefs) {

    private lateinit var glassListener: ManagedPreference.OnChangeListener<Boolean>

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        val prefs = AppPrefs.getInstance()
        val screen = preferenceScreen

        val glassSwitch = SwitchPreference(requireContext()).apply {
            key = prefs.keyboard.glassKeyboard.key
            title = getString(R.string.glass_keyboard)
            isChecked = prefs.keyboard.glassKeyboard.getValue()
            setOnPreferenceChangeListener { _, newValue ->
                prefs.keyboard.glassKeyboard.setValue(newValue as Boolean)
                true
            }
        }
        screen.addPreference(glassSwitch)

        val glassFrostSlider = androidx.preference.SeekBarPreference(requireContext()).apply {
            key = prefs.keyboard.glassFrostLevel.key
            title = getString(R.string.glass_frost_level)
            min = 0
            max = 100
            updatesContinuously = true
            value = prefs.keyboard.glassFrostLevel.getValue()
            isEnabled = glassSwitch.isChecked
            setOnPreferenceChangeListener { _, newValue ->
                prefs.keyboard.glassFrostLevel.setValue(newValue as Int)
                true
            }
        }
        screen.addPreference(glassFrostSlider)

        val glassSmokeSwitch = SwitchPreference(requireContext()).apply {
            key = prefs.keyboard.glassSmokeMode.key
            title = getString(R.string.glass_smoke_mode)
            isChecked = prefs.keyboard.glassSmokeMode.getValue()
            isEnabled = glassSwitch.isChecked
            setOnPreferenceChangeListener { _, newValue ->
                prefs.keyboard.glassSmokeMode.setValue(newValue as Boolean)
                true
            }
        }
        screen.addPreference(glassSmokeSwitch)

        val glassPinkSwitch = SwitchPreference(requireContext()).apply {
            key = prefs.keyboard.glassElectricPink.key
            title = getString(R.string.glass_electric_pink)
            isChecked = prefs.keyboard.glassElectricPink.getValue()
            isEnabled = glassSwitch.isChecked
            setOnPreferenceChangeListener { _, newValue ->
                prefs.keyboard.glassElectricPink.setValue(newValue as Boolean)
                true
            }
        }
        screen.addPreference(glassPinkSwitch)

        val glassDarkModeSwitch = SwitchPreference(requireContext()).apply {
            key = prefs.keyboard.glassDarkMode.key
            title = getString(R.string.glass_dark_mode)
            isChecked = prefs.keyboard.glassDarkMode.getValue()
            isEnabled = glassSwitch.isChecked
            setOnPreferenceChangeListener { _, newValue ->
                prefs.keyboard.glassDarkMode.setValue(newValue as Boolean)
                true
            }
        }
        screen.addPreference(glassDarkModeSwitch)

        glassListener = ManagedPreference.OnChangeListener { _, value ->
            glassFrostSlider.isEnabled = value
            glassSmokeSwitch.isEnabled = value
            glassPinkSwitch.isEnabled = value
            glassDarkModeSwitch.isEnabled = value
            glassSwitch.isChecked = value
        }
        prefs.keyboard.glassKeyboard.registerOnChangeListener(glassListener)
    }

    override fun onDestroy() {
        AppPrefs.getInstance().keyboard.glassKeyboard.unregisterOnChangeListener(glassListener)
        super.onDestroy()
    }
}
