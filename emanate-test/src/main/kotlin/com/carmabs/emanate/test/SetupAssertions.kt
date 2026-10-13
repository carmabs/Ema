package com.carmabs.emanate.test

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.savestate.EmaRestoreState
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModel
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Checks that every middleware, reducer and side effect of the view model can receive the actions of its type: actions
 * of the screen, initializers, lifecycle actions, the restore of the saved state or broadcast actions it listens to. A
 * piece for a broadcast action that isn't listened to in the broadcasts block never runs, and nothing fails at runtime,
 * so this assertion catches a forgotten listen. Only the types that the view model can't receive for sure are
 * reported: two interfaces may be implemented by the same action, so they are considered compatible.
 *
 * @throws AssertionError with every piece that never runs
 */
fun <S : EmaState, A : EmaAction, E : EmaEvent> EmaViewModel<S, A, E>.assertSetup() {
    val setup = sourceSetup
    val listened = setup.broadcastListeners.map { it.java }
    val received = listOf(
        actionClass(),
        EmaAction.Initializer::class.java,
        EmaAction.Lifecycle::class.java,
        EmaRestoreState::class.java
    ) + listened
    val pieces = setup.middlewares.map { "middleware" to it.actionType } +
        setup.reducers.map { "reducer" to it.actionType } +
        setup.sideEffects.map { "side effect" to it.actionType }
    val neverRun = pieces.filter { (_, actionType) -> received.none { canBeBoth(it, actionType.java) } }
    if (neverRun.isEmpty()) return
    val viewModelName = this::class.simpleName
    throw AssertionError(
        neverRun.joinToString(separator = "\n") { (piece, actionType) ->
            neverRunMessage(viewModelName, piece, actionType)
        }
    )
}

private fun neverRunMessage(viewModelName: String?, piece: String, actionType: KClass<*>): String {
    val name = actionType.simpleName
    return "The $piece of $name never runs: $viewModelName doesn't receive $name. If it is broadcast by another " +
        "source, add listen<$name>() in the broadcasts block."
}

/**
 * Whether an action can be of both types. Two classes can only if one extends the other, but an interface can be
 * implemented by any class that isn't final.
 */
private fun canBeBoth(first: Class<*>, second: Class<*>): Boolean {
    if (first.isAssignableFrom(second) || second.isAssignableFrom(first)) return true
    return (first.isInterface && !Modifier.isFinal(second.modifiers)) ||
        (second.isInterface && !Modifier.isFinal(first.modifiers))
}

/**
 * The class of the actions of the screen, the type argument A of EmaViewModel, resolved through the classes between
 * the view model and EmaViewModel. If it can't be resolved, EmaAction, so nothing is reported by mistake.
 */
private fun EmaViewModel<*, *, *>.actionClass(): Class<*> {
    var currentClass: Class<*> = this::class.java
    var typeArguments = emptyMap<Type, Type>()
    while (true) {
        val superclass = currentClass.genericSuperclass ?: return EmaAction::class.java
        val rawSuperclass = (superclass as? ParameterizedType)?.rawType as? Class<*> ?: superclass as? Class<*>
            ?: return EmaAction::class.java
        val arguments = (superclass as? ParameterizedType)?.actualTypeArguments?.map { typeArguments[it] ?: it }
        if (rawSuperclass == EmaViewModel::class.java) {
            return arguments?.getOrNull(1)?.toClass() ?: EmaAction::class.java
        }
        typeArguments = arguments?.let { rawSuperclass.typeParameters.zip(it).toMap() } ?: emptyMap()
        currentClass = rawSuperclass
    }
}

private fun Type.toClass(): Class<*>? = when (this) {
    is Class<*> -> this
    is ParameterizedType -> rawType as? Class<*>
    else -> null
}
