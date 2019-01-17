package com.carmabs.ema.presentation.ui.defaultvm

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.carmabs.ema.R
import com.carmabs.ema.domain.LoginRequest
import com.carmabs.ema.domain.User
import com.carmabs.ema.domain.exception.LoginException
import com.carmabs.ema.domain.exception.PasswordEmptyException
import com.carmabs.ema.domain.exception.UserEmptyException
import com.carmabs.ema.presentation.dialog.DialogProvider
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogData
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogProvider
import kotlinx.android.synthetic.main.fragment_light_login.*
import kotlinx.android.synthetic.main.layout_password.*
import kotlinx.android.synthetic.main.layout_user.*
import java.lang.Exception


class DefaultVmFragment : Fragment() {

    companion object {
        fun newInstance() = DefaultVmFragment()
    }

    private lateinit var viewModel: DefaultVmViewModel

    private val dialog: DialogProvider by lazy {
        SimpleDialogProvider(fragmentManager!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_light_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupButtons()
        viewModel = ViewModelProviders.of(this).get(DefaultVmViewModel::class.java)
        viewModel.user.observe(this, Observer(::onObserveUser))
        viewModel.error.observe(this, Observer(::onObserveError))
    }

    private fun setupButtons() {

        bLightLoginSign.setOnClickListener {
            viewModel.onActionLogin(LoginRequest(
                    etUser.text.toString(),
                    etPassword.text.toString()
            ))
        }
    }

    private fun onObserveError(exception: Exception) {
        when(exception){
            is UserEmptyException -> checkError(tvLightLoginErrorUser)
            is PasswordEmptyException -> checkError(tvLightLoginErrorPassword)
            is LoginException -> showDialog()
        }
    }

    private fun showDialog(){
        dialog.show(SimpleDialogData(
                "INVALID CREDENTIALS",
                "Your credentials are invalid, please retry it",
                accept = "Accept"
        ))
    }

    private fun checkError(textView: TextView){
        textView.visibility = View.VISIBLE
    }

    private fun hideErrors(){
        tvLightLoginErrorUser.visibility = View.GONE
        tvLightLoginErrorPassword.visibility = View.GONE
    }



    private fun onObserveUser(user: User) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
