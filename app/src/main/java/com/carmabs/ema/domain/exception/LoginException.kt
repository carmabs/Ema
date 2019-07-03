package com.carmabs.ema.domain.exception

/**
 * TODO: Add a class header comment.
 *

 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

data class LoginException(override val message:String= "Login failed, please retry it") : Exception()