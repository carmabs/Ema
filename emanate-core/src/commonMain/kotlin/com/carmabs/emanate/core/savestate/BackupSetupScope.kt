package com.carmabs.emanate.core.savestate

import com.carmabs.emanate.core.source.EmaSourceDsl
import com.carmabs.emanate.core.source.SetupGuard
import com.carmabs.emanate.core.state.EmaState

/**
 * Created by Carlos Mateo Benito on 14/10/2026.
 *
 * Adds the values of an [EmaBackup], created with backupOf: with add, or with `+` for the ones already created. Each
 * value is saved every time it changes and, after the death of the process, it is restored in the order they are
 * added.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@EmaSourceDsl
class BackupSetupScope<S : EmaState> internal constructor(private val guard: SetupGuard) {

    internal var backupValueList: List<EmaBackupValue<S, *>> = emptyList()
        private set

    /**
     * Adds a saved value already created.
     *
     * @throws IllegalArgumentException if its key is already saved
     */
    operator fun EmaBackupValue<S, *>.unaryPlus() {
        include(this)
    }

    private fun include(savedValue: EmaBackupValue<S, *>) {
        guard.checkNotBuilt()
        require(backupValueList.none { it.key == savedValue.key }) { "The key ${savedValue.key} is already saved" }
        backupValueList = backupValueList + savedValue
    }

    /**
     * Saves text with [key].
     *
     * @param key unique in the backup
     * @param save returns the text to save from the state, or null to save nothing
     * @param restore returns the state with the saved text restored
     */
    fun add(key: String, save: S.() -> String?, restore: S.(value: String) -> S) {
        include(savedValueOf(key, save, restore))
    }

    /**
     * Saves a value of type [T] with [key], converted to text with [codec].
     *
     * @param key unique in the backup
     * @param save returns the value to save from the state, or null to save nothing
     * @param restore returns the state with the saved value restored
     */
    fun <T> add(key: String, codec: EmaBackupCodec<T>, save: S.() -> T?, restore: S.(value: T) -> S) {
        include(savedValueOf(key, codec, save, restore))
    }
}
