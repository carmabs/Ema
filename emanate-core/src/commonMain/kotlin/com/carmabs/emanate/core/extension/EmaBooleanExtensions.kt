package com.carmabs.emanate.core.extension

/*
 * Created by Carlos Mateo Benito on 2023-01-05.
 *
 * <p>
 * Copyright (c) 2019 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

/**
 * Runs [action] if the value is true.
 * @return the same value, to chain other calls
 */
inline fun Boolean.ifTrue(action: () -> Unit): Boolean {
    if (this) {
        action.invoke()
    }
    return this
}

/**
 * Runs [action] if the value is true.
 * @return the result of [action], or null if the value is false
 */
inline fun <R> Boolean.ifTrueLet(action: () -> R): R? = if (this) {
    action.invoke()
} else {
    null
}

/**
 * Runs [action] if the value is false.
 * @return the same value, to chain other calls
 */
inline fun Boolean.ifFalse(action: () -> Unit): Boolean {
    if (!this) {
        action.invoke()
    }
    return this
}

/**
 * Runs [action] if the value is false.
 * @return the result of [action], or null if the value is true
 */
inline fun <R> Boolean.ifFalseLet(action: () -> R): R? = if (!this) {
    action.invoke()
} else {
    null
}
