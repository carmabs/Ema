package com.carmabs.ema.domain.exception

/**
 * TODO: Add a class header comment.
 *

 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

data class UserEmptyException(override val message:String= "User cannot be empty") : Exception()