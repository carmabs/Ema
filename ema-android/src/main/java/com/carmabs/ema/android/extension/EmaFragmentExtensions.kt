package com.carmabs.ema.android.extension

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment


/**
 * Extensions for fragment
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

/**
 * Add a listener to handle the hardware back button.
 * @param listener Listener to handle the back event. It must return true to disable the normal back behaviour. False to enable it and
 * go back as usual.
 */
fun Fragment.addOnBackPressedListener(listener:()->Boolean){
    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                isEnabled = listener.invoke()
                if(!isEnabled){
                    requireActivity().onBackPressed()
                }
            }
        }
    )
}
