package com.carmabs.ema.testing.core

import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
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
    fun setup() {
        MockitoAnnotations.openMocks(this)
        runBlocking {
            onSetup()
        }

    }

    @After
    fun finish() {
        onFinish()
    }

    protected open suspend fun onSetup() = Unit

    protected open fun onFinish() = Unit

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