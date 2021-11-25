package com.carmabs.ema.presentation.ui.unlogged.fragment

import android.os.Bundle
import android.view.View
import com.carmabs.ema.R
import com.carmabs.ema.android.delegates.emaViewModelSharedDelegate
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.presentation.base.BaseFragment
import com.carmabs.ema.presentation.ui.unlogged.EmaAndroidUnloggedToolbarViewModel
import com.carmabs.ema.presentation.ui.unlogged.EmaAndroidUnloggedViewModel
import com.carmabs.ema.presentation.ui.unlogged.EmaUnloggedNavigator
import com.carmabs.ema.presentation.ui.unlogged.EmaUnloggedState
import com.carmabs.ema.presentation.ui.unlogged.EmaUnloggedToolbarState
import com.carmabs.ema.presentation.ui.unlogged.EmaUnloggedViewModel
import com.carmabs.ema.presentation.ui.unlogged.activity.EmaUnloggedToolbarViewActivity
import kotlinx.android.synthetic.main.fragment_error.*
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * Fragment for unlogged toolbar
 *
 *
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

class EmaUnloggedViewFragment :
    BaseFragment<EmaUnloggedState, EmaUnloggedViewModel, EmaUnloggedNavigator.Navigation>() {

    /**
     * If you wouldn't want to use dependency injection you can provide it instantiating the class.
     * Not recommended
     */
    private val toolbarViewModel: EmaAndroidUnloggedToolbarViewModel by emaViewModelSharedDelegate(
        {
            di.direct.instance()
        }
    ) {
        vm.onToolbarUpdated(it as EmaState<EmaUnloggedToolbarState>)
    }

    override
    val navigator: EmaUnloggedNavigator by instance()

    override
    val androidViewModelSeed: EmaAndroidViewModel<EmaUnloggedViewModel> = EmaAndroidUnloggedViewModel(vm)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        setupButtons(vm)
    }

    override fun onStart() {
        super.onStart()
        vm.toolbarViewModel = toolbarViewModel.emaViewModel
    }

    private fun setupButtons(viewModel: EmaUnloggedViewModel) {
        bErrorToolbar.setOnClickListener { viewModel.onActionToolbar() }
        bErrorAddUser.setOnClickListener { viewModel.onActionAddUser() }
    }

    override fun onNormal(data: EmaUnloggedState) {
        checkShowToolbarTriggerVisibility(data)
    }

    private fun checkShowToolbarTriggerVisibility(data: EmaUnloggedState) {
        bErrorToolbar.visibility =
            if (data.showToolbarViewVisibility) View.VISIBLE else View.GONE
    }

    override fun onAlternative(data: EmaExtraData) {
    }

    override fun onSingle(data: EmaExtraData) {
    }

    override fun onError(error: Throwable) {
    }

    override val layoutId: Int = R.layout.fragment_error
}