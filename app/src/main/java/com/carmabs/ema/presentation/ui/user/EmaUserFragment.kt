package com.carmabs.ema.presentation.ui.user

import android.widget.Toast
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

    lateinit var adapter: EmaUserAdapter

    override fun onInitialized(viewModel: EmaUserViewModel) {
        setupRecycler(viewModel)

    }

    private fun setupRecycler(viewModel: EmaUserViewModel) {
        adapter = EmaUserAdapter(viewModel)
        rvUser.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
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

        when (data.type) {
            EmaUserViewModel.SINGLE_EVENT_USER -> {
                val itemLeft = data.extraData as? EmaUserLeftModel
                itemLeft?.also {
                    Toast.makeText(
                            requireContext(),
                            String.format(getString(R.string.user_hello_user), itemLeft.name),
                            Toast.LENGTH_SHORT)
                            .show()
                }
            }

            EmaUserViewModel.SINGLE_EVENT_GROUP -> {
                val itemRight = data.extraData as? EmaUserRightModel
                itemRight?.also {
                    Toast.makeText(
                            requireContext(),
                            String.format(getString(R.string.user_hello_group), itemRight.number),
                            Toast.LENGTH_SHORT)
                            .show()

                }
            }
        }
    }

    override fun onError(error: Throwable) {
    }
}
