package com.carmabs.emanate.core.error

import com.carmabs.emanate.core.model.EmaConfiguration

/**
 * Writes [error] to the logger of the configuration and, with [EmaErrorPolicy.Catch], gives it to its onError
 */
internal fun EmaConfiguration.reportError(error: EmaError) {
    logger?.log(
        error.reflection.containerClassName,
        "${error.reflection.methodName} failed: ${error.exception.stackTraceToString()}"
    )
    (errorPolicy as? EmaErrorPolicy.Catch)?.onError?.invoke(error)
}
