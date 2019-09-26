package com.carmabs.ema.presentation.ui.home

import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.carmabs.domain.exception.LoginException
import com.carmabs.domain.exception.PasswordEmptyException
import com.carmabs.domain.exception.UserEmptyException
import com.carmabs.ema.R
import com.carmabs.ema.core.dialog.EmaDialogProvider
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.DIALOG_TAG_LOADING
import com.carmabs.ema.presentation.base.BaseFragment
import com.carmabs.ema.presentation.dialog.loading.LoadingDialogData
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogData
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogListener
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogProvider
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_password.*
import kotlinx.android.synthetic.main.layout_user.*
import org.kodein.di.generic.instance

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
class EmaHomeFragment : BaseFragment<EmaHomeState, EmaHomeViewModel, EmaHomeNavigator.Navigation>() {

    override val inputStateKey: String? = null

    override fun getFragmentLayout(): Int = R.layout.fragment_home

    override val viewModelSeed: EmaHomeViewModel by instance()

    override val navigator: EmaHomeNavigator  by instance()

    //As we can see we can use an instance by kodein or generate it by class instance
    private val errorDialog: EmaDialogProvider by lazy { SimpleDialogProvider(requireFragmentManager()) }

    private val loadingDialog: EmaDialogProvider by instance(tag = DIALOG_TAG_LOADING)
    /////////////////////////////////////////////////////////////////////////////////

    override fun onInitialized(viewModel: EmaHomeViewModel) {
        setupButtons(viewModel)
        setupDialog(viewModel)
    }

    private fun setupDialog(viewModel: EmaHomeViewModel) {
        errorDialog.dialogListener = object : SimpleDialogListener {
            override fun onCancelClicked() {
                viewModel.onActionDialogErrorCancel()
            }

            override fun onBackPressed() {
                viewModel.onActionDialogErrorCancel()
            }

            override fun onConfirmClicked() {
                viewModel.onActionDialogErrorAccept()
            }

        }
    }

    private fun setupButtons(viewModel: EmaHomeViewModel) {
        swLightLoginRememberPassword.setOnCheckedChangeListener { _, isChecked -> viewModel.onActionRemember(isChecked) }
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
            viewModel.onActionLogin()
        }
    }

    private fun checkError(textView: TextView) {
        textView.visibility = View.VISIBLE
    }

    private fun hideErrors() {
        tvLightLoginErrorUser.visibility = View.GONE
        tvLightLoginErrorPassword.visibility = View.GONE
        errorDialog.hide()
    }

    private fun hideLoading() {
        loadingDialog.hide()
    }

    private fun showErrorDialog() {
        errorDialog.show(SimpleDialogData(
                getString(R.string.home_invalid_credentials_title),
                getString(R.string.home_invalid_credentials_message),
                accept = getString(R.string.dialog_accept)
        ))
    }

    private fun showLoadingDialog() {
        loadingDialog.show(
                LoadingDialogData(
                        getString(R.string.home_loading_title),
                        getString(R.string.home_loading_title_message)
                ))
    }


    override fun onNormal(data: EmaHomeState) {

        //We hide the dialogs in normal state
        hideErrors()
        hideLoading()

        if (etUser.text.toString() != data.userName)
            etUser.setText(data.userName)

        if (etPassword.text.toString() != data.userPassword)
            etPassword.setText(data.userPassword)

        swLightLoginRememberPassword.isChecked = data.rememberUser

        data.showPassword.let {
            if (it) {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                etPassword.transformationMethod = null
            }
        }
    }

    override fun onLoading(data: EmaExtraData) {
        showLoadingDialog()
    }

    override fun onSingle(data: EmaExtraData) {
        when (data.type) {
            EmaHomeViewModel.EVENT_MESSAGE -> Toast.makeText(requireContext(), data.extraData as String, Toast.LENGTH_LONG).show()
        }
    }

    override fun onError(error: Throwable) {
        when (error) {
            is UserEmptyException -> checkError(tvLightLoginErrorUser)
            is PasswordEmptyException -> checkError(tvLightLoginErrorPassword)
            is LoginException -> showErrorDialog()
        }
    }
}