package com.carmabs.emanate.core.savestate

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.EmaSourcePlugin
import com.carmabs.emanate.core.source.SetupGuard
import com.carmabs.emanate.core.source.SourceSetupScope
import com.carmabs.emanate.core.source.reducer.reducerOf
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModel

/**
 * Created by Carlos Mateo Benito on 15/10/2026.
 *
 * Parts of the state saved so they are restored after the system kills the process of the app. Save only what the
 * user would lose, like the fields of a form, not the whole state: the platform limits the size of the saved values.
 * Create it with backupOf and install it in the setup of a source, before the reducers that react to
 * [EmaRestoreState]: it adds the reducer that restores the values, so the source doesn't know them.
 *
 * An [EmaStateSaver] saves the values every time they change and restores them. An EmaViewModel creates one with the
 * backups installed in its setup and the store of its platform: on Android, the SavedStateHandle of the screen.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaBackup<S : EmaState> internal constructor(internal val values: List<EmaBackupValue<S, *>>) :
    EmaSourcePlugin<S> {

    /**
     * Restores the values of [EmaRestoreState] that belong to this backup, in the order they were added. A value that
     * can't be decoded isn't restored.
     */
    private val restoreReducer = reducerOf<S, EmaRestoreState>(EmaRestoreState::class) { action ->
        values.fold(state) { restoredState, backupValue ->
            action.values[backupValue.key]?.let { backupValue.restore(restoredState, it) } ?: restoredState
        }
    }

    override fun install(setup: SourceSetupScope<S, *, *>) {
        setup.reducers { +restoreReducer }
    }

    private fun <T> EmaBackupValue<S, T>.restore(state: S, value: String): S =
        codec.decode(value)?.let { restore(state, it) } ?: state
}

/**
 * Creates a backup with the values added in [block].
 *
 * @throws IllegalArgumentException if a key is added twice
 */
fun <S : EmaState> backupOf(block: BackupSetupScope<S>.() -> Unit): EmaBackup<S> {
    val guard = SetupGuard()
    val scope = BackupSetupScope<S>(guard).apply(block)
    guard.build()
    return EmaBackup(scope.backupValueList)
}

/**
 * Creates a backup of the state of the view model with the values added in [block].
 *
 * @throws IllegalArgumentException if a key is added twice
 */
fun <S : EmaState, A : EmaAction, E : EmaEvent> EmaViewModel<S, A, E>.backupOf(
    block: BackupSetupScope<S>.() -> Unit
): EmaBackup<S> = com.carmabs.emanate.core.savestate.backupOf(block)
