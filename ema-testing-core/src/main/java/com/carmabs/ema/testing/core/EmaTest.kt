package com.carmabs.ema.testing.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

/**
 * Class for testing. Provide mock initialization.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class EmaTest {

    @Before
    open fun setup() {
        MockitoAnnotations.initMocks(this)
        onSetup()
        runBlocking {
            onSetupSuspend()
        }

    }

    abstract suspend fun onSetupSuspend()

    abstract fun onSetup()


    protected inline fun <reified C, reified R> mockAllMethods(returnObject: R): C {
        return Mockito.mock(C::class.java) {
            if (R::class.java == it.method.returnType) {
                returnObject
            } else {
                Mockito.RETURNS_DEFAULTS.answer(it)
            }
        }
    }

    protected fun onBlocking(func: suspend CoroutineScope.() -> Unit) {
        runBlocking { func.invoke(this) }
    }

    protected fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T


}

fun <T> T.toDeferred() = CompletableDeferred(this)