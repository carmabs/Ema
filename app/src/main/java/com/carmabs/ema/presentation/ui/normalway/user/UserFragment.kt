package com.carmabs.ema.presentation.ui.normalway.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.carmabs.ema.domain.model.User
import kotlinx.android.synthetic.main.fragment_user.*
import com.carmabs.ema.R


class UserFragment : Fragment() {

    private lateinit var viewModel: UserViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        viewModel.user.observe(this, Observer(::onObserveUser))
        val user = arguments?.getSerializable("USER") as? User
        viewModel.onStart(user)
    }

    private fun onObserveUser(user: User) {
        tvUserName.text = user.name
        tvUserSurname.text = user.surname
    }

    override fun onDestroyView() {
        viewModel.user.removeObservers(this)
        super.onDestroyView()
    }
}
