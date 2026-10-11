package com.carmabs.emanate.core.model

import com.carmabs.emanate.core.Ema
import com.carmabs.emanate.core.EmaInternalApi
import com.carmabs.emanate.core.extension.checkNull
import com.carmabs.emanate.core.extension.emaName
import com.carmabs.emanate.core.model.reflection.Reflection
import com.carmabs.emanate.core.model.reflection.ReflectionData
import com.carmabs.emanate.core.model.reflection.ReflectionException
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope

/**
 * Launches [action] applying the [EmaSideEffectConfig]: its default actions and its exception policy. It is the side
 * effect of the ViewModels, shared with other Ema libraries.
 *
 * @param container class that launches the side effect. It is reported in [Reflection]
 * @param scope where the side effect is launched
 * @param dispatcher where the side effect runs
 * @param logName name reported as method name in [Reflection]. If it is null,
 * [EmaSideEffectConfig.methodNameResolver] is used
 * @param sideEffectConfig configuration applied
 * @param throwException if true, the exceptions are thrown instead of being handled
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@EmaInternalApi
fun <T> emaSideEffect(
    container: Any,
    scope: CoroutineScope,
    dispatcher: CoroutineContext = scope.coroutineContext,
    logName: String? = null,
    sideEffectConfig: EmaSideEffectConfig = Ema.configuration.sideEffectConfig,
    throwException: Boolean = sideEffectConfig.exceptionPolicy is EmaSideEffectConfig.ExceptionPolicy.ThrowExceptions,
    action: suspend CoroutineScope.() -> T
): SideEffectResult<T> {
    val reflection: () -> Reflection = {
        Reflection(
            containerClassName = container::class.simpleName.checkNull(),
            containerClassQualifiedName = container::class.emaName,
            methodName = logName ?: sideEffectConfig.methodNameResolver.resolve(container, action)
        )
    }
    return SideEffectResult(
        scope = scope,
        dispatcher = dispatcher,
        onAction = action,
        throwExceptions = throwException,
        successDefaultAction = sideEffectConfig.defaultSuccessAction?.let { success ->
            { success(ReflectionData(reflection(), it)) }
        },
        errorDefaultAction = when (val exceptionPolicy = sideEffectConfig.exceptionPolicy) {
            is EmaSideEffectConfig.ExceptionPolicy.CatchExceptions -> {
                { exceptionPolicy.defaultAction?.invoke(ReflectionException(reflection(), it)) }
            }

            EmaSideEffectConfig.ExceptionPolicy.ThrowExceptions -> null
        },
        finishDefaultAction = sideEffectConfig.defaultFinishAction?.let { finish ->
            { finish(reflection()) }
        }
    )
}
