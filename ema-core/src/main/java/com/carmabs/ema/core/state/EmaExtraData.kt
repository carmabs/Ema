package com.carmabs.ema.core.state

/**
 * Class to handle extra data used for single events, loading states.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

data class EmaExtraData(val type: Int = DEFAULT_ID, val extraData: Any? = null) : EmaDataState
{
    companion object{
       const val DEFAULT_ID = -1
    }
}