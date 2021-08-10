package com.carmabs.ema.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.carmabs.ema.android.di.Injector
import org.kodein.di.DI
import org.kodein.di.android.x.closestDI


/**
 *
 * Abstract base class to implement Kodein framework in fragment context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaBaseFragment : Fragment(), Injector {

    final override val parentKodein: DI by closestDI()

    final override val di: DI by lazy {
        injectKodein()
    }

    final override fun injectModule(kodeinBuilder: DI.MainBuilder): DI.Module? =
        injectFragmentModule(kodeinBuilder)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layoutRes = layoutId
        if (layoutRes == 0) {
            throw IllegalArgumentException(
                    "getLayoutRes() returned 0, which is not allowed. "
                            + "If you don't want to use getLayoutRes() but implement your own view for this "
                            + "fragment manually, then you have to override onCreateView();")
        } else {
            return inflater.inflate(layoutRes, container, false)
        }
    }


    /**
     * Specify the layout of the fragment to be inflated in the [EmaBaseFragment.onCreateView]
     */
    protected abstract val layoutId : Int

    /**
     * The child classes implement this methods to return the module that provides the fragment scope objects
     * @param kodein The kodein object which provide the injection
     * @return The Kodein module which makes the injection
     */
    abstract fun injectFragmentModule(kodein:DI.MainBuilder):DI.Module?




}