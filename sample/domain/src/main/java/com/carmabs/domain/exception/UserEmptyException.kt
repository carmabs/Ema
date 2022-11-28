package com.carmabs.domain.exception

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

data class UserEmptyException(override val message:String= "User cannot be empty") : Exception()