package com.zyyme.einkrk

import android.content.Context
import android.util.Log
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class EinkSettings(val mode: Int, val gamma: Float, val gray256: Boolean)

object SettingsRepository {
    private const val TAG = "EinkRk"
    private const val PREFS = "eink_settings"
    private const val MODE = "mode"
    private const val GAMMA = "gamma"
    private const val GRAY256 = "gray256"
    private val applyExecutor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "EinkRk-setprop").apply { isDaemon = true }
    }

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
        apply(context)
    }

    fun apply(context: Context) {
        val appContext = context.applicationContext
        applyExecutor.execute { apply(read(appContext), appContext) }
    }

    private fun apply(settings: EinkSettings, context: Context) {
        runSetProp(context, "sys.ebook.mode", settings.mode.toString())
        runSetProp(context, "debug.sf.gamma.gamma", settings.gamma.toString())
        runSetProp(context, "persist.ebook.gray256_enable", if (settings.gray256) "1" else "0")
    }

    private fun runSetProp(context: Context, key: String, value: String) {
        val result = AdbRootClient.setProp(context, key, value)
        if (result.isSuccess) {
            Log.i(TAG, "setprop $key succeeded${result.output.trim().let { if (it.isEmpty()) "" else "; output=$it" }}")
        } else {
            Log.e(TAG, "setprop $key failed: ${result.error}")
        }
    }
}
