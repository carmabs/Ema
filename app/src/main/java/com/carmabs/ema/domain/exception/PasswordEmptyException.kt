package com.carmabs.ema.domain.exception

/**
 * TODO: Add a class header comment.
 *

 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

data class PasswordEmptyException(override val message:String= "Password cannot be empty") : Exception()