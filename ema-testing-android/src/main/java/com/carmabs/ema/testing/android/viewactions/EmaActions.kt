package com.carmabs.ema.testing.android.viewactions

import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.*


/**
 * Class to handle custom test espresso actions
 *

 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

object EmaActions {

    fun click(): ViewAction {
        return GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER, Press.FINGER)
    }
}
