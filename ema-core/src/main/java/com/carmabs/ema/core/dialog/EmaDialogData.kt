package com.carmabs.ema.core.dialog

import java.io.Serializable

/**
 * Interface to handle UI dialog data
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

interface EmaDialogData : Serializable {
    val proportionWidth: Float?
    val proportionHeight: Float?
}