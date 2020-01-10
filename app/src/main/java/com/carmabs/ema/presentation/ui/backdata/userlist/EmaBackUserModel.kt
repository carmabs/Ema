package com.carmabs.ema.presentation.ui.backdata.userlist

import com.carmabs.ema.core.state.EmaModel

/**
 * <p>
 * Copyright (c) 2019, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 *
 * Date: 2019-11-07
 */

data class EmaBackUserModel(
        val name:String,
        val surname:String
) : EmaModel