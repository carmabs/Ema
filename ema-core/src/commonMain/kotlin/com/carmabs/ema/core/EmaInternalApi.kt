package com.carmabs.ema.core

/**
 * Marks declarations that are public only to be shared between Ema modules. They are not part of the public
 * API and can change without notice.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@RequiresOptIn(
    message = "This is an internal Ema API. It can change without notice.",
    level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR
)
annotation class EmaInternalApi
