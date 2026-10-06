package com.carmabs.ema.presentation.extension

import com.carmabs.domain.model.User

/**
 * Full name of the user, without blank parts.
 */
val User.fullName: String
    get() = fullNameOf(name, surname)

/**
 * Initials of the user, used for avatars.
 */
val User.initials: String
    get() = initialsOf(name, surname)

fun fullNameOf(vararg parts: String): String =
    parts.filter { it.isNotBlank() }.joinToString(" ") { it.trim() }

fun initialsOf(vararg parts: String): String =
    parts.filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.trim().first().uppercase() }
