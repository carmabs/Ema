package com.carmabs.ema.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.carmabs.domain.model.Role
import com.carmabs.domain.model.User
import com.carmabs.ema.android.di.injectDirect
import com.carmabs.ema.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.ema.android.ui.EmaFragment
import com.carmabs.ema.android.ui.recycler.EmaBaseRecyclerAdapter
import com.carmabs.ema.core.navigator.EmaNavigator
import com.carmabs.ema.presentation.extension.fullNameOf
import com.carmabs.ema.presentation.extension.initialsOf
import com.carmabs.ema.presentation.home.HomeAction
import com.carmabs.ema.presentation.home.HomeEvent
import com.carmabs.ema.presentation.home.HomeInitializer
import com.carmabs.ema.presentation.home.HomeState
import com.carmabs.ema.presentation.home.HomeViewModel
import com.carmabs.ema.sample.ema.R
import com.carmabs.ema.sample.ema.databinding.HomeFragmentBinding


class HomeFragment :
    EmaFragment<HomeFragmentBinding, HomeState, HomeViewModel, HomeEvent>() {

    private var adapter: EmaBaseRecyclerAdapter<User>? = null
    override val initializerStrategy: BundleSerializerStrategy
        get() = BundleSerializerStrategy.kSerialization(HomeInitializer.serializer())

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): HomeFragmentBinding {
        return HomeFragmentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupListeners()
        binding.rvHomeUsers.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun HomeFragmentBinding.setupListeners() {
        fabHomeCreateProfile.setOnClickListener {
            viewModel.dispatch(HomeAction.ProfileClicked)
        }
    }


    override fun provideViewModel(): HomeViewModel {
        return injectDirect()
    }

    override fun HomeFragmentBinding.onState(state: HomeState) {
        state.userData?.also { onUserData(it) }

        if (state.showUserList) {
            if (rvHomeUsers.adapter == null) {
                adapter = adapter ?: when (state.userData?.role) {
                    Role.ADMIN -> HomeMultiAdapter()
                    Role.BASIC -> HomeSingleAdapter()
                    null -> null
                }
                rvHomeUsers.adapter = adapter
            }
            adapter?.submitList(state.userList)
        }
        llHomeEmpty.isVisible = state.showEmptyList
        fabHomeCreateProfile.isVisible = state.showCreateButton
    }

    private fun HomeFragmentBinding.onUserData(userData: HomeState.UserData) {
        val isAdmin = userData.role == Role.ADMIN
        tvHomeGreeting.text = getString(R.string.home_greeting, userData.name)
        tvHomeSubtitle.setText(
            if (isAdmin) R.string.home_subtitle_admin else R.string.home_subtitle_basic
        )
        tvHomeProfileAvatar.text = initialsOf(userData.name, userData.surname)
        tvHomeProfileName.text = fullNameOf(userData.name, userData.surname)
        tvHomeProfileRole.setText(if (isAdmin) R.string.role_admin else R.string.role_basic)
        tvHomeListTitle.setText(
            if (isAdmin) R.string.home_section_admin else R.string.home_section_basic
        )
        cvHomeProfile.isVisible = true
    }

    override suspend fun HomeFragmentBinding.onEvent(event: HomeEvent) {
        navigate(event)
    }

    override val navigator: EmaNavigator<HomeEvent> = HomeNavigator(this)
}
