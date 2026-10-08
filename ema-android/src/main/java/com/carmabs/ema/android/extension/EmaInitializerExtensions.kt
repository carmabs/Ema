package com.carmabs.ema.android.extension

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.carmabs.ema.android.initializer.EmaInitializerBundle
import com.carmabs.ema.android.initializer.bundle.BundleSerializer
import com.carmabs.ema.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.ema.core.action.EmaAction

/**
 * Set a initializer for current bundle
 */
inline fun <reified I : EmaAction.Initializer> Bundle.setInitializer(
    initializer: I,
    strategy: BundleSerializerStrategy
) {
    BundleSerializer(this, strategy).save(initializer)
}

fun EmaAction.Initializer?.toBundle(serializerStrategy: BundleSerializerStrategy): Bundle? = this?.let {
    val bundle = Bundle()
    bundle.setInitializer(it, serializerStrategy)
    bundle
}

fun EmaInitializerBundle?.toBundle(): Bundle? = this?.let {
    val bundle = Bundle()
    bundle.setInitializer(it.initializer, it.serializer)
    bundle
}

/**
 * Get a initializer for current bundle
 */
inline fun <reified I : EmaAction.Initializer> Bundle.getInitializer(strategy: BundleSerializerStrategy): I? =
    BundleSerializer(this, strategy).restore() as? I

/**
 * Get the incoming initializer from another activity by the initializer provided
 */
inline fun <reified I : EmaAction.Initializer> Activity.getInitializer(
    strategy: BundleSerializerStrategy,
    savedInstanceState: Bundle?
): I? = intent?.let {
    (it.extras ?: savedInstanceState)?.let { bundle ->
        BundleSerializer(bundle, strategy).restore() as? I
    }
}

/**
 * Set the initializer for the current activity intent
 */
inline fun <reified I : EmaAction.Initializer> Activity.setInitializer(
    initializer: I,
    strategy: BundleSerializerStrategy
) {
    intent = Intent().setInitializer(initializer, strategy)
}

/**
 * Set the initializer for the current activity intent
 */
inline fun <reified I : EmaAction.Initializer> Intent.setInitializer(
    initializer: I,
    strategy: BundleSerializerStrategy
): Intent {
    val bundle = Bundle()
    bundle.setInitializer(initializer, strategy)
    putExtras(bundle)
    return this
}

/**
 * Get the initializer of the activity intent
 */
inline fun <reified I : EmaAction.Initializer> Intent.getInitializer(strategy: BundleSerializerStrategy): I? =
    extras?.getInitializer(strategy)
