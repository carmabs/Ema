package com.carmabs.emanate.core.savestate

import com.carmabs.emanate.core.error.runHandlingErrors
import com.carmabs.emanate.core.source.EmaSource
import com.carmabs.emanate.core.source.EmaSourceObserver
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.concurrent.Volatile

/**
 * Created by Carlos Mateo Benito on 13/10/2026.
 *
 * Saves the values of [backups], installed in the setup of [source], in [store], and restores the ones saved before.
 * It observes the states of the source and writes a value only when it changes. EmaViewModel creates one with
 * the store of its platform; create one to save the state of a source on its own, for example on a platform without
 * view models.
 *
 * The values read from the store aren't written until they are restored, so an initializer can't replace them with the
 * initial state. Call [restore] once, after the initializer, or those values are never saved again.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaStateSaver<S : EmaState>(
    private val source: EmaSource<S, *, *>,
    private val store: EmaStateStore,
    vararg backups: EmaBackup<S>
) {

    private val savedValues = backups.flatMap { it.values }

    init {
        val keys = savedValues.map { it.key }
        require(keys.size == keys.toSet().size) { "The backups have repeated keys: $keys" }
    }

    /**
     * Last text written in the store, or read from it, of each key
     */
    @Volatile
    private var storedValues: Map<String, String?> = savedValues.associate { it.key to store.read(it.key) }

    /**
     * Texts read from the store that haven't been restored yet
     */
    @Volatile
    private var pendingValues: Map<String, String> =
        storedValues.mapNotNull { (key, value) -> value?.let { key to it } }.toMap()

    @Volatile
    private var restored = false

    private val observer = object : EmaSourceObserver<S, EmaEvent> {
        override fun onState(state: S) {
            save(state)
        }
    }

    init {
        source.addObserver(observer)
    }

    /**
     * Dispatches to the source the values read from the store, after the actions dispatched before, like its
     * initializer. Then they are saved again from the restored state. Only the first call does something.
     */
    fun restore() {
        if (restored) return
        restored = true
        pendingValues.takeIf { it.isNotEmpty() }?.let { source.dispatchAction(EmaRestoreState(it)) }
        source.post {
            pendingValues = emptyMap()
            save(source.state)
        }
    }

    private fun save(state: S) {
        val pending = pendingValues
        savedValues.forEach { savedValue ->
            val key = savedValue.key
            if (key in pending) return@forEach
            source.configuration.runHandlingErrors(source.container, { "save $key" }) {
                val value = savedValue.encode(state)
                if (value != storedValues[key]) {
                    store.write(key, value)
                    storedValues = storedValues + (key to value)
                }
            }
        }
    }

    private fun <T> EmaBackupValue<S, T>.encode(state: S): String? = save(state)?.let(codec::encode)
}
