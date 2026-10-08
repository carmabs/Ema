package com.carmabs.ema.android.extension

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.carmabs.ema.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.ema.core.action.EmaAction

inline fun <reified I : EmaAction.Initializer> Fragment.setInitializer(
    initializer: I,
    strategy: BundleSerializerStrategy
) {
    val bundle = Bundle()
    bundle.setInitializer(initializer, strategy)
    arguments = bundle
}

/**
 * Get the incoming initializer from another fragment/activity
 */
inline fun <reified I : EmaAction.Initializer> Fragment.getInitializer(
    serializerStrategy: BundleSerializerStrategy
): I? = arguments?.getInitializer(serializerStrategy)
