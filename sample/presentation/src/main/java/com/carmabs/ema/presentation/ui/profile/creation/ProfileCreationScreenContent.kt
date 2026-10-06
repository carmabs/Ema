package com.carmabs.ema.presentation.ui.profile.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carmabs.domain.model.Role
import com.carmabs.ema.android.extension.toEmaText
import com.carmabs.ema.compose.action.EmaImmutableActionDispatcher
import com.carmabs.ema.compose.action.EmaImmutableActionDispatcherEmpty
import com.carmabs.ema.core.model.EmaText
import com.carmabs.ema.presentation.base.compose.BaseScreenComposable
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogData
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogListener
import com.carmabs.ema.presentation.ui.compose.AppButton
import com.carmabs.ema.sample.ema.R

class ProfileCreationScreenContent :
    BaseScreenComposable<ProfileCreationState, ProfileCreationAction, ProfileCreationEvent>() {

    //When a dialog is shown, it handles the back press itself
    override fun onBack(state: ProfileCreationState): ProfileCreationAction? =
        if (state.overlap == null) ProfileCreationAction.OnBack else null

    @Composable
    override fun onState(
        state: ProfileCreationState,
        actions: EmaImmutableActionDispatcher<ProfileCreationAction>
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(
                        id = R.string.profile_creation_create_title,
                        state.roleText
                    )
                )
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    value = state.name,
                    label = {
                        Text(stringResource(id = R.string.profile_creation_create_name))
                    },
                    onValueChange = {
                        actions.dispatch(ProfileCreationAction.UserNameWritten(it))
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.surname,
                    label = {
                        Text(stringResource(id = R.string.profile_creation_create_surname))
                    },
                    onValueChange = {
                        actions.dispatch(ProfileCreationAction.UserSurnameWritten(it))
                    }
                )

                AppButton(
                    text = stringResource(id = R.string.profile_creation_create_user),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    actions.dispatch(ProfileCreationAction.CreateClicked)
                }
            }
        }

        state.overlap?.also {
            Overlap(it, actions)
        }
    }

    @Composable
    private fun Overlap(
        overlap: ProfileCreationOverlap,
        actions: EmaImmutableActionDispatcher<ProfileCreationAction>
    ) {
        when (val dialog = overlap) {
            ProfileCreationOverlap.DialogBackConfirmation -> {
                ShowDialog(
                    data = SimpleDialogData(
                        title = EmaText.id(R.string.profile_creation_user_exit_title),
                        message = EmaText.id(R.string.profile_creation_user_exit_message),
                        proportionWidth = 0.8f,
                        showCancel = true,
                        image = R.drawable.ic_exit
                    ),
                    listener = object : SimpleDialogListener {
                        override fun onCancelClicked() {
                            actions.dispatch(ProfileCreationAction.DialogBackCancel)
                        }

                        override fun onConfirmClicked() {
                            actions.dispatch(ProfileCreationAction.DialogBackConfirm)
                        }

                        override fun onBackPressed() {
                            actions.dispatch(ProfileCreationAction.DialogBackCancel)
                        }
                    }
                )
            }

            is ProfileCreationOverlap.DialogUserCreated -> {
                val image = when (dialog.role) {
                    Role.ADMIN -> R.drawable.ic_admin
                    Role.BASIC -> R.drawable.ic_user
                }
                val title = when (dialog.role) {
                    Role.ADMIN -> R.string.profile_creation_user_admin_title
                    Role.BASIC -> R.string.profile_creation_user_basic_title
                }
                ShowDialog(data = SimpleDialogData(
                    title = EmaText.id(title),
                    message = R.string.profile_creation_user_message.toEmaText(),
                    showCancel = true,
                    image = image,
                    proportionWidth = 0.9f
                ),
                    listener = object : SimpleDialogListener {
                        override fun onCancelClicked() {
                            actions.dispatch(ProfileCreationAction.DialogCancelClicked)
                        }

                        override fun onConfirmClicked() {
                            actions.dispatch(ProfileCreationAction.DialogConfirmClicked)
                        }

                        override fun onBackPressed() {
                            actions.dispatch(ProfileCreationAction.DialogCancelClicked)
                        }

                    })
            }
        }
    }

    @Preview(device = Devices.NEXUS_5)
    @Composable
    private fun NormalPreview() {
        onState(
            state = ProfileCreationState(
                Role.ADMIN,
                "Carlos",
                "Mateo"
            ),
            actions = EmaImmutableActionDispatcherEmpty()
        )
    }

    @Preview(device = Devices.PIXEL_3)
    @Composable
    private fun OverlappedPreview() {
        onState(
            state = ProfileCreationState(
                Role.ADMIN,
                "Carlos",
                "Mateo",
                ProfileCreationOverlap.DialogUserCreated(Role.ADMIN)
            ),
            actions = EmaImmutableActionDispatcherEmpty()
        )
    }
}