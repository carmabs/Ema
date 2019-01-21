package com.carmabs.ema.presentation.ui.normalway.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.carmabs.ema.MockRepository
import com.carmabs.ema.domain.exception.LoginException
import com.carmabs.ema.domain.exception.PasswordEmptyException
import com.carmabs.ema.domain.exception.UserEmptyException
import com.carmabs.ema.domain.model.LoginRequest
import com.carmabs.ema.domain.model.User
import com.carmabs.ema.domain.usecase.LoginUseCase
import com.carmabs.ema.presentation.dialog.DialogProvider
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogData
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogListener
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogProvider
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_password.*
import kotlinx.android.synthetic.main.layout_user.*
import com.carmabs.ema.R
import com.carmabs.ema.presentation.dialog.DialogListener
import com.carmabs.ema.presentation.dialog.loading.LoadingDialogData
import com.carmabs.ema.presentation.dialog.loading.LoadingDialogProvider


class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    private val dialogError: DialogProvider by lazy {
        val dialog = SimpleDialogProvider(fragmentManager!!)
        dialog.setDialogListener(object : SimpleDialogListener {
            override fun onCancelClicked() {
                viewModel.onActionDialogErrorCancel()
            }

            override fun onBackPressed() {
                viewModel.onActionDialogErrorCancel()
            }

            override fun onConfirmClicked() {
                viewModel.onActionDialogErrorAccept()
            }
        })
        dialog
    }

    private val dialogLoading: DialogProvider by lazy {
         LoadingDialogProvider(fragmentManager!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupButtons()
        val factory = HomeFactory(LoginUseCase(MockRepository()))
        viewModel = ViewModelProviders.of(activity!!, factory).get(HomeViewModel::class.java)
        viewModel.login.observe(this, Observer(::onObserveLogin))
        viewModel.error.observe(this, Observer(::onObserveError))
        viewModel.user.observe(this, Observer(::onObserveUser))
        viewModel.loading.observe(this, Observer(::onObserveLoading))
        viewModel.showPassword.observe(this, Observer(::onObserveShowPassword))
        viewModel.rememberUser.observe(this, Observer(::onObserveRememberUser))

        viewModel.onStart()
    }

    private fun onObserveRememberUser(b: Boolean) {
        swLightLoginRememberPassword.isChecked = b
    }

    private fun onObserveShowPassword(show: Boolean?) {
        show?.let {
            if (!show) {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                etPassword.transformationMethod = null
            }
        }
    }

    private fun setupButtons() {

        swLightLoginRememberPassword.setOnCheckedChangeListener { _,isChecked -> viewModel.onActionRemember(isChecked) }
        ivHomeTouchEmptyUser.setOnClickListener { viewModel.onActionDeleteUser() }
        ivHomePassEmptyPassword.setOnClickListener { viewModel.onActionDeletePassword() }
        ivHomePassSeePassword.setOnClickListener { viewModel.onActionShowPassword() }
        etUser.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onActionUserWrite(s?.toString() ?: "")
            }
        })
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onActionPasswordWrite(s?.toString() ?: "")
            }

        })
        bLightLoginSign.setOnClickListener {
            viewModel.onActionLogin(LoginRequest(
                    etUser.text.toString(),
                    etPassword.text.toString()
            ))
        }
    }

    private fun onObserveError(exception: Exception?) {
        exception?.let {
            when (exception) {
                is UserEmptyException -> checkError(tvLightLoginErrorUser)
                is PasswordEmptyException -> checkError(tvLightLoginErrorPassword)
                is LoginException -> showErrorDialog()
            }
        }?:dialogError.hide()
    }

    private fun onObserveLoading(loading: Boolean) {
        showLoading(loading)
    }

    private fun showLoading(show:Boolean){
        if (show)
            dialogLoading.show(
                    LoadingDialogData(
                            "Loading",
                            "Getting user data..."
                    ))
        else
            dialogLoading.hide()
    }

    private fun showErrorDialog() {
        dialogError.show(SimpleDialogData(
                "INVALID CREDENTIALS",
                "Your credentials are invalid, please retry it",
                accept = "Accept"
        ))
    }

    private fun checkError(textView: TextView) {
        textView.visibility = View.VISIBLE
    }

    private fun hideErrors() {
        tvLightLoginErrorUser.visibility = View.GONE
        tvLightLoginErrorPassword.visibility = View.GONE
    }


    private fun onObserveLogin(login: LoginRequest) {
        if (etUser.text.toString() != login.name)
            etUser.setText(login.name)

        if (etPassword.text.toString() != login.password)
            etPassword.setText(login.password)

    }

    private fun onObserveUser(user: User?) {
        hideErrors()
        user?.let {
            val bundle = Bundle().apply { putSerializable("USER", it) }
            findNavController().navigate(R.id.action_homeViewFragment_to_userFragment, bundle)
        }
    }


    override fun onDestroyView() {
        viewModel.login.removeObservers(this)
        viewModel.error.removeObservers(this)
        super.onDestroyView()
    }
}
