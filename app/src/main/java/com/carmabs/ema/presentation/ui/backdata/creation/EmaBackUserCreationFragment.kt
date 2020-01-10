package com.carmabs.ema.presentation.ui.backdata.creation

import android.widget.Toast
import com.carmabs.ema.R
import com.carmabs.ema.android.extra.EmaTextWatcher
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseFragment
import com.carmabs.ema.presentation.ui.backdata.EmaBackNavigator
import kotlinx.android.synthetic.main.fragment_back_result.*
import org.kodein.di.generic.instance

/**
 * <p>
 * Copyright (c) 2019, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 *
 * Date: 2019-11-07
 */

class EmaBackUserCreationFragment : BaseFragment<EmaBackUserCreationState, EmaBackUserCreationViewModel, EmaBackNavigator.Navigation>() {

    override val inputStateKey: String? = null

    override fun onInitialized(viewModel: EmaBackUserCreationViewModel) {
        setupButtons(viewModel)
        setupEditTexts(viewModel)
    }

    private fun setupEditTexts(viewModel: EmaBackUserCreationViewModel) {

        etBackResultName.addTextChangedListener(EmaTextWatcher {
            viewModel.onActionNameWrite(it)
        })

        etBackResultSurname.addTextChangedListener(EmaTextWatcher {
            viewModel.onActionSurnameWrite(it)
        })
    }


    private fun setupButtons(viewModel: EmaBackUserCreationViewModel) {

        bBackResultAccept.setOnClickListener {
            val name = etBackResultName.text.toString()
            val surname = etBackResultSurname.text.toString()
            viewModel.onActionAddUser(
                    name = name,
                    surname = surname
            )
        }
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_back_result

    override
    val viewModelSeed: EmaBackUserCreationViewModel by instance()

    override
    val navigator: EmaBackNavigator by instance()

    override fun onNormal(data: EmaBackUserCreationState) {
        etBackResultName.setText(data.name)
        etBackResultSurname.setText(data.surname)
    }

    override fun onLoading(data: EmaExtraData) {
    }

    override fun onSingle(data: EmaExtraData) {
        Toast.makeText(requireContext(), data.extraData as String, Toast.LENGTH_SHORT).show()
    }

    override fun onError(error: Throwable) {

    }
}