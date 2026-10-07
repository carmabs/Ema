package com.carmabs.ema.core.model

/**
 * Resolves the name of the method that launched a sideEffect, used in [com.carmabs.ema.core.model.reflection.EmaReflection].
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
fun interface EmaMethodNameResolver {

    /**
     * @param container the object that launched the sideEffect, usually the ViewModel
     * @param action the lambda executed by the sideEffect
     */
    fun resolve(container: Any, action: Any): String

    companion object {
        val Default = EmaMethodNameResolver { container, _ -> "Method in ${container::class.simpleName}" }
    }
}
