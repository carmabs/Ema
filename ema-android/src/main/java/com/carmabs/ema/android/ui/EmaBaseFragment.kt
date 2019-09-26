package com.carmabs.ema.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein


/**
 *
 * Abstract base class to implement Kodein framework in fragment context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaBaseFragment : Fragment(), KodeinAware {

    private val parentKodein: Kodein by closestKodein()
    override val kodein: Kodein
        get() = Kodein.lazy {
            extend(parentKodein)
            injectFragmentModule(this)?.let {
                import(it)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layoutRes = getFragmentLayout()
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
    protected abstract fun getFragmentLayout(): Int

    /**
     * The child classes implement this methods to return the module that provides the fragment scope objects
     * @param kodein The kodein object which provide the injection
     * @return The Kodein module which makes the injection
     */
    abstract fun injectFragmentModule(kodein:Kodein.MainBuilder):Kodein.Module?
}