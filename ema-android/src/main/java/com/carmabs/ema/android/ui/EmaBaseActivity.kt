package com.carmabs.ema.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module

/**
 *
 * Abstract base class to implement Kodein framework in activity context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaBaseActivity<B:ViewBinding> : AppCompatActivity() {

    protected lateinit var binding:B

    private var activityKoinModule:Module? = null
    /**
     * Method to provide the activity ViewBinding class to represent the layout.
     */
    abstract fun createViewBinding(inflater: LayoutInflater): B

    /**
     * The child classes implement this methods to return the module that provides the activity scope objects
     * @return The koin module which makes the injection
     */
    abstract fun injectActivityModule(): Module?

    /**
     * The onCreate base will set the view specified in [.getLayout] and will
     * inject dependencies and views.
     *
     */
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityKoinModule = injectActivityModule()?.also {
            loadKoinModules(it)
        }

        binding = createViewBinding(layoutInflater)
        setContentView(binding.root)
    }

    @CallSuper
    override fun onDestroy() {
        activityKoinModule?.also {
            unloadKoinModules(it)
        }
        activityKoinModule = null
        super.onDestroy()
    }
}