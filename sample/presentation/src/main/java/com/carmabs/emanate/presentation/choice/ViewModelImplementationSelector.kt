package com.carmabs.emanate.presentation.choice

import kotlin.concurrent.Volatile

/**
 * Keeps the implementation of the view models chosen when the sample starts. There is one for the whole app.
 *
 * <p>
 * Copyright (c) 2026, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
class ViewModelImplementationSelector {

    /**
     * The recommended one until another is chosen, for example if the app is restored after being killed
     */
    @Volatile
    var implementation: ViewModelImplementation = ViewModelImplementation.EMA_VIEW_MODEL
}
