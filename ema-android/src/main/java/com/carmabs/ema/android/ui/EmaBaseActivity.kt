package com.carmabs.ema.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.carmabs.ema.android.di.Injector
import org.kodein.di.DI
import org.kodein.di.android.closestDI

/**
 *
 * Abstract base class to implement Kodein framework in activity context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaBaseActivity<B:ViewBinding> : AppCompatActivity(), Injector {

    final override val parentKodein by closestDI()

    protected lateinit var binding:B

    override val di: DI by lazy {
        injectKodein()
    }

    /**
     * Method to provide the activity ViewBinding class to represent the layout.
     */
    abstract fun createViewBinding(inflater: LayoutInflater): B

    final override fun injectModule(kodeinBuilder: DI.MainBuilder): DI.Module? =
        injectActivityModule(kodeinBuilder)

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
     * The child classes implement this methods to return the module that provides the activity scope objects
     * @param kodein The kodein object which provide the injection
     * @return The Kodein module which makes the injection
     */
    abstract fun injectActivityModule(kodein: DI.MainBuilder): DI.Module?
}