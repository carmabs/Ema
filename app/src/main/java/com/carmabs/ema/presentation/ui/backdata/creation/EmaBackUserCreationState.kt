package com.carmabs.ema.presentation.ui.backdata.creation

import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.presentation.STRING_EMPTY

/**
 * <p>
 * Copyright (c) 2019, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 *
 * Date: 2019-11-07
 */

data class EmaBackUserCreationState (
        val name:String = STRING_EMPTY,
        val surname:String = STRING_EMPTY
) : EmaBaseState