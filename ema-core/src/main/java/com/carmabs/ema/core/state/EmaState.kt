package com.carmabs.ema.core.state

/**
 * Class to represent basic view states
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a> *
 * @constructor T is the state model of the view, data represents the current state of the view
 */

sealed class EmaState<T>(val data: T) : EmaBaseState {

    /**
     * State that represents the current state of a view.
     * @constructor T is the state model of the view
     */
    class Normal<T>(data: T) : EmaState<T>(data = data)

    /**
     * State that represents an overlayed state of a view.
     * @constructor T is the state model of the view, data represents the current state of the view, dataOverlated represents extra data to handle the overlayed state
     */
    class Overlayed<T>(data: T, val dataOverlayed: EmaExtraData = EmaExtraData()) : EmaState<T>(data)

    /**
     * State that represents an error state of a view
     * @constructor T is the state model of the view, data represents the current state of the view, error represents the error who produced this state
     */
    class Error<T>(data: T, val error: Throwable) : EmaState<T>(data)
}