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
package io.chaldeaprjkt.gamespace.data

import android.content.Context
import android.media.AudioManager
import com.google.gson.Gson

class GameSession(private val context: Context) {

    private val db by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    private val appSettings by lazy { AppSettings(context) }
    private val systemSettings by lazy { SystemSettings(context) }
    private val gson by lazy { Gson() }
    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var state
        get() = db.getString(KEY_SAVED_SESSION, "")
            .takeIf { !it.isNullOrEmpty() }
            ?.let {
                try {
                    gson.fromJson(it, SessionState::class.java)
                } catch (e: RuntimeException) {
                    null
                }
            }
        private set(value) = db.edit()
            .putString(KEY_SAVED_SESSION, value?.let {
                try {
                    gson.toJson(value)
                } catch (e: RuntimeException) {
                    ""
                }
            } ?: "")
            .apply()

    fun register(sessionName: String) {
        if (state?.packageName != sessionName) unregister()

        state = SessionState(
            packageName = sessionName,
            autoBrightness = systemSettings.autoBrightness,
            headsUp = systemSettings.headsUp,
            threeScreenshot = systemSettings.threeScreenshot,
            navbarToggle = systemSettings.navbarToggle,
            ringerMode = audioManager.ringerModeInternal,
        )
        if (appSettings.noHeadsUp) {
            systemSettings.headsUp = false
        }
        if (appSettings.noAutoBrightness) {
            systemSettings.autoBrightness = false
        }
        if (appSettings.noThreeScreenshot) {
            systemSettings.threeScreenshot = false
        }
        if (appSettings.noNavbar) {
            systemSettings.navbarToggle = false
        }
        audioManager.ringerModeInternal = appSettings.ringerMode
    }

    fun unregister() {
        val orig = state?.copy() ?: return
        if (appSettings.noHeadsUp) {
            orig.headsUp?.let { systemSettings.headsUp = it }
        }
        if (appSettings.noAutoBrightness) {
            orig.autoBrightness?.let { systemSettings.autoBrightness = it }
        }
        if (appSettings.noThreeScreenshot) {
            orig.threeScreenshot?.let { systemSettings.threeScreenshot = it }
        }
        if (appSettings.noNavbar) {
            orig.navbarToggle?.let { systemSettings.navbarToggle = it }
        }
        audioManager.ringerModeInternal = orig.ringerMode
        state = null
    }

    fun finalize() {
        unregister()
    }

    companion object {
        const val PREFS_NAME = "persisted_session"
        const val KEY_SAVED_SESSION = "session"
    }
}
