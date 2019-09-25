package com.carmabs.ema.presentation.ui.user

import androidx.recyclerview.widget.LinearLayoutManager
import com.carmabs.ema.R
import com.carmabs.ema.android.navigation.EmaNavigator
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_user.*


class EmaUserFragment : BaseFragment<EmaUserState, EmaUserViewModel, EmaNavigationState>() {

    override val navigator: EmaNavigator<EmaNavigationState>? = null

    override val inputStateKey: String = EmaUserState::class.java.name

    private var adapter:EmaUserAdapter = EmaUserAdapter()

    override fun onInitialized(viewModel: EmaUserViewModel) {
        setupRecycler(viewModel)

    }

    private fun setupRecycler(viewModel: EmaUserViewModel) {
        rvUser.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        rvUser.adapter = adapter
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_user

    override val viewModelSeed: EmaUserViewModel = EmaUserViewModel()

    override fun onNormal(data: EmaUserState) {
        tvUserName.text = data.name
        tvUserSurname.text = data.surname
        setupRecyclerList(data)
    }

    private fun setupRecyclerList(data: EmaUserState) {
        adapter.updateList(data.itemList)
    }

    override fun onLoading(data: EmaExtraData) {
    }

    override fun onSingle(data: EmaExtraData) {
    }

    override fun onError(error: Throwable) {
    }
}
