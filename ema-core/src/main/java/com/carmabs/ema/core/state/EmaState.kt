package com.carmabs.ema.core.state

/**
 * Class to represent basic view states
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a> *
 * @constructor T is the state model of the view, data represents the current state of the view
 */

sealed class EmaState<T> private constructor(val data: T) : EmaDataState {

    /**
     * State that represents the current state of a view.
     * @constructor T is the state model of the view
     */
    class Normal<T>(data: T) : EmaState<T>(data = data)

    /**
     * State that represents an overlapped state of a view.
     * @constructor T is the state model of the view, data represents the current state of the view, dataOverlated represents extra data to handle the overlapped state
     */
    class Overlapped<T>(data: T, val dataOverlapped: EmaExtraData = EmaExtraData()) : EmaState<T>(data)
}