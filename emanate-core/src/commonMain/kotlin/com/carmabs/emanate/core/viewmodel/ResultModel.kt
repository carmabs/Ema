package com.carmabs.emanate.core.viewmodel

import com.carmabs.emanate.core.EmaInternalApi

/**
 * Model to handle activity result feature
 *
 * @id Id for the code for activity result
 * @implementation Function to handle activity result. Return true to remove the listener after use it
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
@EmaInternalApi
class ResultModel internal constructor(val key: String, val data: Any?, val ownerId: String)
