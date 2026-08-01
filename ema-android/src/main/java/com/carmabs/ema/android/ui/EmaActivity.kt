package com.carmabs.ema.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.carmabs.ema.android.base.EmaCoreActivity
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel

/**
 *
 * Abstract base class to implement Kodein framework in activity context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaActivity<B : ViewBinding, S : EmaState, VM : EmaViewModel<S,E>, E : EmaEvent> :
    EmaCoreActivity<S, VM, E>() {

    protected lateinit var binding: B

    /**
     * Method to provide the activity ViewBinding class to represent the layout.
     */
    abstract fun createViewBinding(inflater: LayoutInflater): B


    /**
     * The onCreate base will set the view specified in [.getLayout] and will
     * inject dependencies and views.
     *
     */
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = createViewBinding(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * Determines first execution for each one of the state methods. EmaView determines when to set it to false.
     */
    protected var isFirstNormalExecution: Boolean = true
        private set


    final override fun onState(state: S) {
        binding.onState(state)
        isFirstNormalExecution = false
    }


    final override suspend fun onEffect(effect: E) {
        binding.onEffect(effect)
    }

    abstract fun B.onState(data: S)
    protected open suspend fun B.onEffect(effect: E) = Unit
}
