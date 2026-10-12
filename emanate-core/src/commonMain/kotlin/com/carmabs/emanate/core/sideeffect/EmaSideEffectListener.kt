package com.carmabs.emanate.core.sideeffect

import com.carmabs.emanate.core.model.reflection.Reflection
import com.carmabs.emanate.core.model.reflection.ReflectionData

/**
 * Created by Carlos Mateo Benito on 12/10/2026.
 *
 * Receives the result of every side effect, set in
 * [com.carmabs.emanate.core.model.EmaConfiguration.sideEffectListener]. Useful to track or measure them. The errors
 * are handled by the error policy of the configuration.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface EmaSideEffectListener {

    /**
     * A side effect has finished successfully, returning [ReflectionData.data]
     */
    fun onSuccess(data: ReflectionData) = Unit

    /**
     * A side effect has finished, with or without error
     */
    fun onFinish(reflection: Reflection) = Unit
}
