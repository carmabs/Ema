package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.state.EmaState
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

/**
 * Created by Carlos Mateo Benito on 16/10/2026.
 *
 * Block of setup that installs the plugins of the source: with [add], or with `+`. Each plugin adds its pieces to the
 * setup as soon as it is added, so they are kept in order with the ones added before and after the block. The type of
 * the actions of each plugin is taken from its type, so the source knows it receives them.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@EmaSourceDsl
class PluginSetupScope<S : EmaState> internal constructor(
    private val guard: SetupGuard,
    private val install: (plugin: EmaSourcePlugin<S, *>, actionType: KClass<out EmaAction>?) -> Unit
) {

    /**
     * Implementation of the inline add with the type of the actions of the plugin.
     */
    @PublishedApi
    internal fun <P : EmaAction> add(plugin: EmaSourcePlugin<S, P>, actionType: KClass<P>?) {
        guard.checkNotBuilt()
        install(plugin, actionType)
    }

    /**
     * Installs [plugin], a feature that the source doesn't know, like an EmaBackup. The source receives the actions of
     * type [P] that it dispatches.
     */
    inline fun <reified P : EmaAction> add(plugin: EmaSourcePlugin<S, P>) {
        add(plugin, P::class)
    }

    /**
     * Installs [plugin], which doesn't dispatch actions.
     */
    @JvmName("addWithoutActions")
    fun add(plugin: EmaSourcePlugin<S, Nothing>) {
        add(plugin, null)
    }

    /**
     * Installs a plugin. The source receives the actions of type [P] that it dispatches.
     */
    inline operator fun <reified P : EmaAction> EmaSourcePlugin<S, P>.unaryPlus() {
        add(this, P::class)
    }

    /**
     * Installs a plugin that doesn't dispatch actions.
     */
    @JvmName("unaryPlusWithoutActions")
    operator fun EmaSourcePlugin<S, Nothing>.unaryPlus() {
        add(this, null)
    }
}
