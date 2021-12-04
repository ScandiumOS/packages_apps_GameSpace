/*
 * Copyright (C) 2021 Chaldeaprjkt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.chaldeaprjkt.gamespace.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import io.chaldeaprjkt.gamespace.R
import io.chaldeaprjkt.gamespace.data.SystemSettings
import io.chaldeaprjkt.gamespace.preferences.AppListPreferences
import io.chaldeaprjkt.gamespace.preferences.appselector.AppSelectorActivity


class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private var apps: AppListPreferences? = null
    private var settings: SystemSettings? = null

    private val selectorResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            apps?.useSelectorResult(it)
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settings = context?.let { SystemSettings(it) }
        apps = findPreference(SystemSettings.KEY_GAME_LIST)

        listOf(
            SystemSettings.KEY_AUTO_BRIGHTNESS_DISABLE,
            SystemSettings.KEY_HEADS_UP_DISABLE,
            SystemSettings.KEY_3SCREENSHOT_DISABLE,
            SystemSettings.KEY_NAVBAR_DISABLE
        ).onEach {
            findPreference<SwitchPreference>(it)?.onPreferenceChangeListener = this
        }

        findPreference<Preference>(AppListPreferences.KEY_ADD_GAME)
            ?.setOnPreferenceClickListener {
                selectorResult.launch(Intent(context, AppSelectorActivity::class.java))
                return@setOnPreferenceClickListener true
            }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        when (preference?.key) {
            SystemSettings.KEY_AUTO_BRIGHTNESS_DISABLE -> {
                settings?.userNoAutoBrightness = newValue as Boolean
                return true
            }
            SystemSettings.KEY_HEADS_UP_DISABLE -> {
                settings?.userNoHeadsUp = newValue as Boolean
                return true
            }
            SystemSettings.KEY_3SCREENSHOT_DISABLE -> {
                settings?.userNoThreeScreenshot = newValue as Boolean
                return true
            }
            SystemSettings.KEY_NAVBAR_DISABLE -> {
                settings?.userNoNavbar = newValue as Boolean
                return true
            }
        }
        return false
    }
}
