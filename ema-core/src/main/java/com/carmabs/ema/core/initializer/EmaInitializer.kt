package com.carmabs.ema.core.initializer

import java.io.Serializable

/**
 * Created by Carlos Mateo Benito on 8/8/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface EmaInitializer : Serializable {
    companion object {
        const val KEY = "EmaInitializer"
    }
}