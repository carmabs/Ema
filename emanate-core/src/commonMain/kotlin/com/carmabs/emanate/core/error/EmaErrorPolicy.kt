package com.carmabs.emanate.core.error

/**
 * Created by Carlos Mateo Benito on 12/10/2026.
 *
 * What happens when the middlewares, reducers or side effects of a view model throw an exception, set in
 * [com.carmabs.emanate.core.model.EmaConfiguration.errorPolicy]. Cancellations are never errors.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
sealed interface EmaErrorPolicy {

    /**
     * The exception is caught and written to the logger of the configuration. The action that failed is discarded,
     * or the side effect finishes, and the view model keeps working.
     * @param onError also receives each error, for example to send it to a crash reporter
     */
    class Catch(val onError: ((EmaError) -> Unit)? = null) : EmaErrorPolicy

    /**
     * The exception is thrown again, so the app fails at once. Useful while developing.
     */
    data object Throw : EmaErrorPolicy
}
