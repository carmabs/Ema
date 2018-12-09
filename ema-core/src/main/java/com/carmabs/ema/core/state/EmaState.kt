package com.carmabs.ema.core.state

/**
 * Class to represent basic view states
 *
 * @author <a href="mailto:carlos.mateo@babel.es">Carlos Mateo Benito</a>
 */

sealed class EmaState : EmaBaseState {

    /**
     * State that represents the current state of a view.
     * @constructor T is the state model of the view
     */
    class Normal<T>(val data: T) : EmaState()

    /**
     * State that represents a loading state of a view.
     * @constructor data respresents extra data to handle the loading state
     */
    class Loading(val data: EmaExtraData = EmaExtraData()) : EmaState()

    /**
     * State that represents an error state of a view
     * @constructor error represents the error who produced this state
     */
    class Error(val error: Throwable) : EmaState()
}