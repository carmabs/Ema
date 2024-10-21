package com.carmabs.ema.core.model

import com.carmabs.ema.core.model.reflection.EmaReflection
import com.carmabs.ema.core.model.reflection.EmaReflectionData
import com.carmabs.ema.core.model.reflection.EmaReflectionException

/**
 * Created by Carlos Mateo Benito on 22/10/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 * Configuration when sideEffect is launched on ViewModels. Can be used for log/development purposes
 * @param defaultFinishAction called everytime a sideEffect action has been finished, independently of success/fail
 * @param exceptionPolicy called when an error is launched. Can be configured to throw exception or
 * catch automatically, with default action if needed
 * @param defaultSuccessAction called when a sideEffect has been executed successfully.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
data class EmaSideEffectConfig(
    val defaultSuccessAction: ((EmaReflectionData) -> Unit)? = null,
    val exceptionPolicy: ExceptionPolicy = ExceptionPolicy.CatchExceptions(),
    val defaultFinishAction: ((EmaReflection) -> Unit)?=null
) {
    sealed interface ExceptionPolicy {
        data object ThrowExceptions : ExceptionPolicy

        class CatchExceptions(val defaultAction: ((EmaReflectionException) -> Unit)? = null) : ExceptionPolicy
    }
}