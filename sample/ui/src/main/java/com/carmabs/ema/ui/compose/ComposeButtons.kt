package com.carmabs.ema.ui.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.carmabs.ema.sample.ema.R
import com.carmabs.ema.ui.theme.EmaSampleTheme

@Composable
fun AppButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.height(dimensionResource(id = R.dimen.button_height)),
        enabled = enabled,
        onClick = onClick
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

//region Previews

@Preview
@Composable
fun AppButtonPreview() {
    EmaSampleTheme {
        AppButton(text = "Test") {

        }
    }
}

//endregion
