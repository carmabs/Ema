package com.carmabs.ema.presentation.ui.backdata.creation

import android.widget.Toast
import com.carmabs.ema.R
import com.carmabs.ema.android.extra.EmaTextWatcher
import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.extension.checkNull
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseFragment
import com.carmabs.ema.presentation.ui.backdata.EmaBackNavigator
import kotlinx.android.synthetic.main.fragment_back_result.*
import org.kodein.di.generic.instance

/**
 *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Date: 2019-11-07
 *
 * Use of EmaEditText.
 */

class EmaBackUserCreationFragment : BaseFragment<EmaBackUserCreationState, EmaBackUserCreationViewModel, EmaBackNavigator.Navigation>() {

    override fun onInitialized(viewModel: EmaBackUserCreationViewModel) {
        setupButtons(viewModel)
        setupEditTexts(viewModel)
    }

    private fun setupEditTexts(viewModel: EmaBackUserCreationViewModel) {

        ///With EmaEditText is not necessary the following code to add a textwatcher to update the
        ///state in viewmodel

        /*
        etBackResultSurname.addTextChangedListener(object : TextWatcher {
               override fun afterTextChanged(s: Editable?) {
               }

               override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
               }

               override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                   viewModel.onActionSurnameWrite(s?.toString() ?: STRING_EMPTY)
               }
        })*/

        ///Use the next code ///

        etBackResultName.setEmaTextWatcherListener {
            viewModel.onActionNameWrite(it.checkNull())
        }
        etBackResultSurname.setEmaTextWatcherListener {
            viewModel.onActionSurnameWrite(it.checkNull())
        }
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

    override val layoutId: Int = R.layout.fragment_back_result

    override val viewModelSeed: EmaBackUserCreationViewModel by instance()

    override val navigator: EmaBackNavigator by instance()

    override fun onNormal(data: EmaBackUserCreationState) {

        ///Using the EmaEditText it handles text updates to avoid infinite loops described in
        ///HomeViewFragment

        etBackResultName.setText(data.name)
        etBackResultSurname.setText(data.surname)
    }

    override fun onAlternative(data: EmaExtraData) {
    }

    override fun onSingle(data: EmaExtraData) {
        Toast.makeText(requireContext(), data.extraData as String, Toast.LENGTH_SHORT).show()
    }

    override fun onError(error: Throwable) {

    }
}