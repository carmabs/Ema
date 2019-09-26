package com.carmabs.ema.presentation.ui.error

import com.carmabs.ema.core.state.EmaBaseState

/**
 * State for error toolbar
 *
*
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

data class EmaToolbarState(
        val visibility:Boolean = true
) : EmaBaseState