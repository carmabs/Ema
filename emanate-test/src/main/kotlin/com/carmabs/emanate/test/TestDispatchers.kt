package com.carmabs.emanate.test

import com.carmabs.emanate.core.Emanate
import com.carmabs.emanate.core.concurrency.EmaDispatchers
import com.carmabs.emanate.core.model.EmaConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Configuration of Emanate before [useTestDispatchers], restored by [resetTestDispatchers]
 */
private var configurationBeforeTest: EmaConfiguration? = null

/**
 * Runs the view models, sources and use cases created after it on [dispatcher]. Call it before each test, for example in
 * a function annotated with BeforeTest, and [resetTestDispatchers] after it.
 *
 * It also sets [dispatcher] as `Dispatchers.Main`, so runTest uses its scheduler: advanceUntilIdle runs the work of the
 * view models, delays included.
 *
 * @param dispatcher by default the actions are processed as soon as they are dispatched. With a
 * StandardTestDispatcher they are processed when the test advances.
 * @return [dispatcher]
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun useTestDispatchers(dispatcher: TestDispatcher = UnconfinedTestDispatcher()): TestDispatcher {
    if (configurationBeforeTest == null) configurationBeforeTest = Emanate.configuration
    Dispatchers.setMain(dispatcher)
    Emanate.initForTest(
        Emanate.configuration.copy(
            dispatchers = EmaDispatchers(main = dispatcher, io = dispatcher, default = dispatcher)
        )
    )
    return dispatcher
}

/**
 * Undoes [useTestDispatchers]: restores `Dispatchers.Main` and the configuration of Emanate, and removes the actions
 * broadcast with keepUntilReceived that nobody received, so they don't reach the next test. Call it after each test,
 * for example in a function annotated with AfterTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun resetTestDispatchers() {
    Dispatchers.resetMain()
    configurationBeforeTest?.let { Emanate.initForTest(it) }
    configurationBeforeTest = null
    Emanate.actionBroadcaster.clearPendingActions()
}
