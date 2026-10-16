package com.carmabs.emanate.test

import com.carmabs.emanate.core.savestate.EmaRestoreState
import com.carmabs.emanate.core.savestate.EmaStateStore
import java.util.concurrent.ConcurrentHashMap

/**
 * Store that keeps in memory the values saved by the view models, to test that they are saved and restored. It
 * survives the view models that use it, like the store of the platform survives the death of the process.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaTestStateStore internal constructor() : EmaStateStore {

    private val savedValues = ConcurrentHashMap<String, String>()

    /**
     * Values saved, by key.
     */
    val values: Map<String, String>
        get() = savedValues.toMap()

    override fun read(key: String): String? = savedValues[key]

    override fun write(key: String, value: String?) {
        if (value == null) savedValues.remove(key) else savedValues[key] = value
    }
}

/**
 * Creates an empty store in memory. Pass it to the view model that receives the store of its backup, and then to a new
 * view model to simulate the death of the process: the new one restores the values saved by the previous one.
 */
fun testStateStore(): EmaTestStateStore = EmaTestStateStore()

/**
 * Creates the action that restores the saved [values], by key, to check it with assertActions:
 * `recorder.assertActions(MyInitializer, restoreStateOf("query" to "ema"))`. The values are the saved texts, as they
 * are in the store.
 */
fun restoreStateOf(vararg values: Pair<String, String>): EmaRestoreState = EmaRestoreState(values.toMap())
