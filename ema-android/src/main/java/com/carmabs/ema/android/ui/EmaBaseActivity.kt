package com.carmabs.ema.android.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavHost
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
abstract class EmaBaseActivity : AppCompatActivity(), NavHost, Injector {

    final override val parentKodein by closestDI()

    override val di: DI by lazy {
        injectKodein()
    }

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
        setContentView(layoutId)
    }

    /**
     * @return The layout ID that's gonna be the activity view.
     */
    protected abstract val layoutId: Int

    /**
     * The child classes implement this methods to return the module that provides the activity scope objects
     * @param kodein The kodein object which provide the injection
     * @return The Kodein module which makes the injection
     */
    abstract fun injectActivityModule(kodein: DI.MainBuilder): DI.Module?
}