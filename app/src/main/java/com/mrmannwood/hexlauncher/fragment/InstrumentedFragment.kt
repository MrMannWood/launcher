package com.mrmannwood.hexlauncher.fragment

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import timber.log.Timber

abstract class InstrumentedFragment : Fragment() {

    abstract val nameForInstrumentation : String

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("Fragment %s: onCreate", nameForInstrumentation)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        Timber.v("Fragment %s: onStart", nameForInstrumentation)
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        Timber.v("Fragment %s: onResume", nameForInstrumentation)
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        Timber.v("Fragment %s: onPause", nameForInstrumentation)
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        Timber.v("Fragment %s: onStop", nameForInstrumentation)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        Timber.v("Fragment %s: onDestroy", nameForInstrumentation)
    }
}