package com.carmabs.ema.core.viewmodel

import com.carmabs.ema.core.navigator.EmaDestination
import com.carmabs.ema.core.state.EmaDataState
import kotlinx.coroutines.flow.*


/**
 *
 * Abstract base class for ViewModel in MVVM architecture.
 *
 * @param S is template about the class which will represent the state of the view. It has to implement
 * the [EmaDataState] interface
 *
 * @param D is the template about the available navigation states contained in the [EmaNavigator] used
 * for the feature that implement this ViewModel
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaBaseViewModel<S : EmaDataState, D : EmaDestination> {


}