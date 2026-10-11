package com.carmabs.ema.core

/**
 * Marks the API meant only for tests, such as replacing the configuration of Ema in each test.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@RequiresOptIn(
    message = "This API is only for tests.",
    level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class EmaTestApi
