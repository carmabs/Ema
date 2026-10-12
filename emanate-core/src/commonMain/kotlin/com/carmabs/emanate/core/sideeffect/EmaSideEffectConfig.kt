package com.carmabs.emanate.core.sideeffect

import com.carmabs.emanate.core.model.reflection.Reflection
import com.carmabs.emanate.core.model.reflection.ReflectionData
import com.carmabs.emanate.core.model.reflection.ReflectionException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
 * @param methodNameResolver resolves the method name reported in [Reflection] when no logName is provided
 * @param destroyedTimeout time the side effects of Lifecycle.Destroyed in an EmaViewModel can run after it is cleared,
 * unless they have their own timeout. Then they are cancelled, so they don't keep the view model alive.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
data class EmaSideEffectConfig(
    val defaultSuccessAction: ((ReflectionData) -> Unit)? = null,
    val exceptionPolicy: ExceptionPolicy = ExceptionPolicy.CatchExceptions(),
    val defaultFinishAction: ((Reflection) -> Unit)? = null,
    val methodNameResolver: EmaMethodNameResolver = EmaMethodNameResolver.Default,
    val destroyedTimeout: Duration = 5.seconds
) {
    sealed interface ExceptionPolicy {
        data object ThrowExceptions : ExceptionPolicy

        class CatchExceptions(val defaultAction: ((ReflectionException) -> Unit)? = null) : ExceptionPolicy
    }
}
