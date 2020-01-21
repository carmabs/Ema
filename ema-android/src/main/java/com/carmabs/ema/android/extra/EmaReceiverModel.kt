package com.carmabs.ema.android.extra

/**
 * Model to handle receiver feature
 * @ownerCode code to check if the receiver caller is not the same that result caller
 * @id Id for the receiver id. It has to match with the result id
 * @function Function to execute when result is received
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
data class EmaReceiverModel(
        val resultId: Int,
        internal val function: (EmaResultModel) -> Unit,
        internal val ownerCode: Int
)