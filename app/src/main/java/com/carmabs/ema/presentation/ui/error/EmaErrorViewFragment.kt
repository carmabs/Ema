package com.carmabs.ema.presentation.ui.error

import android.os.Bundle
import android.view.View
import com.carmabs.ema.R
import com.carmabs.ema.android.delegates.emaViewModelSharedDelegate
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_error.*
import org.kodein.di.generic.instance

class EmaErrorViewFragment : BaseFragment<EmaErrorState, EmaErrorViewModel, EmaErrorNavigator.Navigation>() {

    /**
     * If you wouldn't want to use dependency injection you can provide it instantiating the class.
     * Not recommended
     */
    private val toolbarSeed: EmaAndroidErrorToolbarViewModel by instance()

    private val toolbarViewModel: EmaAndroidErrorToolbarViewModel by emaViewModelSharedDelegate(
            {
                vm.onToolbarUpdated(it)
            },
            {
                toolbarSeed
            }
    )

    override val navigator: EmaErrorNavigator by instance()

    override val androidViewModelSeed: EmaAndroidViewModel<EmaErrorViewModel> by instance<EmaAndroidErrorViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupButtons(vm)
    }

    override fun onStart() {
        super.onStart()
        vm.toolbarViewModel = toolbarViewModel.emaViewModel
    }

    private fun setupButtons(viewModel: EmaErrorViewModel) {
        bErrorToolbar.setOnClickListener { viewModel.onActionToolbar() }
        bErrorAddUser.setOnClickListener { viewModel.onActionAddUser() }
    }

    override fun onNormal(data: EmaErrorState) {
        checkShowToolbarTriggerVisibility(data)
    }

    private fun checkShowToolbarTriggerVisibility(data: EmaErrorState) {
        bErrorToolbar.visibility = if (data.showToolbarViewVisibility) View.VISIBLE else View.GONE
    }

    override fun onAlternative(data: EmaExtraData) {
    }

    override fun onSingle(data: EmaExtraData) {
    }

    override fun onError(error: Throwable) {
    }

    override
    val layoutId: Int = R.layout.fragment_error
}