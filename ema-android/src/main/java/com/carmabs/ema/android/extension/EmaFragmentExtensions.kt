package com.carmabs.ema.android.extension

import android.annotation.SuppressLint
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.os.BuildCompat
import androidx.fragment.app.Fragment


/**
 * Extensions for fragment
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

/**
 * Add a listener to handle the hardware back button.
 * @param listener Listener to handle the back event. It must return true to enable default back behaviour. False to enable custom
 * back behaviour.
 */
@SuppressLint("UnsafeOptInUsageError")
fun Fragment.addOnBackPressedListener(listener: () -> Boolean) {
    if (BuildCompat.isAtLeastT()) {
        val activity = requireActivity()
        val onBackInvokedCallback = object : OnBackInvokedCallback {
            override fun onBackInvoked() {
                val backDefaultLaunched = listener.invoke()
                if (backDefaultLaunched) {
                    activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(this)
                    activity.onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        activity.onBackInvokedDispatcher
            .registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT, onBackInvokedCallback
            )
    } else {
        val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val backDefaultLaunched = listener.invoke()
                isEnabled = !backDefaultLaunched
                if (backDefaultLaunched) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, onBackPressedCallback
        )
    }
}


