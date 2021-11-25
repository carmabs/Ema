package com.carmabs.ema.presentation.ui.unlogged.compose

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.carmabs.ema.R
import com.carmabs.ema.android.delegates.emaViewModelSharedDelegate
import com.carmabs.ema.android.di.instanceDirect
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.presentation.base.BaseComposableFragment
import com.carmabs.ema.presentation.ui.unlogged.EmaAndroidUnloggedToolbarViewModel
import com.carmabs.ema.presentation.ui.unlogged.EmaAndroidUnloggedViewModel
import com.carmabs.ema.presentation.ui.unlogged.EmaUnloggedNavigator
import com.carmabs.ema.presentation.ui.unlogged.EmaUnloggedState
import com.carmabs.ema.presentation.ui.unlogged.EmaUnloggedToolbarState
import com.carmabs.ema.presentation.ui.unlogged.EmaUnloggedViewModel
import org.kodein.di.direct
import org.kodein.di.instance

class EmaComposableUnloggedViewFragment :
    BaseComposableFragment<EmaUnloggedState, EmaUnloggedViewModel, EmaUnloggedNavigator.Navigation>() {

    /**
     * If you wouldn't want to use dependency injection you can provide it instantiating the class.
     * Not recommended
     */
    private val toolbarViewModel: EmaAndroidUnloggedToolbarViewModel by emaViewModelSharedDelegate(
        {
            EmaAndroidUnloggedToolbarViewModel(instanceDirect())
        }
    ) {
        vm.onToolbarUpdated(it as EmaState<EmaUnloggedToolbarState>)
    }

    override
    val navigator: EmaUnloggedNavigator by instance()

    override val androidViewModelSeed: EmaAndroidViewModel<EmaUnloggedViewModel>
        get() = EmaAndroidUnloggedViewModel(instanceDirect())

    override fun onStart() {
        super.onStart()
        vm.toolbarViewModel = toolbarViewModel.emaViewModel
    }

    @Composable
    override fun onNormal(data: EmaUnloggedState) {
        val bottomMargin = 10.dp
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .height(IntrinsicSize.Max),
            ) {
                Image(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(100.dp),
                    painter = painterResource(id = R.drawable.ic_error),
                    contentDescription = "Image error"
                )
                Spacer(modifier = Modifier.height(bottomMargin))
                Text(
                    text = stringResource(id = R.string.error_invalid_credentials),
                    fontSize = 33.sp,
                    color = colorResource(id = R.color.black),
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .offset(0.dp, 30.dp),
            ) {
                if (data.showToolbarViewVisibility) {
                    AppButton2(
                        onClick = { vm.onActionToolbar() },
                        text = stringResource(R.string.error_hide_toolbar)
                    )
                    Spacer(modifier = Modifier.height(bottomMargin))

                }
                AppButton(
                    onClick = { vm.onActionAddUser() },
                    text = stringResource(R.string.error_add_user)
                )

                Spacer(modifier = Modifier.height(bottomMargin))
                AppButton(
                    text = stringResource(id = R.string.error_show_dialog),
                    onClick = { vm.onActionShowDialog() }
                )
                Spacer(modifier = Modifier.height(bottomMargin))
                AppButton(
                    text = stringResource(id = R.string.error_show_single_event),
                    onClick = { vm.onActionSingleEvent() }
                )
            }
        }
    }

    @Composable
    override fun onAlternative(data: EmaExtraData) {
        when (data.type) {
            EmaUnloggedViewModel.EXTRA_DATA_SHOW_DIALOG -> {
                val dialogData = data.extraData as String
                Dialog(onDismissRequest = {vm.onActionHideDialog() }) {
                    DialogContent(dialogMessage = dialogData)
                }

            }
        }
    }

    override fun onSingle(data: EmaExtraData) {
        Toast.makeText(requireContext(), R.string.error_toast_sample, Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun DialogContent(dialogMessage: String) {
        Box(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.space_big_huge))
                .background(
                    color = colorResource(id = R.color.white),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.cardview_default_radius))
                )
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(
                                top = dimensionResource(id = R.dimen.space_big),
                                end = dimensionResource(
                                    id = R.dimen.space_big
                                )
                            ),
                        painter = painterResource(id = R.drawable.ic_cross),
                        contentDescription = "Cancel"
                    )
                    Image(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(dimensionResource(id = R.dimen.icon_size_huge)),
                        painter = painterResource(id = R.drawable.ic_cross),
                        contentDescription = "Content image"
                    )
                    Text(
                        modifier = Modifier
                            .padding(
                                top = dimensionResource(id = R.dimen.space_big),
                            )
                            .align(
                                Alignment.CenterHorizontally
                            ),
                        text = stringResource(id = R.string.error_dialog_title),
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.black)
                    )
                }
                Text(
                        modifier = Modifier
                            .padding(
                                top = dimensionResource(id = R.dimen.space_big),
                            )
                            .align(Alignment.CenterHorizontally)
                            .wrapContentSize(),
                        text = dialogMessage,
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.grayDark)
                    )

                val padding = dimensionResource(id = R.dimen.space_big_huge)
                val paddingTop = dimensionResource(id = R.dimen.space_very_big)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(padding, paddingTop, padding, padding)
                        .fillMaxWidth(),
                ) {
                    AppButton2(
                        modifier = Modifier.wrapContentSize()
                        ,
                        text = stringResource(id = R.string.dialog_no),
                        onClick = {
                            // Change the state to close the dialog
                            vm.onActionHideDialog()
                        },
                    )
                    AppButtonAccent(
                        modifier = Modifier.wrapContentSize()
                        ,
                        text = stringResource(id = R.string.dialog_yes),
                        onClick = {
                            // Change the state to close the dialog
                            vm.onActionHideDialog()
                        },
                    )
                }

            }
        }
    }

    @Composable
    override fun onError(error: Throwable) {

    }

    @Preview(
        device = Devices.NEXUS_5
    )
    @Composable
    fun OnNormalPreview() {
        onNormal(EmaUnloggedState(showToolbarViewVisibility = true))
    }

    @Preview(
        device = Devices.NEXUS_5
    )
    @Composable
    fun OnDialogPreview() {
        DialogContent("Preview sample")
    }

    @Preview
    @Composable
    fun AppButtonPreview() {
        AppButton(text = "Test") {

        }
    }

    @Preview
    @Composable
    fun AppButton2Preview() {
        AppButton2(text = "Test") {

        }
    }

    @Composable
    fun AppButton(text: String, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
        Button(
            modifier = modifier,
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
        ) {
            Text(
                text = text,
                color = colorResource(id = R.color.white)
            )
        }
    }

    @Composable
    fun AppButtonAccent(
        text: String,
        modifier: Modifier = Modifier.fillMaxWidth(),
        onClick: () -> Unit
    ) {
        Button(
            modifier = modifier,
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorAccent))
        ) {
            Text(
                text = text,
                color = colorResource(id = R.color.white)
            )
        }
    }

    @Composable
    fun AppButton2(
        text: String,
        modifier: Modifier = Modifier.fillMaxWidth(),
        onClick: () -> Unit
    ) {
        Button(
            modifier = modifier,
            onClick = onClick,
            border = BorderStroke(1.dp, colorResource(id = R.color.colorAccent)),
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.white))
        ) {
            Text(
                text = text,
                color = colorResource(id = R.color.colorAccent)
            )
        }
    }

}