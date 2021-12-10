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
import android.provider.Settings
import io.chaldeaprjkt.gamespace.utils.GameModeUtils

class SystemSettings(private val context: Context) {

    private val resolver = context.contentResolver

    var systemHeadsUp
        get() =
            Settings.Global.getInt(resolver, Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 1) == 1
        set(it) {
            Settings.Global.putInt(
                resolver,
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED,
                it.toInt()
            )
        }

    var autoBrightness
        get() =
            Settings.System.getInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            ) ==
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        set(auto) {
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                if (auto) Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                else Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
        }

    var threeScreenshot
        get() = Settings.System.getInt(resolver, Settings.System.THREE_FINGER_GESTURE, 0) == 1
        set(it) {
            Settings.System.putInt(resolver, Settings.System.THREE_FINGER_GESTURE, it.toInt())
        }
    var navbarToggle
        get() = Settings.System.getInt(resolver, Settings.System.FORCE_SHOW_NAVBAR, 1) == 1
        set(it) {
            Settings.System.putInt(resolver, Settings.System.FORCE_SHOW_NAVBAR, it.toInt())
        }

    var userGames
        get() =
            Settings.System.getString(resolver, KEY_GAME_LIST)
                ?.split(";")
                ?.toList()?.filter { it.isNotEmpty() }
                ?.map { UserGame.fromSettings(it) } ?: emptyList()
        set(games) {
            Settings.System.putString(
                resolver, KEY_GAME_LIST,
                if (games.isEmpty()) "" else
                    games.joinToString(";") { it.toString() }
            )
            GameModeUtils.setupBatteryMode(context, games.isNotEmpty())
        }

    var userNoHeadsUp
        get() = Settings.System.getInt(resolver, KEY_HEADS_UP_DISABLE, 0) == 1
        set(it) {
            Settings.System.putInt(resolver, KEY_HEADS_UP_DISABLE, it.toInt())
        }

    var userNoAutoBrightness
        get() = Settings.System.getInt(resolver, KEY_AUTO_BRIGHTNESS_DISABLE, 0) == 1
        set(it) {
            Settings.System.putInt(resolver, KEY_AUTO_BRIGHTNESS_DISABLE, it.toInt())
        }

    var userNoThreeScreenshot
        get() = Settings.System.getInt(resolver, KEY_3SCREENSHOT_DISABLE, 0) == 1
        set(it) {
            Settings.System.putInt(resolver, KEY_3SCREENSHOT_DISABLE, it.toInt())
        }
    var userNoNavbar
        get() = Settings.System.getInt(resolver, KEY_NAVBAR_DISABLE, 0) == 1
        set(it) {
            Settings.System.putInt(resolver, KEY_NAVBAR_DISABLE, it.toInt())
        }
    private fun Boolean.toInt() = if (this) 1 else 0

    fun applyUserSettings(session: SessionState) {
        session.headsUp = systemHeadsUp
        session.autoBrightness = autoBrightness
        session.threeScreenshot = threeScreenshot
        session.navbarToggle = navbarToggle
        if (userNoHeadsUp) {
            systemHeadsUp = false
        }
        if (userNoAutoBrightness) {
            autoBrightness = false
        }
        if (userNoThreeScreenshot) {
            threeScreenshot = false
        }
        if (userNoNavbar) {
            navbarToggle = false
        }
    }

    fun restoreUserSettings(session: SessionState) {
        if (userNoHeadsUp) {
            session.headsUp?.let { systemHeadsUp = it }
        }
        if (userNoAutoBrightness) {
            session.autoBrightness?.let { autoBrightness = it }
        }
        if (userNoThreeScreenshot) {
            session.threeScreenshot?.let { threeScreenshot = it }
        }
        if (userNoNavbar) {
            session.navbarToggle?.let { navbarToggle = it }
        }
    }

    companion object {
        const val KEY_GAME_LIST = "gamespace_game_list"
        const val KEY_HEADS_UP_DISABLE = "gamespace_heads_up_disabled"
        const val KEY_AUTO_BRIGHTNESS_DISABLE = "gamespace_auto_brightness_disabled"
        const val KEY_3SCREENSHOT_DISABLE = "gamespace_tfgesture_disabled"
        const val KEY_NAVBAR_DISABLE = "gamespace_navbar_disabled"
    }
}
