package com.carmabs.ema.presentation.ui.error

import android.view.View
import com.carmabs.ema.R
import com.carmabs.ema.android.navigation.EmaNavigator
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseFragment
import com.carmabs.ema.presentation.ui.home.EmaHomeNavigator
import kotlinx.android.synthetic.main.fragment_error.*
import org.kodein.di.generic.instance

class EmaErrorViewFragment : BaseFragment<EmaErrorState, EmaErrorViewModel, EmaHomeNavigator.Navigation>() {

    override val inputStateKey: String? = null

    override val viewModelSeed: EmaErrorViewModel by instance()

    private val toolbarViewModelSeed: EmaToolbarViewModel by instance()

    override val navigator: EmaNavigator<EmaHomeNavigator.Navigation> by instance<EmaHomeNavigator>()

    override fun onInitialized(viewModel: EmaErrorViewModel) {
        viewModel.toolbarViewModel = addExtraViewModel(toolbarViewModelSeed, this, requireActivity()) {
            viewModel.onToolbarUpdated(it)
        }
        setupButtons(viewModel)
    }

    private fun setupButtons(viewModel: EmaErrorViewModel) {
        bErrorToolbar.setOnClickListener { viewModel.onActionToolbar() }
    }

    override fun onNormal(data: EmaErrorState) {
        checkShowToolbarTriggerVisibility(data)
    }

    private fun checkShowToolbarTriggerVisibility(data: EmaErrorState) {
        bErrorToolbar.visibility = if (data.showToolbarViewVisibility) View.VISIBLE else View.GONE
    }

    override fun onLoading(data: EmaExtraData) {
    }

    override fun onSingle(data: EmaExtraData) {
    }

    override fun onError(error: Throwable) {
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_error
}