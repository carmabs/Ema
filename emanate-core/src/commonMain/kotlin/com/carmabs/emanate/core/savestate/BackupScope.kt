package com.carmabs.emanate.core.savestate

import com.carmabs.emanate.core.source.EmaSourceDsl
import com.carmabs.emanate.core.source.SetupGuard
import com.carmabs.emanate.core.state.EmaState

/**
 * Created by Carlos Mateo Benito on 14/10/2026.
 *
 * Block of setup that adds the parts of the state saved by the view model: with [add], or with `+` for the ones
 * already created. Each value is saved every time it changes and, when the view model is created again after the
 * death of the process, it is restored after the initializer, in the order they are added.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@EmaSourceDsl
class BackupScope<S : EmaState> internal constructor(private val guard: SetupGuard) {

    internal var savedValueList: List<EmaSavedValue<S, *>> = emptyList()
        private set

    /**
     * Adds saved values already created.
     *
     * @throws IllegalArgumentException if a key is already saved
     */
    fun add(vararg savedValue: EmaSavedValue<S, *>) {
        guard.checkNotBuilt()
        savedValue.forEach { value ->
            require(savedValueList.none { it.key == value.key }) { "The key ${value.key} is already saved" }
            savedValueList = savedValueList + value
        }
    }

    /**
     * Adds a saved value already created.
     */
    operator fun EmaSavedValue<S, *>.unaryPlus() {
        add(this)
    }

    /**
     * Saves text with [key].
     *
     * @param key unique in the view model
     * @param save returns the text to save from the state, or null to save nothing
     * @param restore returns the state with the saved text restored
     */
    fun add(key: String, save: S.() -> String?, restore: S.(value: String) -> S) {
        add(savedValueOf(key, save, restore))
    }

    /**
     * Saves a value of type [T] with [key], converted to text with [codec].
     *
     * @param key unique in the view model
     * @param save returns the value to save from the state, or null to save nothing
     * @param restore returns the state with the saved value restored
     */
    fun <T> add(key: String, codec: EmaSavedCodec<T>, save: S.() -> T?, restore: S.(value: T) -> S) {
        add(savedValueOf(key, codec, save, restore))
    }
}
