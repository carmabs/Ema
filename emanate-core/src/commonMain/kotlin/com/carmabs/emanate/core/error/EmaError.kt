package com.carmabs.emanate.core.error

import com.carmabs.emanate.core.model.reflection.Reflection

/**
 * Created by Carlos Mateo Benito on 20/10/24.
 *
 * Exception thrown by a piece of a view model and caught by [EmaErrorPolicy.Catch].
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @param reflection where it was thrown: the class of the view model and, for a middleware or a reducer, the action
 * that failed, like "action Search", or for a side effect the name of the function that launched it
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@ConsistentCopyVisibility
data class EmaError internal constructor(val reflection: Reflection, val exception: Throwable)
