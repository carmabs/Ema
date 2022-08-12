package com.carmabs.ema.android.navigation

import androidx.fragment.app.Fragment
import com.carmabs.ema.core.navigator.EmaEmptyDestination

/**
 * Created by Carlos Mateo Benito on 29/7/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 *  Navigator to handle navigation through navController in a Fragment
 * Created by: Carlos Mateo Benito on 29/07/22.
 */
class EmaFragmentNavControllerNavigatorBack(
    fragment:Fragment
) : EmaFragmentNavControllerNavigator<EmaEmptyDestination>(fragment) {
    override fun navigate(navigationTarget: EmaEmptyDestination) = Unit
}