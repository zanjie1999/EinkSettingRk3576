package com.zyyme.einkrk

import android.content.Context
import android.util.Log

data class EinkSettings(val mode: Int, val gamma: Float, val gray256: Boolean)

object SettingsRepository {
    private const val TAG = "EinkRk"
    private const val PREFS = "eink_settings"
    private const val MODE = "mode"
    private const val GAMMA = "gamma"
    private const val GRAY256 = "gray256"

    fun read(context: Context): EinkSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return EinkSettings(
            mode = prefs.getInt(MODE, 7),
            gamma = prefs.getFloat(GAMMA, 1.5f),
            gray256 = prefs.getBoolean(GRAY256, false)
        )
    }

    fun save(context: Context, mode: Int, gamma: Float, gray256: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(MODE, mode)
            .putFloat(GAMMA, gamma.coerceIn(0.5f, 1.5f))
            .putBoolean(GRAY256, gray256)
            .apply()
        apply(EinkSettings(mode, gamma, gray256))
    }

    fun apply(context: Context) = apply(read(context))

    fun apply(settings: EinkSettings) {
        runSetProp("sys.ebook.mode", settings.mode.toString())
        runSetProp("debug.sf.gamma.gamma", settings.gamma.toString())
        runSetProp("persist.ebook.gray256_enable", if (settings.gray256) "1" else "0")
    }

    private fun runSetProp(key: String, value: String) {
        val command = "setprop $key $value"
        try {
            // adb root only affects the adb shell. An app must explicitly enter the
            // root domain through the su binary installed by the device's root manager.
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val exitCode = process.waitFor()
            val stdout = process.inputStream.bufferedReader().use { it.readText() }.trim()
            val stderr = process.errorStream.bufferedReader().use { it.readText() }.trim()
            if (exitCode == 0) {
                Log.i(TAG, "$command via su succeeded${if (stdout.isNotEmpty()) "; stdout=$stdout" else ""}")
            } else {
                Log.e(TAG, "$command via su failed; exitCode=$exitCode${if (stderr.isNotEmpty()) "; stderr=$stderr" else ""}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "$command via su could not be executed: ${e.javaClass.simpleName}: ${e.message}", e)
        }
    }
}
