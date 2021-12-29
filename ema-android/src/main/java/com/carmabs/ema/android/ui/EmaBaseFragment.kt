package com.carmabs.ema.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
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
abstract class EmaBaseFragment<B : ViewBinding> : Fragment(), Injector {

    private var _binding: B? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding get() = _binding!!

    final override val parentKodein: DI by closestDI()

    final override val di: DI by lazy {
        injectKodein()
    }

    final override fun injectModule(kodeinBuilder: DI.MainBuilder): DI.Module? =
        injectFragmentModule(kodeinBuilder)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createViewBinding(inflater, container)
        return binding.root
    }

    /**
     * Method to provide the fragment ViewBinding class to represent the layout.
     */
    abstract fun createViewBinding(inflater: LayoutInflater,container: ViewGroup?): B

    /**
     * The child classes implement this methods to return the module that provides the fragment scope objects
     * @param kodein The kodein object which provide the injection
     * @return The Kodein module which makes the injection
     */
    abstract fun injectFragmentModule(kodein: DI.MainBuilder): DI.Module?

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}