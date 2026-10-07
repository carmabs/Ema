package com.carmabs.ema.core.viewmodel

import com.carmabs.ema.core.Ema
import com.carmabs.ema.core.action.DefaultEmaEventDispatcher
import com.carmabs.ema.core.action.EmaEventDispatcher
import com.carmabs.ema.core.broadcast.BackBroadcastId
import com.carmabs.ema.core.broadcast.backBroadcastId
import com.carmabs.ema.core.concurrency.EmaMainScope
import com.carmabs.ema.core.extension.checkNull
import com.carmabs.ema.core.extension.emaName
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.model.EmaConfiguration
import com.carmabs.ema.core.model.EmaFunctionResultHandler
import com.carmabs.ema.core.model.EmaSideEffectConfig
import com.carmabs.ema.core.model.reflection.EmaReflection
import com.carmabs.ema.core.model.reflection.EmaReflectionData
import com.carmabs.ema.core.model.reflection.EmaReflectionException
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * View model to handle view states.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaViewModelBasic<S : EmaState, E : EmaEvent>(
    initialDataState: S,
    defaultScope: CoroutineScope = EmaMainScope()
) : EmaViewModel<S, E> {

    private val emaEventDispatcher = DefaultEmaEventDispatcher<E>()
    private val singleSideEffectMap by lazy {
        hashMapOf<String, Job>()
    }

    /**
     * Ema configuration
     */
    private val config: EmaConfiguration = Ema.configuration

    /**
     * SideEffect configuration
     */
    private val sideEffectConfig = config.sideEffectConfig

    /**
     * The scope where coroutines will be launched by default.
     */
    protected var scope: CoroutineScope = defaultScope
        private set

    /**
     * To determine if the view must be updated when view model is created automatically
     */
    protected open val updateOnInitialization: Boolean = true

    /**
     * Used to know if state has been updated at least once
     */
    private var hasBeenUpdated = false

    /**
     * Used to know if subscribed view should render the state
     */
    final override val shouldRenderState: Boolean
        get() {
            return updateOnInitialization || hasBeenUpdated
        }

    final override fun setScope(scope: CoroutineScope) {
        this.scope = scope
    }

    /**
     * Observable state that launch event every time a value is set. This value will be the state
     * of the view.
     */
    private val mStateFlow: MutableStateFlow<S> = MutableStateFlow(initialDataState)

    final override val eventFlow: Flow<List<E>> = emaEventDispatcher.eventFlow

    final override fun consumeEvent(event: E) = emaEventDispatcher.consumeEvent(event)

    private var firstTimeResumed = true

    /**
     * Determine if the viewmodel has initialized its state
     */

    protected var hasBeenInitialized: Boolean = false
        private set

    override fun onCreated(initializer: EmaInitializer?) {
        if (!hasBeenInitialized) {
            hasBeenInitialized = true
            if (updateOnInitialization) {
                mStateFlow.tryEmit(state)
            }
            onStateCreated(initializer)
            onBroadcastListenerSetup()
        }
    }

    final override fun onStartView() {
        onViewStarted()
    }

    /**
     * Called when view is shown in foreground
     */
    final override fun onResumeView() {
        onViewResumed()
        firstTimeResumed = false
    }

    /**
     * Called when view is hidden in background
     */
    final override fun onPauseView() {
        onViewPaused()
    }

    final override fun onStopView() {
        onViewStopped()
    }

    /**
     * Called when the state of the view has been created
     */
    abstract fun onStateCreated(initializer: EmaInitializer? = null)

    /**
     * Called always the view goes to the foreground
     */
    protected open fun onViewResumed() = Unit

    /**
     * Called always the view is not fully visible
     */
    protected open fun onViewPaused() = Unit

    /**
     * Called when the view is visible to the user
     */
    protected open fun onViewStarted() = Unit

    /**
     * Called always the view goes to the background
     */
    protected open fun onViewStopped() = Unit

    final override val stateFlow: StateFlow<S> = mStateFlow.asStateFlow()

    /**
     * When a background task must be executed for data retrieving or other background job, it must
     * be called through this method with [action] function
     * @param action is the function that will be executed in background
     * @param dispatcher where the useCase is launched by default
     * @param logName name reported as method name in [EmaReflection] for the default actions of
     * [EmaSideEffectConfig]. If it is null, [EmaSideEffectConfig.methodNameResolver] is used
     * @return The EmaUseCaseResult where you can handle the result with the methods
     * - onSuccess when the result of action function is successful
     * - onError when the action function has thrown an error
     * - onFinish when the action function has ended, independently if an error has been thrown
     * - job returns the job where the action function has been executed
     */
    protected fun <T> sideEffect(
        dispatcher: CoroutineContext = this.scope.coroutineContext,
        throwException: Boolean = shouldThrowException(),
        logName: String? = null,
        action: suspend CoroutineScope.() -> T
    ): EmaFunctionResultHandler<T> = generateResultHandler(dispatcher, throwException, logName, action)

    private fun <T> generateResultHandler(
        dispatcher: CoroutineContext,
        throwException: Boolean,
        logName: String?,
        action: suspend CoroutineScope.() -> T
    ) = EmaFunctionResultHandler(
        scope = scope,
        dispatcher = dispatcher,
        onAction = action,
        throwExceptions = throwException,
        successDefaultAction = sideEffectConfig.defaultSuccessAction?.let { success ->
            {
                success.invoke(EmaReflectionData(generateEmaReflection(action, logName), it))
            }
        },
        errorDefaultAction = when (val exceptionPolicy = sideEffectConfig.exceptionPolicy) {
            is EmaSideEffectConfig.ExceptionPolicy.CatchExceptions -> {
                {
                    exceptionPolicy.defaultAction?.invoke(
                        EmaReflectionException(
                            generateEmaReflection(
                                action,
                                logName
                            ),
                            it
                        )
                    )
                }
            }

            EmaSideEffectConfig.ExceptionPolicy.ThrowExceptions -> null
        },
        finishDefaultAction = sideEffectConfig.defaultFinishAction?.let {
            {
                it.invoke(generateEmaReflection(action, logName))
            }
        }
    )

    private fun <T> generateEmaReflection(action: suspend CoroutineScope.() -> T, logName: String?): EmaReflection =
        EmaReflection(
            containerClassName = this::class.simpleName.checkNull(),
            containerClassQualifiedName = this::class.emaName,
            methodName = logName ?: sideEffectConfig.methodNameResolver.resolve(this, action)
        )

    protected fun <T> singleSideEffect(
        id: String,
        dispatcher: CoroutineContext = this.scope.coroutineContext,
        throwException: Boolean = shouldThrowException(),
        logName: String? = null,
        action: suspend CoroutineScope.() -> T
    ): EmaFunctionResultHandler<T> {
        singleSideEffectMap[id]?.cancel()
        val handler = generateResultHandler(dispatcher, throwException, logName, action)
        singleSideEffectMap[id] = handler.job
        return handler
    }

    private fun shouldThrowException() =
        sideEffectConfig.exceptionPolicy is EmaSideEffectConfig.ExceptionPolicy.ThrowExceptions

    /**
     * Method to override onCleared ViewModel method
     */
    protected open fun onDestroy() = Unit

    /**
     * Normal state content of the view
     */
    final override val initialState: S = initialDataState

    /**
     * The state of the view.
     */
    protected var state: S = initialState
        private set

    private val emaResultHandler: EmaResultHandler = EmaResultHandler.getInstance()

    /**
     * Here should implement the listener for result data from other views through [registerBackBroadcastListener] method
     */
    protected open fun onBroadcastListenerSetup() = Unit

    /**
     * Update the current state and update the normal view state by default
     * @param changeStateFunction create the new state
     */
    protected fun updateState(changeStateFunction: S.() -> S) {
        state = state.changeStateFunction()
        hasBeenUpdated = true
        mStateFlow.value = state
    }

    /**
     * Dispatches a one-shot event to be consumed by the view
     * @param event The event to be dispatched.
     * @param allowDuplicated If true, allows the same event to be dispatched multiple times before being consumed.
     */
    protected fun postEvent(event: E, allowDuplicated: Boolean = false) {
        emaEventDispatcher.postEvent(event, allowDuplicated)
    }

    /**
     * Set a result for previous view when the current one is destroyed
     */
    protected fun dispatchBroadcast(data: Any?) {
        emaResultHandler.addResult(
            EmaResultModel(
                key = this::class.backBroadcastId.id,
                ownerId = id,
                data = data
            )
        )
    }

    /**
     * Set the listener for back data when the result view is destroyed. To select the resultId use the EmaViewModel::class.resultId() method
     * of the selected implementation of EmaViewModel whose result is required. Example SampleEmaViewModel::class.resultId()
     */
    protected fun registerBackBroadcastListener(backBroadcastId: BackBroadcastId, receiver: (Any?) -> Unit) {
        emaResultHandler.addResultReceiver(
            EmaReceiverModel(
                resultKey = backBroadcastId.id,
                ownerId = id,
                function = receiver
            )
        )
        emaResultHandler.notifyPendingResults(id, backBroadcastId)
    }

    /**
     * Method called when the ViewModel is destroyed. It cancels all background pending tasks.
     * Check call name for EmaAndroidView. It uses reflection to call this internal method
     */
    final override fun onCleared() {
        emaResultHandler.notifyResults(id)
        emaResultHandler.removeResultListener(id)
        scope.cancel()
        onDestroy()
    }
}
