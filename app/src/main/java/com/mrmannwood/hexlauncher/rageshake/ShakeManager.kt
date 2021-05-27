package com.mrmannwood.hexlauncher.rageshake

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import com.squareup.seismic.ShakeDetector

class ShakeManager(triggerShakes: Int, onRageShake: () -> Unit) {

    private val shakeDetector = ShakeDetector(object : ShakeDetector.Listener {

        private val handler = Handler(Looper.getMainLooper())
        private var numShakes = 0

        override fun hearShake() {
            handler.removeCallbacks(resetRunnable)
            numShakes++
            if (numShakes == triggerShakes) {
                onRageShake()
                numShakes = 0
            } else {
                handler.postDelayed(resetRunnable, 500)
            }
        }

        private val resetRunnable = Runnable { numShakes = 0 }

    })

    fun startRageShakeDetector(context: Context) {
        shakeDetector.start(context.getSystemService(SENSOR_SERVICE) as SensorManager)
    }

    fun stopRageShakeDetector() {
        shakeDetector.stop()
    }
}