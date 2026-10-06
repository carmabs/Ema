package com.carmabs.ema.core.state

/**
 * Interface to represent basic view states
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class EmaStateDsl

@EmaStateDsl
interface EmaState {
    object EMPTY : EmaState
    fun checkIsValidStateDataClass() = this is EMPTY || this::class.isData
}
