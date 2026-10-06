package com.carmabs.ema.core.state

/**
 * Interface to represent one-shot events that happen in the feature. The receiver decides how to react to them (navigation, messages...)
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface EmaEvent{
    object EMPTY : EmaEvent
}
